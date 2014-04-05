package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

/**
 * Created by root on 04.04.14.
 */
public abstract class ChildNode extends GraphNode{
    private RoomNode parent;

    public ChildNode(String name, RoomNode parent) {
        super(name);
        this.parent = parent;
    }

    public RoomNode getParent() {
        return parent;
    }

}
