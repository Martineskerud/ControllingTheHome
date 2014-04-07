package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

/**
 * Created by root on 04.04.14.
 */
public abstract class ChildNode extends ChildEnabledGraphNode{

    private ChildEnabledGraphNode parent;

    public ChildNode(String name, ChildEnabledGraphNode parent) {
        super(name, parent);
        this.parent = parent;
    }

    public ChildEnabledGraphNode getParent() {
        return parent;
    }

}
