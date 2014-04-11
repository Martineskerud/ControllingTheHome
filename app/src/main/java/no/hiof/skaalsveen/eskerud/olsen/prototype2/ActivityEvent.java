package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;

/**
 * Created by root on 07.04.14.
 */
public class ActivityEvent<T> {

    public static final int START_DIALOG = 1;
    public static final int CONNECTION_ADDED = 2;
    public static final int PERFORM_HAPTIC_FEEDBACK = 3;
    public static final int REPORT_INTERACTION = 4;

    private final int type;
    private T value;

    public ActivityEvent(int type){
        this.type = type;
    }

    public void addValue(T value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

}
