package no.hiof.skaalsveen.eskerud.olsen.prototype2.i;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;

/**
 * Created by root on 06.04.14.
 */
public interface HapticDevice {

    boolean performHapticFeedback(int time);

    void highlightOtherChildren(GraphNode node, int alpha);
}
