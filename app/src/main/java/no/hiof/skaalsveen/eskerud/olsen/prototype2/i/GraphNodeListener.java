package no.hiof.skaalsveen.eskerud.olsen.prototype2.i;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;

public interface GraphNodeListener {

	public void onEvent(GraphNodeEvent graphNodeEvent, GraphNode graphNode);
	
}
