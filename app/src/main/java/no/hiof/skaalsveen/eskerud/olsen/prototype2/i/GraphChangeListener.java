package no.hiof.skaalsveen.eskerud.olsen.prototype2.i;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.SlotManager;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;

/**
 * Created by root on 02.04.14.
 */
public interface GraphChangeListener {

    public void onGraphChange(RoomNode newNode, boolean removed, RoomNode node, SlotManager roomNodes);

}
