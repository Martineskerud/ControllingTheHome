package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import android.util.Log;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.DeviceNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;

/**
 * Created by root on 05.04.14.
 */
public class ChildAwareSlotManager extends SlotManager {

    private static final String TAG = "ChildAwareSlotManager";
    private ArrayList<DeviceNode> children;
    private int[] childrenTouchMap;
    private int activeChildren2 = 0;
    private DeviceNode hoverChild;
    private DeviceNode movingChild = null;
    private boolean debug = false;

    public ChildAwareSlotManager(float screenW, float screenH) {
        super(screenW, screenH);

        children = new ArrayList<DeviceNode>();
        childrenTouchMap = new int[0];
    }

    @Override
    public boolean add(RoomNode newNode) {
        boolean res = super.add(newNode);
        updateChildArray();
        return res;
    }

    @Override
    public boolean remove(RoomNode node, RoomNode newNode) {
        boolean res = super.remove(node, newNode);
        updateChildArray();
        return res;
    }

    private void updateChildArray() {

        children.clear();
        for(RoomNode node : this){
            if(node.childrenVisible) {

                ArrayList<DeviceNode> children = node.getChildren();
                if(children != null) {
                    for (DeviceNode child : children) {
                        this.children.add(child);
                        if(child.children != null && child.children.size() > 0){
                            for (DeviceNode subChild : child.children) {
                                this.children.add(subChild);
                                subChild.setDebuggable(debug);
                            }
                        }
                        child.setDebuggable(debug);
                    }
                }
            }
            node.setDebuggable(debug);
        }
        childrenTouchMap = new int[children.size()];

    }

    public boolean manageTouch(RoomNode fingerNode, float x, float y, int finger) {
        int activeChildren = 0;


        if (children != null && children.size() > 0) {

            boolean fingerInUse = false;
            for (DeviceNode child : children) {
                if (child.getFingerId() == finger) {
                    fingerInUse = true;
                    break;
                }
            }

            int i = 0;
            for (DeviceNode child : children) {

                //child.setName(i+"");
//                if (childrenTouchMap[i] == 0){

                    boolean hi = child.handleInteraction(x, y, finger, fingerInUse, movingChild);

                    if(hi) {
                        activeChildren++;
                        childrenTouchMap[i] = finger+1;
                        movingChild = child;
                    }
                    else{
                        childrenTouchMap[i] = 0;
                    }

//                } else {
//                    childrenTouchMap[i] = 0;
//                }
                i++;
            }

            return (activeChildren > 0);
        }
        return false;
    }

    private void handleChildrenNotActive() {
        if (children != null && children.size() > 0) {

            int i = 0;
            try {
                for (DeviceNode child : children) {
                    if (child.isHoveredOver()) {
                        hoverChild = child;
                    }
                    i++;
                }

                for (DeviceNode child : children) {
                    child.handleNoInteraction(hoverChild);
                }
            } catch (ConcurrentModificationException e){
                Log.e(TAG, "Concurrent fuckup!");
            }
        }
    }

    public void onUp() {
        handleChildrenNotActive();
    }

    public void highlightOtherChildren(GraphNode node, int a) {
        for(RoomNode n : this){
            n.setAlpha(a);
        }
        ArrayList<DeviceNode> ch = getAllChildren();
        if(ch != null && ch.size() > 0){
            for(DeviceNode child : ch){
                child.highlight((a > 125 ? false : true));
            }
        }
    }

    public void toggleDebugging() {
        debug = !debug;
    }
}
