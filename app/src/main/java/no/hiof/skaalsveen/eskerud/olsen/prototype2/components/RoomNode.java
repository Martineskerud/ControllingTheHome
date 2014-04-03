package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.CustomSurfaceView;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

import java.util.ArrayList;

public class RoomNode extends ChildEnabledGraphNode {

	public static final String TAG = "GraphNode";
    private final Paint ghostPaint;
    private Paint paint;
	public float fx, fy;
	private Paint textPaint;
	private String name;
	private double distance;

	private float extra_margin = 20;

	private float screenCenterFx;
	private float screenCenterFy;
    private float screenCenterX;
    private float screenCenterY;
    private long childrenVisibleTime;
    private int minimizedRadius = 50;
    private int requestUp;
    private float startX, startY;
    private boolean ghost;
    private float ghostX, ghostY;

    public RoomNode(String name, Paint textPaint) {
		super();

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
        ghostPaint.setPathEffect(new DashPathEffect(new float[]{2, 4}, 0));

		this.textPaint = textPaint;
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.CENTER);

		int textSize = 42;
		int margin = 4;
		do {

			textPaint.setTextSize(textSize--);

		} while (textPaint.measureText(name) > (radius - margin) * 2);

		this.name = name;
		
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
	}

	@Override
	public void draw(Canvas canvas) {


		canvas.drawCircle(ox, oy, radius, (ghost ? ghostPaint : paint));

        float tx, ty;

        if(ghost){
            canvas.drawCircle(ghostX, ghostY, radius, paint);
            tx = ghostX;
            ty = ghostY;
        }
        else{
            tx = ox;
            ty = oy;
        }

        textPaint.setColor((minimized ? Color.BLUE : Color.RED));


		canvas.drawText(name, tx, ty + 10.5f, textPaint);

		if (childrenVisible && children.size() > 0) {
			for (DeviceNode child : children) {
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


        radius = (childrenVisible ? 75 : minimizedRadius);

		if (!isActive) {
			
//			oy += (fy * 0.1) * tpf;
//			ox += (screenCenterFx * 0.000001) * tpf;
//
//			if(!minimized){
//				ox += (fx * 0.1) * tpf;
//				oy += (screenCenterFy * 0.000001) * tpf;
//			}
//
//			fx = 0;
//			fy = 0;
//
//			screenCenterFx = 0;
//			screenCenterFy = 0;

		}
		// follow fingers smooth
		else if (isMoving) {

			float dx = x - ox;
			float dy = y - oy;

			ox += (dx / 20) * tpf;
			oy += (dy / 20) * tpf;
		}

		paint.setColor(isActive ? Color.RED : Color.BLUE);
        childrenVisibleTime+=tpf;

		if (children != null && children.size() > 0) {


            if(childrenVisibleTime < 1000 || childrenVisible) {

                for (DeviceNode child : children) {

                    if (childrenVisibleTime < 1000) {
                        child.updateAlpha(tpf, childrenVisible);
                    }

                    child.update(tpf);
                    child.updateParentLine(this);
                }
            }

//            if(childrenVisible && getRadius() < 75){
//                radius += (radius * 0.005f * tpf);
//                if(radius > 75){
//                    radius = 75;
//                }
//            }
//            else if(!childrenVisible && getRadius() > 50){
//                radius -= (radius * 0.005f * tpf);
//                if(radius < 50){
//                    radius = 50;
//                }
//            }
		}

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

    public double getDistanceTo(float x2, float y2) {
		distance = Math.sqrt(Math.pow(ox - x2, 2) + Math.pow(oy - y2, 2));
		return distance;
	}

	

	public void prepDelta() {
		useTheForce = true;
		socialfx = 0;
		socialfy = 0;
	}

	public void setGraphNodeListener(GraphNodeListener graphNodeListener) {
		this.graphNodeListener = graphNodeListener;
	}

	public boolean trySet(float x, float y, CustomSurfaceView customDrawableView) {

		float distance = (float) Math.sqrt(Math.pow(x - ox, 2)
				+ Math.pow(y - oy, 2));
		
		
		//	Log.d(TAG, "distance = "+ distance+ "    ("+x+","+y+")("+ox+""+oy+")");
		
		if (distance < radius + extra_margin || isMovable) {

            if(!isMovable){
                startX = x;
                startY = y;
            }
            isMovable = true;

			//forceSet(x, y);
            if(!childrenVisible){

                moveTo(x,y);
                return true;
            }
            else{

                float distance2 = (float) Math.sqrt(Math.pow(x - startX, 2)
                        + Math.pow(y - startY, 2));

                if(distance2 > radius + extra_margin){
                    ghost = false;
                    moveTo(x,y);
                    return true;
                }
                else{
                    ghost = true;
                    ghostX = x;
                    ghostY = y;
                }
            }

		}

		return false;
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

//        if(screenCenterX == 0 && screenCenterY == 0){
//            screenCenterX = width/2;
//            screenCenterY = (minimized ? height-radius*2 : height/2);
//        }
//
//		float distance = getDistance(screenCenterX,ox,screenCenterY,oy);
//
//		float cx = (screenCenterX - ox) * (screenCenterX - distance);
//		float cy = (screenCenterY - oy) * (screenCenterY - distance);
//
//		screenCenterFx = cx;
//		screenCenterFy = cy;
	}



    public void applyMinimizeForce(int height) {
		
		float distance = (height - oy);
		
		if(distance > radius){
			screenCenterFy += 100;
		}
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

//        for(int u = 0; u < 10; u++){
//
//            int i=0;
//            int minimizedRadius1 = getMinimizedRadius();
//            for(RoomNode node : roomNodes){
//
//                if(!node.equals(this)) {
//                    float rr = minimizedRadius1 + node.getMinimizedRadius();
//                    float dist = getDistance(x1, node.getX());
//                    if (dist < rr) {
//                        i++;
//                    }
//                }
//            }
//            if(i < 1) {
//                Log.d(TAG, this +" found spot! ");
//                moveX(x1);
//                return;
//            }
//
//            float w2 = (width / 2);
//            float wr = w2 - minimizedRadius1;
//            x1 = (float) (Math.random() * wr);
//
//
//            if(w2 < getX()){
//                x1 += w2 - minimizedRadius1;
//            }
//            else{
//                x1 += minimizedRadius1;
//            }
//
//        }
//
//        Log.w(TAG, "Did not find spot... using old x.");
//        moveX(getX());

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


    public boolean isActive() {
        return isActive;
    }

    public double getDistanceTo(RoomNode roomNode) {
        return getDistanceTo(roomNode.getX(), roomNode.getY());
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
    }

    public boolean hasGhost() {
        return ghost;
    }
}
