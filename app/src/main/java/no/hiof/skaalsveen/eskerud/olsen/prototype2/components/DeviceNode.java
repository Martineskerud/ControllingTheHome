package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.ActivityEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

public class DeviceNode extends ChildNode implements GraphNodeListener {

	public static final String TAG = "DeviceNode";

    public static final int TYPE_BOOLEAN = 0;
    public static final int TYPE_ANALOG = 1;
    public static final int TYPE_GROUPED = 2;

    public static final float NORMAL_STROKE_WIDTH = 2.5f;

    private final Paint edgePaint;
    private final int type;
    private final Paint identityPaint;
    private Paint paint;
	public float radius;
	private Paint textPaint;
	private Edge parentLine;
    private int alpha;
    private int fingerId = -1;
    private DeviceNode hoverChild;
    private DeviceNode movingChild;
    private float value = 0f;
    private float targetValue = 0;
    private float savedState = 0f;
    private long stateChangeTimestamp;
    private long dragTime = 0;

    private boolean isHighlighted;
    private boolean isAdjustable;
    private boolean moveDisabled = true;
    private boolean isHoveredOverWithOtherNode = false;
    private boolean isPressed = false;
    public boolean state = false;

    @Override
    protected String getDebugInfo() {

        String[] typeDef = new String[]{"BOOLEAN", "ANALOG", "GROUPED"};

        String debugInfo = super.getDebugInfo();
        debugInfo+= "\n state="+fromBoolean(state);
        debugInfo+= " isPressed="+fromBoolean(isPressed);
        debugInfo+= "\n moveDisabled="+fromBoolean(moveDisabled);
        debugInfo+= " isAdjustable="+fromBoolean(isAdjustable);
        debugInfo+= "\n isHighlighted="+fromBoolean(isHighlighted);
        debugInfo+= "\n isHoveredOverWithOtherNode="+fromBoolean(isHoveredOverWithOtherNode);
        debugInfo+= "\n type="+typeDef[type];

        return debugInfo;
    }

    public DeviceNode(String name, ChildEnabledGraphNode parent, Paint textPaint, int type) {
        super(name, parent);

		this.type = type;

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(NORMAL_STROKE_WIDTH);
		
		this.textPaint = textPaint;
        this.textPaint.setAntiAlias(true);
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setTextAlign(Align.CENTER);

        edgePaint = new Paint();
        edgePaint.setAntiAlias(true);
        edgePaint.setColor(Color.GRAY);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2.5f);

        identityPaint = new Paint();
        identityPaint.setAntiAlias(true);
        identityPaint.setARGB(75, 50,50,50);
        identityPaint.setStyle(Paint.Style.STROKE);
        identityPaint.setStrokeWidth(10f);

        if(type == TYPE_ANALOG) {
            identityPaint.setPathEffect(new DashPathEffect(new float[]{2, 2, 4, 2, 8, 2, 16, 2, 32, 2, 64, 2, 128, 2}, 0));
        }

		radius = 50;
		useTheForce = true;
		
		parentLine = new Edge(edgePaint);
        setGraphNodeListener(this);

        savedState = setValueFromAngle(Math.PI/2*5);
        setValueFromAngle(Math.PI / 2);

        if(name.equals("Stove")){
            String[] devices = new String[]{"Light", "Fireplace", "TV"};
            createChildren(textPaint, devices);
        }
	}


    @Override
    public float getRadius() {
        return super.getRadius()+ (isHoveredOverWithOtherNode ? 10 : 0) + (isHighlighted ? (moveDisabled ? 10 : 0) : 0);
    }

    @Override
	public void draw(Canvas canvas) {

        int oColor;


        boolean isDashed = isHighlighted && moveDisabled;
        if(isDashed){
            oColor = Color.rgb(255,125,0);
            paint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
//            paint.setStrokeWidth(5f);
        }
        else{
            paint.setPathEffect(null);
            paint.setStrokeWidth(NORMAL_STROKE_WIDTH);
        }

        if(isPressed){
            oColor = (isMoving ? Color.GREEN : Color.RED);
        }
        else{
            if(isHoveredOverWithOtherNode){
                oColor = Color.BLACK;
//                paint.setStrokeWidth(7.5f);
            }
            else{
                oColor = Color.BLUE;
            }
        }

        paint.setColor(oColor);

		//paint.setStyle((state ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
		canvas.drawCircle(ox, oy, getRadius() , paint);

        if(!isHoveredOverWithOtherNode){
            drawText(canvas);
        }

		parentLine.draw(canvas);


        if(state){
            identityPaint.setARGB(75, 102, 227, 91);
        }
        else{
            identityPaint.setARGB(75, 50, 50, 50);
        }

        if(!isDashed) {
            if (type == TYPE_ANALOG) {
                drawValueArch(canvas, 1.1f, 0, 1f, identityPaint, -90);
            } else if (type == TYPE_BOOLEAN) {
                canvas.drawCircle(ox, oy, getRadius() * 1.1f, identityPaint);
            }

//            setName("v "+value);
            if(value > 0){
                drawValueArch(canvas, 1.1f, 0, value, paint, -90);
            }


            //drawHelperArrow(canvas);

            //canvas.drawLine(aStartX, oy, aStopX, oy, explanationPaint);

        }

        super.draw(canvas);
	}

    private void drawHelperArrow(Canvas canvas) {
        long deltaDragTime = System.currentTimeMillis() - dragTime;
        if(deltaDragTime < 10000) {
            makeExpArrow(canvas, (deltaDragTime%1000) / 10f);

        }
    }

    private void makeExpArrow(Canvas canvas, float offset) {
        final Path path = new Path();

        float aStartX = ox + getRadius();
        float aStopX = offset + ox + getRadius() * 2;

        float r3 = getRadius() * 0.4f;
        float r4 = getRadius() * 0.5f;
        float r5 = getRadius() * 0.2f;

        path.moveTo(aStartX,    oy+r5);
        path.lineTo(aStopX, oy + r5);
        path.lineTo(aStopX,     oy+r3);
        path.lineTo(aStopX + r4,oy);
        path.lineTo(aStopX,     oy-r3);
        path.lineTo(aStopX,     oy-r5);
        path.lineTo(aStartX,    oy-r5);
        path.close();

        Paint explanationPaint = new Paint(edgePaint);
        explanationPaint.setARGB((int) (100-offset), 50, 50, 50);
        explanationPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        explanationPaint.setStrokeWidth(2.5f);
        canvas.drawPath(path, explanationPaint);

        Paint explanationPaint2 = new Paint(explanationPaint);
        explanationPaint2.setTextSize(getRadius() * 0.3f);
        explanationPaint2.setStyle(Paint.Style.FILL);
        explanationPaint2.setColor(Color.WHITE);
        canvas.drawTextOnPath("DRAG", path, offset+r5, -r5 / 2, explanationPaint2);
    }

    private void drawValueArch(Canvas canvas, float scale, float startVal, float val, Paint paint, int offset) {
        final RectF oval = new RectF();

        float r = radius * scale;
        oval.set(getX()-r, getY()-r, getX()+ r, getY()+ r);
        canvas.drawArc(oval, ((360* startVal)+offset) % 360, ((360* val)+offset) % 360, false, paint);
    }

    private void drawText(Canvas canvas) {


        String[] lines = getDisplayText().split("\n");
        int l2 = lines.length/2;
        float textHeight = 21f;
        float offset = oy +(lines.length==1 ? textHeight/2 : 0);

        for(String line : lines){
            canvas.drawText(line, ox, offset, textPaint);
            offset += textHeight;
        }
    }

    private String getDisplayText() {

        if(isAdjustable){
            float val = targetValue - 0.25f;

            return (Math.round(val * 100.0))+" %";
        }
        if(getTimeSinceStateChange() < 500){
            if(state){
                return "ON";
            }
            else{
                return "OFF";
            }
        }

        return name;
    }

    private long getTimeSinceStateChange() {
        return System.currentTimeMillis() - stateChangeTimestamp;
    }

    @Override
	public void update(long tpf) {

        if(Math.abs(targetValue - value) > (1/12)){
//            float dv = value - targetValue;
//            value += dv;
            value += (targetValue-value) * 0.01 * tpf;
        }

		super.update(tpf);
        updateParentLine(getParent());


	}

    @Override
    protected void onStartMove() {



    }

    public boolean containsPosition(float x, float y, float margin) {
		
		float distance = (float) Math.sqrt(Math.pow(x - ox, 2)
				+ Math.pow(y - oy, 2));
		
		return (distance < radius);
	}

	@Override
	public void makeDeltaForce(RoomNode n2, long tps) {
		super.makeDeltaForce(n2, tps);
		
		float r = (float) Math.sqrt(Math.pow(getParent().ox - ox, 2)
				+ Math.pow(getParent().oy - oy, 2));

		float target = 200;

		float cx = (getParent().ox - ox);
		float cy = (getParent().oy - oy);

		socialfx += (cx * (r - target)) * 0.3f;
		socialfy += (cy * (r - target)) * 0.3f;
	}
	protected float calculateSocialWeight(RoomNode n2, float distance) {
		return 0f;
	}

    @Override
    public void setPlacement(float[] po) {

    }

    public void onClick() {
		state = !state;
		
		getParent().graphNodeEvent.setEvent(GraphNodeEvent.CLICK);
		getParent().graphNodeListener.onEvent(getParent().graphNodeEvent, this);
		
	}

	public void updateParentLine(ChildEnabledGraphNode roomNode) {
		parentLine.update(this, roomNode);
	}

    @Override
    public String toString() {

        return "DeviceNode[" + name + "]";
    }


    public void updateAlpha(long tpf, boolean in) {

        int max = 255;
        int delta = (int)(tpf * 0.600f);

        if(in && alpha < max){
            alpha += delta;
            if(alpha > max) alpha = max;
        }
        else {
            int min = 0;
            if(alpha > min){
                alpha -= delta;
                if(alpha < min) alpha = min;
            }
        }
        setAlpha(alpha);

    }

    public void setAlpha(int a) {
        paint.setAlpha(a);
        edgePaint.setAlpha(a);
        textPaint.setAlpha(a);
    }

    public boolean handleInteraction(float x, float y, int finger, boolean fingerInUse, DeviceNode movingChild) {

        this.movingChild = movingChild;
        if(!getParent().isMoving){

            saveCursor(x,y);
            if (!isPressed) {
                if (containsPosition(x, y, (float) GraphNodeEvent.MOVE_ON_CLICK_THRESHOLD)){
                    if(!fingerInUse) {
                        fingerId = finger;
                        isPressed = true;
                        handleTouch(true);
                    }
                    else{
                        if(movingChild != null && movingChild.isMoving()){
                            isHoveredOverWithOtherNode = true;

                        }
//                        if(hoverChild != null && hoverChild.isMoving){

//                        }
//                        else{
//                            isHoveredOverWithOtherNode = false;
//                        }
                    }
                }
                else{
                    isHoveredOverWithOtherNode = false;
                }
            } else if(isMoving && fingerId == finger){
                moveTo(x, y);
            }

        }
        return isPressed;
    }


    @Override
    public void onEvent(GraphNodeEvent graphNodeEvent, GraphNode graphNode) {


        if (graphNodeEvent.getEvent() == GraphNodeEvent.CLICK) {
            Log.d(TAG, this+" CLICK!");
            if(type == TYPE_BOOLEAN || type == TYPE_ANALOG){
                toggleSwitch();
                performHapticFeedback(10);

            }

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.UP) {
            Log.d(TAG, this+" UP!");
            isAdjustable = false;
//            if(!moveDisabled){
                highlightOtherChildren(this, 255);
//            }

        }else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE) {
            Log.d(TAG, this+" MOVE!");
//            highlightOtherChildren(this, 100);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_START) {
            Log.d(TAG, this+" MOVE_START!");
//            highlightOtherChildren(this, 100);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_UP) {
            highlightOtherChildren(this, 255);

            Log.d(TAG, this+" MOVE UP!");

            moveDisabled = true;
            if(hoverChild != null){
                if(collidesWith(hoverChild)) {
                    Log.d(TAG, "Dropped " + this + " in " + hoverChild);

                    graphNodeEvent.setEvent(GraphNodeEvent.DROPPED);
                    graphNodeListener.onEvent(graphNodeEvent, hoverChild);
                }
                hoverChild = null;
            }
            else{
                Log.d(TAG, "hoverChild is null!");
            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.LONG_PRESS) {
//            Log.d(TAG, this+" LONG PRESS!");
//            debugPosition = !debugPosition;

            if(!isAdjustable) {
                enableDrag();
            }

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.DROPPED) {
            Log.d(TAG, this+" DROPPED!");
            highlightOtherChildren(this, 255);

            addConnection(graphNode);


            ActivityEvent activityEvent = new ActivityEvent(ActivityEvent.CONNECTION_ADDED);
            getParent().sendActivityEvent(activityEvent);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVED_OUT_OF_NODE) {
            Log.d(TAG, this + " MOVED OUT!");

            if(type == TYPE_BOOLEAN){
                toggleSwitch();
                performHapticFeedback(10);
            }

            if(type == TYPE_BOOLEAN || type == TYPE_ANALOG) {
                getParent().disableInteraction(true);
            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVING_OUTSIDE_OF_NODE) {

            if (type == TYPE_ANALOG) {
                state = true;
                float cx = ox - cursorX;
                float cy = oy - cursorY;


                if (isAdjustable || getDistanceTo(cursorX, cursorY) > getRadius() * 2) {
                    if (!isAdjustable) {
                        performHapticFeedback(10);
                    }
                    isAdjustable = true;

                    double angle = Math.atan(cy / (cx == 0 ? 0.00001f : cx));
                    angle += Math.PI / 2;

                    if (cx > 0) {
                        angle += Math.PI;

                        if (cy > 0) {
                            //angle-= Math.PI;
                        }
                    }

                    angle = angle + (Math.PI / 2 % (Math.PI * 2));
                    setValueFromAngle(angle);
                }
            }
//            else if(type == TYPE_BOOLEAN){
//                float cx = ox - cursorX;
//                float cy = oy - cursorY;
//
//                if(cy - cx > 0){
//                    state = (cx > 0);
//                }
//                else{
//                    state = (cy < 0);
//                }
//
//
//
//            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_UP_FROM_OUTSIDE_OF_NODE) {
            Log.d(TAG, this + " MOVED OUT FROM OUTSIDE NODE!");
            getParent().disableInteraction(false);
            isAdjustable = false;
            moveDisabled = true;
        }

    }

    private void enableDrag() {
        performHapticFeedback(10);
        moveDisabled = false;
        highlightOtherChildren(this, 100);
        dragTime = System.currentTimeMillis();
    }

    private void toggleSwitch() {
        state = !state;
        stateChangeTimestamp = System.currentTimeMillis();

        if(type == TYPE_BOOLEAN) {
            setValueFromAngle(Math.PI / 2 + (state ? Math.PI * 2-0.001f : 0));
        }
        else if(type == TYPE_ANALOG){
            if(state){
                setValue(savedState);
            }
            else{
                savedState = getValue();
                setValueFromAngle(Math.PI / 2);
            }
        }
    }

    private float setValueFromAngle(double angle) {
        targetValue = (float) (angle / (Math.PI*2));
        return targetValue;
    }

    private void setValue(float val) {
        targetValue = val;
    }

    private float getValue() {
        return targetValue;
    }

    public void handleNoInteraction(DeviceNode hoverChild) {

        if(isPressed){
            isPressed = false;
            fingerId = -1;
            this.hoverChild = hoverChild;
            handleTouch(false);

        }
        isHoveredOverWithOtherNode = false;

    }

    public int getFingerId() {
        return fingerId;
    }

    public boolean isHoveredOver() {
        return isHoveredOverWithOtherNode;
    }


    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected boolean isMoveDisabled() {
        return moveDisabled;
    }

    public void highlight(boolean hightlighted) {
        isHighlighted = hightlighted;
    }
}
