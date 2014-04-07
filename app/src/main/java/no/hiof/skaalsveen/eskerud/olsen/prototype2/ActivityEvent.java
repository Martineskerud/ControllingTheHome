package no.hiof.skaalsveen.eskerud.olsen.prototype2;

/**
 * Created by root on 07.04.14.
 */
public class ActivityEvent {

    public static final int START_DIALOG = 1;
    public static final int CONNECTION_ADDED = 2;
    private final int type;

    public ActivityEvent(int type){
        this.type = type;
    }

}
