package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.util.Log;

public class DeviceNode extends ChildNode implements GraphNodeListener {

	public static final String TAG = "DeviceNode";
    private final Paint edgePaint;
	private Paint paint;
	public float radius;
	private Paint textPaint;
	public boolean state = false;
	private Edge parentLine;
    private int alpha;
    private boolean isPressed = false;
    private int fingerId = -1;
    private boolean isHoveredOverWithOtherNode = false;
    private DeviceNode hoverChild;
    private boolean moveDisabled = true;
    private DeviceNode movingChild;
    private float value = 0f;
    private boolean isAdjustable;
    private float targetValue = 0;

    public DeviceNode(String name, RoomNode parent, Paint textPaint) {
        super(name, parent);

		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.5f);
		
		this.textPaint = textPaint;
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.CENTER);

        edgePaint = new Paint();
        edgePaint.setAntiAlias(true);
        edgePaint.setColor(Color.GRAY);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2.5f);

		radius = 50;
		useTheForce = true;
		
		parentLine = new Edge(edgePaint);
        setGraphNodeListener(this);
	}


    @Override
    public float getRadius() {
        return super.getRadius()+ (isHoveredOverWithOtherNode ? 10 : 0);
    }

    @Override
	public void draw(Canvas canvas) {

        paint.setColor((isPressed ? (isMoving ? Color.GREEN : Color.RED) : (isHoveredOverWithOtherNode ? Color.YELLOW : Color.BLUE)));
		paint.setStyle((state ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
		canvas.drawCircle(ox, oy, getRadius() , paint);

        if(!isHoveredOverWithOtherNode){
            drawText(canvas);
        }

		parentLine.draw(canvas);

        if(value > 0){
            drawValue(canvas);
        }
        super.draw(canvas);
	}

    private void drawValue(Canvas canvas) {
        final RectF oval = new RectF();
        RoomNode p = getParent();

        float r = radius* 1.1f;
        oval.set(getX()-r, getY()-r, getX()+ r, getY()+ r);
        canvas.drawArc(oval, -90, (360*value)-90, false, paint);
    }

    private void drawText(Canvas canvas) {
        String[] lines = name.split("\n");
        int l2 = lines.length/2;
        float textHeight = 21f;
        float offset = oy +(lines.length==1 ? textHeight/2 : 0);

        for(String line : lines){
            canvas.drawText(line, ox, offset, textPaint);
            offset += textHeight;
        }
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

	public void updateParentLine(RoomNode roomNode) {
		parentLine.update(this, roomNode);
	}

    @Override
    public String toString() {

        return "DeviceNode[" + name+ "]";
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
                if (containsPosition(x, y, 10)){
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
        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE) {
            Log.d(TAG, this+" MOVE!");
            highlightOtherChildren(this, 100);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_START) {
            Log.d(TAG, this+" MOVE_START!");
            highlightOtherChildren(this, 100);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_UP) {

            Log.d(TAG, this+" MOVE UP!");

            moveDisabled = true;
            if(hoverChild != null){
                if(collidesWith(hoverChild)) {
                    Log.d(TAG, "Dropped " + this + " in " + hoverChild);

                    graphNodeEvent.setEvent(GraphNodeEvent.DROPPED);
                    graphNodeListener.onEvent(graphNodeEvent, hoverChild);
                }
            }
            else{
                Log.d(TAG, "hoverChild is null!");
            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.LONG_PRESS) {

            if(!isAdjustable) {
                performHapticFeedback(10);
                //Log.d(TAG, this+" LONG PRESS!");
//            debugPosition = !debugPosition;
                moveDisabled = false;
            }

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.DROPPED) {
            Log.d(TAG, this+" DROPPED!");
            highlightOtherChildren(this, 255);

            graphNode.addConnection(this);
        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVED_OUT_OF_NODE) {
            Log.d(TAG, this+" MOVED OUT!");
            getParent().disableInteraction(true);


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVING_OUTSIDE_OF_NODE) {


            float cx = ox-cursorX;
            float cy = oy-cursorY;



            if(isAdjustable || getDistanceTo(cursorX, cursorY) > getRadius() * 2){
                if(!isAdjustable){
                    performHapticFeedback(10);
                }
                isAdjustable = true;

                double angle = Math.atan(cy / (cx == 0 ? 0.00001f : cx));
                angle+= Math.PI/2;

                if(cx > 0){
                    angle+= Math.PI;

                    if(cy > 0){
                        //angle-= Math.PI;
                    }
                }

                angle = angle+(Math.PI/2 % (Math.PI * 2));
                setValue(angle);
            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_UP_FROM_OUTSIDE_OF_NODE) {
            Log.d(TAG, this+" MOVED OUT FROM OUTSIDE NODE!");
            getParent().disableInteraction(false);
            isAdjustable = false;
            moveDisabled = true;
        }
    }

    private void setValue(double angle) {
        targetValue = (float) (angle / (Math.PI*2));
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
}
