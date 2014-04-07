package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.HashMap;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.ActivityEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.HapticDevice;


public abstract class ChildEnabledGraphNode extends GraphNode {

	public boolean childrenVisible = false;
	public ArrayList<DeviceNode> children = new ArrayList<DeviceNode>();

    protected HashMap<String, Integer> typeHashMap;
    protected boolean interactionDisabled;

    public ChildEnabledGraphNode(String name, HapticDevice hapticDevice) {
        super(name, hapticDevice);

        typeHashMap = new HashMap<String, Integer>();
        typeHashMap.put("Light",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Floor heating",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Fireplace",DeviceNode.TYPE_ANALOG);
        typeHashMap.put("Stove",DeviceNode.TYPE_GROUPED);
        typeHashMap.put("Oven",DeviceNode.TYPE_GROUPED);
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
            children.add(new DeviceNode(dev, this, textPaint, getChildType(dev)));
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

    public boolean sendActivityEvent(ActivityEvent activityEvent) {
        return sendActivityEvent(activityEvent);
    }

    public void setRoomConnection(ChildEnabledGraphNode parentB, ChildNode nodeA, ChildNode nodeB) {
        addConnection(parentB);
    }

}
