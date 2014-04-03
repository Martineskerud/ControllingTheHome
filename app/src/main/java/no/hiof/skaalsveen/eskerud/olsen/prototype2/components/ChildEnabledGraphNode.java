package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import java.util.ArrayList;


public abstract class ChildEnabledGraphNode extends GraphNode {

	public boolean childrenVisible = false;
	public ArrayList<DeviceNode> children = new ArrayList<DeviceNode>();

	public void makeDeltaForce(RoomNode n2, long tps) {
		super.makeDeltaForce(n2, tps);
		
		
		if (childrenVisible && children.size() > 0) {
			for (DeviceNode child : children) {
                child.makeDeltaForce(n2, tps);
			}
		}
		
	}
	
}
