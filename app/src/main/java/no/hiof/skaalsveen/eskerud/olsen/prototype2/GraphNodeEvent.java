package no.hiof.skaalsveen.eskerud.olsen.prototype2;

public class GraphNodeEvent {

	public static final long LONG_PRESS_THRESHOLD = 400;
	public static final long LONG_PRESS_OVERKILL = 10000;
	public static final double MOVE_ON_CLICK_THRESHOLD = 20;
	
	public static final int CLICK = 1;
	public static final int LONG_PRESS = 2;
	public static final int MOVE = 3;
	public static final double GESTURE_MOVE_TRESHOLD = 1;
	public static final int ZOOM_OUT = 4;
	public static final int ZOOM_IN = 5;
	public static final long CLICK_THRESHOLD = 50;
    public static final int MOVE_START = 6;
    public static final int MOVE_UP = 7;
    public static final int DROPPED = 8;
    public static final int MOVED_OUT_OF_NODE = 9;
    public static final int MOVING_OUTSIDE_OF_NODE = 10;
    public static final int MOVE_UP_FROM_OUTSIDE_OF_NODE = 11;
    public static final int UP = 12;

    private int action;

	public GraphNodeEvent() {
		
	}

	public void setEvent(int event) {
		action = event;
		
	}
	public int getEvent(){
		return action;
	}
	
}
