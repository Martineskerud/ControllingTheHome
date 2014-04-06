package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.HapticDevice;

public class RoomNode extends ChildEnabledGraphNode implements HapticDevice {

	public static final String TAG = "GraphNode";
    private static final int LOW_ALPHA = 100;
    private static final int HIGH_ALPHA = 255;
    public static final int MAX_RADIUS = 75;
    private static final double MOVE_FOCUSED_NODE_THRESHOLD = MAX_RADIUS;
    private final Paint ghostPaint;
    private int[] childrenTouchMap;
    private Paint paint;
	public float fx, fy;
	private Paint textPaint;

	private float extra_margin = 20;

    private long childrenVisibleTime;
    private int minimizedRadius = 50;
    private int requestUp;
    private float startX, startY;
    private boolean ghost;
    private float ghostX, ghostY;
    private int alpha = 255;
    private int alternativeRadius = MAX_RADIUS;
    private int activeChildren2;
    private DeviceNode hoverChild;
    private int d1 = 0;
    private boolean interactionDisabled;

    public RoomNode(String name, Paint textPaint, HapticDevice hapticDevice) {
		super(name, hapticDevice);

		this.ox = 0;
		this.oy = 0;

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(4.5f);

        ghostPaint = new Paint();
        ghostPaint.setAntiAlias(true);
        ghostPaint.setColor(Color.GRAY);
        ghostPaint.setStyle(Paint.Style.STROKE);
        ghostPaint.setStrokeWidth(4.5f);
        //ghostPaint.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
        ghostPaint.setAlpha(LOW_ALPHA);

		this.textPaint = textPaint;
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.CENTER);

		int textSize = 42;
		int margin = 4;
		do {

			textPaint.setTextSize(textSize--);

		} while (textPaint.measureText(name) > (radius - margin) * 2);
		
		if(name.equals("Living room")){
            String[] devices = new String[]{"Light", "Fireplace", "TV"};
            for(String dev : devices){
                children.add(new DeviceNode(dev, this, textPaint));
            }
		}
        else if(name.equals("Kitchen")){
            String[] devices = new String[]{"Stove", "Oven", "Light", "Radio", "Dishwasher"};
            for(String dev : devices){
                children.add(new DeviceNode(dev, this, textPaint));
            }
        }
        else if(name.equals("Hallway")){
            String[] devices = new String[]{"Floor\nheating", "Light"};
            for(String dev : devices){
                children.add(new DeviceNode(dev, this, textPaint));
            }
        }
        else if(name.equals("WC")){
            String[] devices = new String[]{"Radio", "Light"};
            for(String dev : devices){
                children.add(new DeviceNode(dev, this, textPaint));
            }
        }
        else if(name.equals("Bedroom")){
            String[] devices = new String[]{"Alarm\nClock", "Light"};
            for(String dev : devices){
                children.add(new DeviceNode(dev, this, textPaint));
            }
        }

        childrenTouchMap = new int[children.size()];
	}

    @Override
    protected boolean outsideThreshold(double deltaPosition) {
        if(childrenVisible){
//            ghost = true;
            alpha = 0;
        }
        return super.outsideThreshold(deltaPosition);
    }

    @Override
	public void draw(Canvas canvas) {

//      ghostPaint.setAlpha(0);
//		canvas.drawCircle(ox, oy, radius, (ghost ? ghostPaint : paint));

        float tx, ty;

        if(ghost){
            canvas.drawCircle(ghostX, ghostY, radius, paint);
            tx = ghostX;
            ty = ghostY;
        }
        else{
            canvas.drawCircle(ox, oy, radius, paint);
            tx = ox;
            ty = oy;
        }

        textPaint.setColor((minimized ? Color.BLUE : Color.RED));

		canvas.drawText(name, tx, ty + 10.5f, textPaint);

		if (childrenVisible && children.size() > 0) {
			for (DeviceNode child : children) {
                child.setAlpha(alpha);
				child.draw(canvas);
			}
		}
	}

	/**
	 * While coordinates are different from actual position, approach position.
	 * 
	 * */
	@Override
	public void update(long tpf) {
		super.update(tpf);

        radius = (childrenVisible ? alternativeRadius : minimizedRadius);

		paint.setColor(isActive ? Color.RED : Color.BLUE);
        childrenVisibleTime+=tpf;

		if (children != null && children.size() > 0) {


            if(childrenVisibleTime < 1000 || childrenVisible) {

                clearConnections();
                for (DeviceNode child : children) {

                    if (childrenVisibleTime < 1000) {
                        child.updateAlpha(tpf, childrenVisible);
                    }

                    child.update(tpf);
                    child.updateParentLine(this);
                }
            }

            placeInCircle(getX(), getY(), getChildRadius(), getChildren());
		}

        activeChildren2 = 0;
        for (DeviceNode child : children) {
            activeChildren2 += (child.isMoving ? 1 : 0);
        }

	}



    public float getChildRadius() {
        return radius * 1.5f;
    }

    @Override
    protected void onStartMove() {
        resetAlpha();
        //forceSet(ghostX, ghostY);
    }

    @Override
    public void setPlacement(float[] po) {
        if(po != null && po.length > 1) {

//            float dist = getDistance(po[0], ox, po[1], oy);
//            if(dist > getRadius()){
//                ox += (ox - po[0]) * 0.001f;
//                oy += (oy - po[1]) * 0.001f;
//            }

            moveTo(po[0], po[1]);
        }
    }



	

	public void prepDelta() {
		useTheForce = true;
		socialfx = 0;
		socialfy = 0;
	}



	public boolean trySet(float x, float y) {

        d1 = 0;
        if(activeChildren2 > 0 || interactionDisabled){
            return false;

        }

		float distance = (float) Math.sqrt(Math.pow(x - ox, 2)
				+ Math.pow(y - oy, 2));
		

		//	Log.d(TAG, "distance = "+ distance+ "    ("+x+","+y+")("+ox+""+oy+")");
        saveCursor(x,y);
        double threshold = MOVE_FOCUSED_NODE_THRESHOLD;//getRadius() + extra_margin;
        if (distance < threshold || isMovable) {

            if(isMovable){

                ghost = false;
                alpha = HIGH_ALPHA;
//                childrenVisible = false;
                moveTo(x,y);
            }
            else if(childrenVisible){
                ghostX = x;
                ghostY = y;
                ghost = true;

                float distance2 = (float) Math.sqrt(Math.pow(x - pressedPositionX, 2)
                        + Math.pow(y - pressedPositionY, 2));
                alpha = (int)((255/ threshold) * (threshold - distance2));

                //alternativeRadius = minimizedRadius + (int) ((MAX_RADIUS-minimizedRadius)/threshold * (threshold - distance2));


            }

            //
            return true;
		}


		return false;
	}




    @Override
    protected void onFingerUp() {

        alternativeRadius = MAX_RADIUS;
        resetAlpha();
    }

    private void resetAlpha() {
        alpha = 255;
    }

    public void forceSet(float x, float y) { // used for moving nodes by hand & zooming
        setX(x);
        setY(y);
	}

	public int getFingersInsideRadius(float[] fingerPositionsX,
			float[] fingerPositionsY) {

		int fingers = 0;
		for (int i = 0; i < fingerPositionsX.length; i++) {
			if (Math.sqrt(Math.pow(fingerPositionsX[i] - ox, 2)
					+ Math.pow(fingerPositionsY[i] - oy, 2)) < radius + 20) {
				fingers++;
			}
		}

		return fingers;
	}

	public void setChildrenVisible(boolean visible) {
		childrenVisible = visible;
        childrenVisibleTime = 0;
		
		if(childrenVisible && children.size() > 0){
			float step = (float) (2 * Math.PI / children.size());
			float a = 0;
			
			for(DeviceNode child : children){
				
				child.ox = (float) (ox + (radius+child.radius) * Math.sin(a));
				child.oy = (float) (oy + (radius+child.radius) * Math.cos(a));
				a += step;
			}
		}
	}

	public void setRadius(float r) {
		radius = r;
	}

	public void applyCenterScreenForce(int width, int height) {

	}


    public void toggleMinimized() {
        minimized = !minimized;
    }

    @Override
    public String toString() {

        return "DeviceNode[" + name+ "]";
    }

    public ArrayList<DeviceNode> getChildren() {
        return children;
    }

    public void place(ArrayList<RoomNode> roomNodes, float width) {

        Log.d(TAG, this + " I will now try to place myself... ");

        float x1 = getX();

        while(isVerticallyInsideNode(roomNodes, x1)){
            float w2 = (width / 2);
            float wr = w2 - getMinimizedRadius();
            x1 = (float) (Math.random() * wr);
            x1 = subtractEdges(x1, w2);
        }

        moveX(x1);

    }

    private float subtractEdges(float x1, float w2) {
        if(w2 < getX()){
            x1 += w2 - getMinimizedRadius();
        }
        else{
            x1 += getMinimizedRadius();
        }
        return x1;
    }

    private boolean isVerticallyInsideNode(ArrayList<RoomNode> roomNodes, float x) {

        for(RoomNode node : roomNodes){

            if(!node.equals(this)) {
                float rr = node.getMinimizedRadius()*2;
                float dist = getDistance(x, node.getX());
                if (dist < rr) {
                    return true;
                }
            }
        }

        return false;
    }


    private float getDistance(float a, float a1) {
        return Math.abs(a-a1);
    }



    public int getMinimizedRadius() {
        return minimizedRadius;
    }

    public void requestUp(int requestUp) {
        this.requestUp = requestUp;
    }

    public int getRequestUp() {
        return requestUp;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
        if(!ghost){
            resetAlpha();
        }
    }

    public boolean hasGhost() {
        return ghost;
    }

//    public void handleChildrenNotActive() {
//        if (childrenVisible && children.size() > 0) {
//
//            for(DeviceNode child : children){
//                if(child.isHoveredOver()){
//                    hoverChild = child;
//
//                }
//            }
//
//            for (DeviceNode child : children) {
//                child.handleNoInteraction(hoverChild);
//            }
//        }
//    }

//    public boolean manageChildren(float x, float y, int finger) {
//        int activeChildren = 0;
//        d1++;
//
//        if (childrenVisible && children.size() > 0) {
//
//            boolean fingerInUse = false;
//            for(DeviceNode child : children){
//                if (child.getFingerId() == finger){
//                    fingerInUse = true;
//                    break;
//                }
//            }
//
//            int i = 0;
//            for (DeviceNode child : children) {
//
//                if(childrenTouchMap[i] == 0 && child.handleInteraction(x, y, finger, fingerInUse, activeChildren2)){
//                    activeChildren++;
//                    childrenTouchMap[i] = finger;
//
//                } else if (childrenTouchMap[i] != 0){
//                    childrenTouchMap[i] = 0;
//                }
//                i++;
//            }
//
//            return (activeChildren > 0);
//        }
//
//        return false;
//    }

    public void setRoomConnection(RoomNode parentB, ChildNode nodeA, ChildNode nodeB) {
        addConnection(parentB);
    }


    public void disableInteraction(boolean movable) {
        interactionDisabled = movable;
    }


    public void setPaintAlpha(int a) {
        paint.setAlpha(a);
    }
}
