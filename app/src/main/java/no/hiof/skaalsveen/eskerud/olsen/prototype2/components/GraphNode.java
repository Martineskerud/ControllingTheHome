package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.PhysicalObject;

public abstract class GraphNode implements PhysicalObject {

    private static final String TAG = "GraphNode";
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

    public GraphNode() {

        graphNodeEvent = new GraphNodeEvent();
        actionQueue = new ArrayList<Runnable>();
    }

    public void setActive(boolean b) {

        if (graphNodeListener != null && b != isActive) { // change in state

            if (b) { // finger down

                pressedTimestamp = System.currentTimeMillis();
                pressedPositionX = x;
                pressedPositionY = y;

            } else { // finger up


                long deltaTime = System.currentTimeMillis() - pressedTimestamp;
                double deltaPosition = Math.sqrt(Math.pow((x - pressedPositionX), 2)
                        + Math.pow((y - pressedPositionY), 2));

                double moveOnClickThreshold = getRadius();//GraphNodeEvent.MOVE_ON_CLICK_THRESHOLD;
                if (deltaTime > GraphNodeEvent.CLICK_THRESHOLD &&
                        deltaTime < GraphNodeEvent.LONG_PRESS_THRESHOLD
                        && deltaPosition < moveOnClickThreshold && !isLongPressing) { // single
                    // click
                    graphNodeEvent.setEvent(GraphNodeEvent.CLICK);
                    graphNodeListener.onEvent(graphNodeEvent, this);
                }
                else if(isMovable){

                    graphNodeEvent.setEvent(GraphNodeEvent.MOVE_UP);
                    graphNodeListener.onEvent(graphNodeEvent, this);
                }

                isMovable = false;
                isMoving = false;
                isLongPressing = false;
                onFingerUp();
            }

        }

        isActive = b;
    }


    public void setGraphNodeListener(GraphNodeListener graphNodeListener) {
        this.graphNodeListener = graphNodeListener;
    }

    protected void onFingerUp() {

    }

    public abstract void draw(Canvas canvas);

    public void update(long tpf) {

        if (graphNodeListener != null && isActive) {

            long deltaTime = System.currentTimeMillis() - pressedTimestamp;

            double deltaPosition = Math.sqrt(Math.pow((x - pressedPositionX), 2)
                    + Math.pow((y - pressedPositionY), 2));

            if (outsideThreshold(deltaPosition) && !isLongPressing) {
                onStartMove();
                graphNodeEvent.setEvent((isMoving ? GraphNodeEvent.MOVE : GraphNodeEvent.MOVE_START));// move
                graphNodeListener.onEvent(graphNodeEvent, this);
                isMoving = true;
                isMovable = true; // not really here

            } else if (deltaTime > GraphNodeEvent.LONG_PRESS_THRESHOLD && pressedTimestamp > 0) {
                pressedTimestamp = 0;
                isMoving = false;
                isMovable = false;
                isLongPressing = true;
                graphNodeEvent.setEvent(GraphNodeEvent.LONG_PRESS);// press
                graphNodeListener.onEvent(graphNodeEvent, this);

            }
        }

        if (!isActive) {

            ox += (x - ox) * tpf * 0.01;
            oy += (y - oy) * tpf * 0.01;

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
        Log.d(TAG, "Moving " + this + " to (" + x + ", " + y + ")");
        moveX(x);
        moveY(y);
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


}