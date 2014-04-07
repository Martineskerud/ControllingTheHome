package no.hiof.skaalsveen.eskerud.olsen.prototype2;

/**
 * Created by root on 07.04.14.
 */
public class ActivityEvent {

    public static final int START_DIALOG = 1;
    public static final int CONNECTION_ADDED = 2;
    public static final int PERFORM_HAPTIC_FEEDBACK = 3;
    private final int type;
    private int value;

    public ActivityEvent(int type){
        this.type = type;
    }

    public void addValue(int value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
