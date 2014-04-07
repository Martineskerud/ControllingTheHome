package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ActivityEventListener;

/**
 * Created by root on 04.04.14.
 */
public abstract class ChildNode extends ChildEnabledGraphNode{

    private ChildEnabledGraphNode parent;

    public ChildNode(String name, ActivityEventListener activityEventListener, ChildEnabledGraphNode parent) {
        super(name, activityEventListener);
        this.parent = parent;
    }

    public ChildEnabledGraphNode getParent() {
        return parent;
    }

    @Override
    protected void manageCirclePlacement() {
        placeInCircle(getX(), getY(), getChildRadius(), getChildren(), Math.PI, (float) (-Math.PI / 2));
    }

    @Override
    public float getRadius() {

        return (float) (super.getRadius() * (getParent() instanceof DeviceNode ? 0.5 : 1));
    }

    @Override
    public void highlightOtherChildren(GraphNode node, int alpha) {

        getParent().highlightOtherChildren(node, alpha);
    }
}
