package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphChangeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class CustomSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback {

	protected int height;
	protected int width;
	protected Physics physics;
	private boolean initialized = false;
    protected SlotManager slotManager;

	private static final String TAG = "CustomSurfaceView";

	public CustomSurfaceView(Context context) {
		super(context);
        init();
	}

	public CustomSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
        init();
	}

	public CustomSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
	}

    private void init() {
        physics = new Physics();

    }

    @Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed");
		stopGame();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

		setupNodes();
		startGame();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		this.height = height;
		this.width = width;

		Log.d(TAG, "Surface: "+ width+ "x" + height);

        ArrayList<RoomNode> roomNodes = getRoomNodes();
        GraphNode.placeInCircle((width / 2), (height / 2), height / 4, roomNodes);

        if(roomNodes != null && roomNodes.size() > 0) {

            for (RoomNode node : roomNodes) {
                if (!initialized) {
                    physics.add(node);
                }
            }

            createSlotManager(width, height);

            initialized = true;
        }
	}

    protected abstract void createSlotManager(int width, int height);

    public abstract void setupNodes();

	public abstract void startGame();

	public abstract void stopGame();

	public abstract ArrayList<RoomNode> getRoomNodes();
}