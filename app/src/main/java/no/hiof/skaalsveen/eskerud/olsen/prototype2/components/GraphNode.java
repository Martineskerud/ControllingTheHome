package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.ActivityEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ActivityEventListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.PhysicalObject;

public abstract class GraphNode implements PhysicalObject {

    private static final String TAG = "GraphNode";
    private final Paint debugPaint;
    private final int debugTextSize;
    protected final ActivityEventListener activityEventListener;
    protected String name;
    protected boolean isActive;
    protected GraphNodeListener graphNodeListener;
    protected GraphNodeEvent graphNodeEvent;

    protected long pressedTimestamp = 0;
    protected float pressedPositionX = 0;
    protected float pressedPositionY = 0;

    protected boolean isMovable = false;

    protected float x, y, ox, oy;
    protected boolean isMoving = false;
    private boolean isLongPressing;
    protected float radius = 50;


    // force
    protected boolean useTheForce;
    protected double socialfx = 0;
    protected double socialfy = 0;
    public boolean minimized = true;
    private ArrayList<Runnable> actionQueue;
    protected boolean debugPosition;
    private ArrayList<Connection> connections;
    protected float cursorX;
    protected float cursorY;
    private boolean isPressed = false;
    private boolean movedOutOfNode;
    private boolean isPressedOutside;
    private boolean movingOutsideNode;
    protected boolean connectionsVisible = false;
    protected boolean isDebuggable;

    public GraphNode(String name, ActivityEventListener activityEventListener) {
        this.activityEventListener = activityEventListener;
        this.name = name;
        graphNodeEvent = new GraphNodeEvent();
        actionQueue = new ArrayList<Runnable>();

        debugPaint = new Paint();
        debugPaint.setAntiAlias(true);
        debugPaint.setColor(Color.BLACK);
        debugTextSize = 10;
        debugPaint.setTextSize(debugTextSize);

        connections = new ArrayList<Connection>();
    }

    protected String getDebugInfo(){
        String s = this + "\n(" + round(ox) + "," + round(oy) + " | " + round(x) + "," + round(y) + " R=" + round(getRadius()) + ")";
        s+= "\n isMoving="+fromBoolean(isMoving);
        s+= " isMovable="+fromBoolean(isMovable);
        s+= "\n movingOutsideNode="+fromBoolean(movingOutsideNode);

        s+= "\n isPressed="+fromBoolean(isPressed);
        s+= " isLongPressing="+fromBoolean(isLongPressing);
        s+= "\n isPressedOutside="+fromBoolean(isPressedOutside);

        s+= "\ncursorX="+round(cursorX);
        s+= " cursorY="+round(cursorY);

        s+= "\nisActive="+fromBoolean(isActive);
        s+= "\n Connections="+(connections!=null ? connections.size() : "null");


        return s;
    }

    protected String fromBoolean(boolean bool) {
        return (bool ? "1" : "0");
    }

    protected int round(float d) {
        return Math.round(d);
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setDebuggable(boolean debuggable) {
        this.isDebuggable = debuggable;
    }

    public String getName() {
        return name;
    }

    private class Connection{

        private final GraphNode node;
        private Edge curve;

        public Connection(GraphNode node, BezierCurve curve){
            this.node = node;
            this.curve = curve;
        }

        public Connection(GraphNode node) {
            this.node = node;

            if(node instanceof RoomNode){
                this.curve = new Edge<GraphNode, GraphNode>(debugPaint, "Connection");
            }
            else if(node instanceof ChildNode) {
                this.curve = new BezierCurve(debugPaint);
            }
        }

        public GraphNode getNode() {
            return node;
        }
//
//        public BezierCurve getCurve() {
//            return curve;
//        }

        public void draw(Canvas canvas) {
            if(curve != null && node != null){
                curve.draw(canvas);
            }
        }

        public void update(long tpf) {
            if(curve != null && node != null){
                curve.update(GraphNode.this, node);

//                if(GraphNode.this instanceof ChildNode){
//                    if(node instanceof ChildNode){
//                        curve.update((ChildNode)GraphNode.this, (ChildNode)node);
//                    }
//                }
            }
        }
    }

    public void handleTouch(boolean b) {

        if (graphNodeListener != null && b != isActive) { // change in state
            //isActive = b;

            if (b && !isPressed) { // finger down

                if(!isPressedOutside && getDistanceTo(cursorX, cursorY) < getRadius()) {
                    pressedTimestamp = System.currentTimeMillis();
                    pressedPositionX = x;
                    pressedPositionY = y;
                    isPressed = true;
                    movedOutOfNode = false;
                    isActive = true;
                }
                else{
                    isPressedOutside = true;
                    isPressed = false;
                    isActive = false;
                }

            } else{ // finger up
                isActive = false;
                movingOutsideNode = false;
                isPressedOutside = false;

                long deltaTime = System.currentTimeMillis() - pressedTimestamp;
                double deltaPosition = Math.sqrt(Math.pow((ox - pressedPositionX), 2)
                        + Math.pow((oy - pressedPositionY), 2));

                double moveOnClickThreshold = getRadius();//GraphNodeEvent.MOVE_ON_CLICK_THRESHOLD;
                boolean notNoise = deltaTime > GraphNodeEvent.CLICK_THRESHOLD;
                boolean withinTime = deltaTime < GraphNodeEvent.LONG_PRESS_THRESHOLD;
                boolean withinPos = deltaPosition < moveOnClickThreshold;

                if(isPressed) {
                    if (movedOutOfNode && !isMoving) {

                        graphNodeEvent.setEvent(GraphNodeEvent.MOVE_UP_FROM_OUTSIDE_OF_NODE);
                        graphNodeListener.onEvent(graphNodeEvent, this);
                        movedOutOfNode = false;

                    } else if (notNoise && withinTime && withinPos && !isLongPressing) { // single
                        // click
                        graphNodeEvent.setEvent(GraphNodeEvent.CLICK);
                        graphNodeListener.onEvent(graphNodeEvent, this);
                    } else if (isMovable && !isMoveDisabled()) {

                        graphNodeEvent.setEvent(GraphNodeEvent.MOVE_UP);
                        graphNodeListener.onEvent(graphNodeEvent, this);
                    }
                }
                isPressed = false;

                isMovable = false;
                isMoving = false;
                isLongPressing = false;
                onFingerUp();

                graphNodeEvent.setEvent(GraphNodeEvent.UP);
                graphNodeListener.onEvent(graphNodeEvent, this);
            }
        }
        else if(!b){
            isPressedOutside = false;
        }

    }

    public boolean isActive() {
        return isActive;
    }

    public void setGraphNodeListener(GraphNodeListener graphNodeListener) {
        this.graphNodeListener = graphNodeListener;
    }

    protected void onFingerUp() {

    }

    protected void draw(Canvas canvas){

        if(debugPosition) {
            canvas.drawText("ox: " + Math.floor(ox), ox + radius, oy - 2 * debugTextSize, debugPaint);
            canvas.drawText("oy: " + Math.floor(oy), ox + radius, oy - debugTextSize, debugPaint);
            canvas.drawText("x: " + Math.floor(pressedPositionX), ox + radius, oy + debugTextSize, debugPaint);
            canvas.drawText("y: " + Math.floor(pressedPositionY), ox + radius, oy + 2 * debugTextSize, debugPaint);
        }

        if(isDebuggable && getDistanceTo(cursorX, cursorY) < 3*getRadius()) {
            drawDebugInfo(canvas);
        }

        drawConnections(canvas);

    }

    private void drawDebugInfo(Canvas canvas) {
        String[] splitInfo = getDebugInfo().split("\n");
        float lineOffset = getDebugLineOffset(splitInfo);
        for(String line : splitInfo){
            canvas.drawText(line, ox + (radius*1.2f), oy + lineOffset, debugPaint);
            lineOffset += debugTextSize;
        }
    }

    protected int getDebugLineOffset(String[] splitInfo) {
        return -(splitInfo.length / 2) * debugTextSize;
    }


    private void drawConnections(Canvas canvas) {

        if(connectionsVisible) {
            int c = 0;
            if (connections != null && connections.size() > 0) {
                for (Connection connection : connections) {
                    connection.draw(canvas);
                    c++;
                }
            }

            //canvas.drawText("C: " + c, ox, oy + 3 * debugTextSize, debugPaint);
        }
    }


    public void update(long tpf) {

        if(isPressed && !isPressedOutside) {
            if (graphNodeListener != null && isActive) {

                long deltaTime = System.currentTimeMillis() - pressedTimestamp;

                double deltaPosition = Math.sqrt(Math.pow((cursorX - pressedPositionX), 2)
                        + Math.pow((cursorY - pressedPositionY), 2));

                if (outsideThreshold(deltaPosition)) {
                    if (!isMoveDisabled() && !movingOutsideNode) {

                        if(!isMoving){
                            onStartMove();
                            isMoving = true;
                            isMovable = true;
                            graphNodeEvent.setEvent(GraphNodeEvent.MOVE_START);// start move
                            graphNodeListener.onEvent(graphNodeEvent, this);
                        }
                        else{
                            graphNodeEvent.setEvent(GraphNodeEvent.MOVE);// move
                            graphNodeListener.onEvent(graphNodeEvent, this);
                        }

                    } else if (!movedOutOfNode) {

                        movedOutOfNode = true;
                        graphNodeEvent.setEvent(GraphNodeEvent.MOVED_OUT_OF_NODE);// MOVE OUT
                        graphNodeListener.onEvent(graphNodeEvent, this);

                    } else {
                        movingOutsideNode = true;
                        graphNodeEvent.setEvent(GraphNodeEvent.MOVING_OUTSIDE_OF_NODE);// MOVE OUT
                        graphNodeListener.onEvent(graphNodeEvent, this);
                    }

                } else if (!isLongPressing && deltaTime > GraphNodeEvent.LONG_PRESS_THRESHOLD && pressedTimestamp > 0) {
                    if (!isMoving && !isMovable) {

                        pressedTimestamp = 0;
                        isLongPressing = true;
                        graphNodeEvent.setEvent(GraphNodeEvent.LONG_PRESS);// press
                        graphNodeListener.onEvent(graphNodeEvent, this);
                    }

                }
            }
        }

        if (!isActive) {

            ox += (x - ox) * tpf * 0.01;
            oy += (y - oy) * tpf * 0.01;

        }
        // follow fingers smooth
        else if (isMoving) {

            float dx = x - ox;
            float dy = y - oy;

            ox += (dx / 20) * tpf;
            oy += (dy / 20) * tpf;
        }

        updateConnections(tpf);
    }

    protected boolean isMoveDisabled() {
        return false;
    }

    private void updateConnections(long tpf) {
        if(connections != null && connections.size() > 0){
            if(connectionsVisible){
                for(Connection connection: connections){
                    connection.update(tpf);
                }
            }
        }
    }

    protected boolean outsideThreshold(double deltaPosition) {
        return deltaPosition > GraphNodeEvent.MOVE_ON_CLICK_THRESHOLD;
    }

    protected abstract void onStartMove();

    private void runActionFromQueue() {
        Runnable action = actionQueue.get(0);
        actionQueue.remove(0);
        if(action != null){
            action.run();
        }
    }


    protected float getDistance(float x1, float x2, float y1, float y2) {
        return (float) Math.sqrt(Math.pow(x1 - x2, 2)
                + Math.pow(y1 - y2, 2));
    }

    public double getDistanceTo(GraphNode node) {
        return getDistanceTo(node.getX(), node.getY());
    }

    public double getDistanceTo(float x2, float y2) {
        return Math.sqrt(Math.pow(getX() - x2, 2) + Math.pow(getY() - y2, 2));
    }

    public void makeDeltaForce(RoomNode n2, long tps) {

        if (useTheForce) {

            float distance = (float) Math.sqrt(Math.pow(n2.ox - ox, 2)
                    + Math.pow(n2.oy - oy, 2));

            float target = 200;

            float dist2 = (distance > 0 ? distance - target : distance + target);


            if (distance > target) distance = target + 10;

            // if(r > 200){
            float cx = (n2.ox - ox);
            float cy = (n2.oy - oy);

            float weight = calculateSocialWeight(n2, distance);

            if (!minimized && !n2.minimized) {

                socialfx += (cx * (distance - target)) + (cx * (distance - target)) * weight;
                socialfy += (cy * (distance - target)) + (cy * (distance - target)) * weight;

            } else if (minimized && n2.minimized) {


                socialfx += ((cx * (distance - target)) + (cx * (distance - target)) * weight) * (dist2 > 0 ? 1 / dist2 : 1);
            }
        }
    }

    protected float calculateSocialWeight(RoomNode n2, float distance) {
        return Math.abs(n2.radius - radius) / (radius / 10);
    }

    public float getX() {
        return ox;
    }

    public float getY() {
        return oy;
    }

    public void setX(float x) {
        ox = x;
        this.x = ox;
    }

    public void setY(float y) {
        oy = y;
        this.y = oy;
    }

    public abstract void setPlacement(float[] po);

    public float getRadius() {
        return radius;
    }

    public void moveTo(float x, float y) {
        //Log.d(TAG, "Moving " + this + " to (" + x + ", " + y + ")");
        moveX(x);
        moveY(y);
    }

    public void saveCursor(float x, float y) {
        cursorX = x;
        cursorY = y;
    }

    public void moveX(float x, Runnable r) {
        moveX(x);
        actionQueue.add(r);
    }

    public void moveY(float y, Runnable r) {
        moveY(y);
        actionQueue.add(r);
    }

    public void moveX(float x) {
        this.x = x;
    }

    public void moveY(float y) {
        this.y = y;
    }

    public static void placeInCircle(float x, float y, float radius, ArrayList<? extends GraphNode> nodes) {

        if(nodes != null && nodes.size() > 0) {

            double fullAngle = Math.PI * 2;
            float angleOffset = 0;

            placeInCircle(x, y, radius, nodes, fullAngle, angleOffset);
        }
    }

    public static void placeInCircle(float x, float y, float radius, ArrayList<? extends GraphNode> nodes, double fullAngle, float angleOffset) {
        float step = (float) (fullAngle / nodes.size());

        for (GraphNode node : nodes) {

            if(!node.isActive && !node.isMoving){
                float radius2 = radius + node.getRadius();

                float rx = (float) (radius2 * Math.sin(angleOffset));
                float ry = (float) (radius2 * Math.cos(angleOffset));

                node.setX(x + rx);
                node.setY(y + ry);
            }

            angleOffset += step;
        }
    }

    public boolean collidesWith(GraphNode node) {

        boolean b1 = getDistanceTo(node) < getRadius() + node.getRadius();
        boolean b3 = node != null;

        Log.d(TAG, (b1? "B1":"!B1")+"::"+(b3 ? "B3": "!B3"));
        Log.d(TAG, getDistanceTo(node)+":::" + getRadius() + " : " + node.getRadius());


        return (b3 && b1);
    }

    public void addConnection(GraphNode node) {
        if(!this.equals(node)) {
            connections.add(new Connection(node));
        }
    }

    protected void clearConnections() {
        connections.clear();
    }

    public boolean performHapticFeedback(int time) {
        if(activityEventListener != null){

            ActivityEvent event = new ActivityEvent<Integer>(ActivityEvent.PERFORM_HAPTIC_FEEDBACK);
            event.addValue(time);
            return activityEventListener.onActivityEvent(event);
        }
        return false;
    }

    public void reportInteraction(GraphNodeEvent graphNodeEvent, GraphNode graphNode) {

        int[] validEvents = new int[]{
                GraphNodeEvent.CLICK,
                GraphNodeEvent.MOVE_START,
                GraphNodeEvent.MOVE_UP,
                GraphNodeEvent.LONG_PRESS,
                GraphNodeEvent.MOVED_OUT_OF_NODE,
                GraphNodeEvent.UP
        };

        for(int state : validEvents){
            if(graphNodeEvent.getEvent() == state){

                ActivityEvent event = new ActivityEvent<String>(ActivityEvent.REPORT_INTERACTION);
                event.addValue("{'node': '" + graphNode + "', 'event': " + graphNodeEvent + "}");
                activityEventListener.onActivityEvent(event);
                break;

            }
        }

    }

}