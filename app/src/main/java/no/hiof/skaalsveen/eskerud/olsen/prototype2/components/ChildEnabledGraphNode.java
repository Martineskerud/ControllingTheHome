package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.HashMap;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.ActivityEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ActivityEventListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.HapticDevice;


public abstract class ChildEnabledGraphNode extends GraphNode implements HapticDevice{

	public boolean childrenVisible = false;
	public ArrayList<DeviceNode> children = new ArrayList<DeviceNode>();

    protected HashMap<String, Integer> typeHashMap;
    protected boolean interactionDisabled;
    protected long childrenVisibleTime;

    protected Paint paint;
    protected int alpha = 255;

    @Override
    protected String getDebugInfo() {
        String debugInfo = super.getDebugInfo();

        if (childrenVisible && children.size() > 0) {
            int i = 0;
            for (DeviceNode child : children) {
                debugInfo+= "\n Child["+i+"]="+child+" @ ("+round(child.getX())+","+round(getY())+")";
                i++;
            }
        }

        return debugInfo;
    }

    public ChildEnabledGraphNode(String name, ActivityEventListener activityEventListener) {
        super(name, activityEventListener);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4.5f);

        typeHashMap = new HashMap<String, Integer>();
        typeHashMap.put("Light",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Floor heating",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Fireplace",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Stove",DeviceNode.TYPE_GROUPED);
        typeHashMap.put("Oven",DeviceNode.TYPE_GROUPED);

        typeHashMap.put("1",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("2",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("3",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("4",DeviceNode.TYPE_ANALOG);
    }

    @Override
    protected void draw(Canvas canvas) {

        if (childrenVisible && children.size() > 0) {
            for (DeviceNode child : children) {
                child.setAlpha(alpha);
                child.draw(canvas);
            }
        }

        super.draw(canvas);
    }

    @Override
    public void update(long tpf) {
        super.update(tpf);

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


            manageCirclePlacement();
        }

    }

    protected void manageCirclePlacement() {
        placeInCircle(getX(), getY(), getChildRadius(), getChildren());
    }

    protected void resetAlpha() {
        alpha = 255;
    }

    public ArrayList<DeviceNode> getChildren() {
        return children;
    }

    public void makeDeltaForce(RoomNode n2, long tps) {
		super.makeDeltaForce(n2, tps);
		
		
		if (childrenVisible && children.size() > 0) {
			for (DeviceNode child : children) {
                child.makeDeltaForce(n2, tps);
			}
		}
		
	}

    protected void createChildren(Paint textPaint, String[] devices) {
        for(String dev : devices){
            children.add(new DeviceNode(dev, this, activityEventListener, textPaint, getChildType(dev)));
        }
    }

    protected int getChildType(String dev) {

        if(typeHashMap.containsKey(dev)){
            return typeHashMap.get(dev);
        }
        return DeviceNode.TYPE_BOOLEAN;
    }

    public float getChildRadius() {
        return radius * 1.5f;
    }


    public void disableInteraction(boolean movable) {
        interactionDisabled = movable;
    }



    public void setRoomConnection(ChildEnabledGraphNode parentB, ChildNode nodeA, ChildNode nodeB) {
        addConnection(parentB);
    }


}
