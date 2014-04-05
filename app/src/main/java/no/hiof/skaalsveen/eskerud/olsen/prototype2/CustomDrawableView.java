package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.DeviceNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.GraphNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphChangeListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphNodeListener;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ServerEventListener;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class CustomDrawableView extends CustomSurfaceView implements
        GraphNodeListener, GraphChangeListener {

	protected static final String TAG = "CustomDrawableView";
	private GameThread thread = null;
	ArrayList<RoomNode> roomNodes = new ArrayList<RoomNode>();
	private int rooms = 10;
	private int fingersActive = 0;
	private int[] fingerMap, lastMask;
	public int bgColor = Color.WHITE;
	private int colorFrames = 0;
	private boolean gestureMode = false;
	private double startGestureCenterDist;
	private int gestureTicks;
	private boolean gestureMeasureMode = false;
	private Context context;
	private String[] labels;
	private ServerEventListener serverEventListener;
	private boolean childPress = false;
    private boolean graphChanged = true;


    public CustomDrawableView(Context context) {
		super(context);
		init(context);
	}

	public CustomDrawableView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

    @Override
    protected void createSlotManager(int width, int height) {

        slotManager = new SlotManager(width, height);
        slotManager.setOnGraphChangeListener(this);
    }

    public void init(Context context) {
		this.setHapticFeedbackEnabled(true);
		getHolder().addCallback(this);
		this.context = context;

		Resources res = getResources();
		labels = res.getStringArray(R.array.room_nodes);

		rooms = labels.length;
		fingerMap = new int[rooms];
		lastMask = new int[rooms];

	}

	@Override
	public void startGame() {

		if (thread == null) {
			thread = new GameThread(this);
			thread.startThread();
		}
	}

	public void setupNodes() {

		// Font path
		String fontPath = "fonts/Oswald-Regular.ttf";

		// Loading Font Face
		Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
		Paint textPaint = new Paint();
		textPaint.setTypeface(tf);

		int i = 0;
		while (roomNodes.size() < rooms) {

			RoomNode rn = new RoomNode(labels[i], textPaint);
			rn.setGraphNodeListener(this);
			roomNodes.add(rn);
			fingerMap[i] = i;
			i++;
		}
		
		
	}

	@Override
	public void stopGame() {
		if (thread != null) {
			thread.stopThread();

			// Waiting for the thread to die by calling thread.join,
			// repeatedly if necessary
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
			thread = null;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int[] mask = new int[rooms];
        //ArrayList<GraphNode> map2 = new ArrayList<GraphNode>();

        int roomsInMotion = 0;

		if (event.getAction() == MotionEvent.ACTION_UP) {
			fingersActive = 0;
			gestureMode = false;
			gestureMeasureMode = false;
			gestureTicks = 0;
			childPress = false;
		} else {

			fingersActive = event.getPointerCount();
			int gestureFingers = 0;
			float[] fingerPositionsX = new float[fingersActive];
			float[] fingerPositionsY = new float[fingersActive];
			float gesturePositionsSumX = 0;
			float gesturePositionsSumY = 0;


			for (int i = 0; i < rooms; i++) {

				if (i < fingersActive && i < rooms) {
					int pi = event.getPointerId(i);

					if (lastMask.length > pi && lastMask[pi] == 0) {
						swapFingerNodes(pi, (int) event.getX(i),
								(int) event.getY(i));
					}

					RoomNode node = getFingerNode(pi);

					if (node != null) { // for when there is more fingers than
										// nodes

						fingerPositionsX[i] = event.getX(i);
						fingerPositionsY[i] = event.getY(i);

						int fingersInside = node.getFingersInsideRadius(
								fingerPositionsX, fingerPositionsY);

                        boolean b1 = !gestureMode && fingersInside < 2;
						if (b1 && node.trySet(event.getX(i), event.getY(i))) {
						    //click, move, press
							node.setActive(true);
							mask[pi] = 1;
                            roomsInMotion++;
						} else if(b1 && node.manageChildren(event.getX(i), event.getY(i), pi)){
                            mask[pi] = 1;
                        }
                        else {

							gesturePositionsSumX += fingerPositionsX[i];
							gesturePositionsSumY += fingerPositionsY[i];
							gestureFingers++;

//                            if (node.childrenVisible && node.children.size() > 0) {
//                                for (DeviceNode child : node.children) {
//                                    if(child.handleInteraction(fingerPositionsX[i], fingerPositionsY[i], i)){
//                                        map2.add(child);
//                                    }
//                                }
//                            }

						}
					}
				}
			}

			if (gestureFingers == fingersActive && fingersActive > 0) {
				// bgColor = Color.GRAY;

				if (gestureFingers > 1) { // zoom-gesture

					boolean firstGestureData = (!gestureMeasureMode);
					gestureMode = true;
					gestureMeasureMode = true;

					float centerX = gesturePositionsSumX / fingersActive;
					float centerY = gesturePositionsSumY / fingersActive;

					double fingerSumCenterDist = 0;
					for (int gpi = 0; gpi < fingersActive; gpi++) {

						float deltaX = centerX - fingerPositionsX[gpi];
						float deltaY = centerY - fingerPositionsY[gpi];

						double fingerCenterDist = Math.sqrt(Math.pow(deltaX, 2)
								+ Math.pow(deltaY, 2));
						fingerSumCenterDist += fingerCenterDist;
					}

					if (firstGestureData) {
						startGestureCenterDist = (fingerSumCenterDist / fingersActive);
					} else {
						double fingerAvgCenterDist = (fingerSumCenterDist / fingersActive);
						double deltaGestureCenterDist = fingerAvgCenterDist
								- startGestureCenterDist;

//						if (deltaGestureCenterDist > GraphNodeEvent.GESTURE_MOVE_TRESHOLD) { // zoom
//																								// out
//							sendZoomEvent(GraphNodeEvent.ZOOM_OUT, centerX,
//									centerY,
//									fingerPositionsX[fingersActive - 1],
//									fingerPositionsY[fingersActive - 1],
//									gestureTicks++, fingerAvgCenterDist);
//							// bgColor = Color.RED;
//							gestureMeasureMode = false;
//						} else if (-deltaGestureCenterDist > GraphNodeEvent.GESTURE_MOVE_TRESHOLD) { // zoom
//																										// in
//							sendZoomEvent(GraphNodeEvent.ZOOM_IN, centerX,
//									centerY,
//									fingerPositionsX[fingersActive - 1],
//									fingerPositionsY[fingersActive - 1],
//									gestureTicks++, fingerAvgCenterDist);
//							// bgColor = Color.GREEN;
//							gestureMeasureMode = false;
//						}
					}
				}
			}

		}

		lastMask = mask;

		for (int i = 0; i < mask.length; i++) {
			if (mask[i] != 1) {
				RoomNode node = getFingerNode(i);
				node.setActive(false);
                if(node.hasGhost()){
                    node.setGhost(!(fingersActive == 0));
                }

                node.handleChildrenNotActive();
			}
		}

//        for(RoomNode roomNode : roomNodes){
//            for(DeviceNode child : roomNode.getChildren()){
//                if(!map2.contains(child)){
//                    child.handleNoInteraction();
//                }
//            }
//        }
//        map2.clear();


		return true;
	}

	private void sendZoomEvent(int zoomState, float centerX, float centerY,
			float currentCenterX, float currentCenterY, int gestureTicks,
			double fingerAvgCenterDist) {

		int minIdx = getNearestNodeIdx(centerX, centerY);

		RoomNode node = getFingerNode(minIdx);
		node.setRadius((float) fingerAvgCenterDist);
		node.forceSet(centerX, centerY);
	}

	/**
	 * Helper-method for selecting the nearest node when finger touches screen.
	 * */
	public void swapFingerNodes(int i, int x, int y) {

		int minIdx = getNearestNodeIdx(x, y);

		int tmp = fingerMap[i];
		fingerMap[i] = fingerMap[minIdx];
		fingerMap[minIdx] = tmp;

		// Log.d(TAG, "FINGER MAP: "+Arrays.toString(fingerMap));
	}

	public int getNearestNodeIdx(float x, float y) {
		double minValue = getFingerNode(0).getDistanceTo(x, y);
		double value;
		int minIdx = 0;

		for (int u = 1; u < rooms; u++) {
			value = getFingerNode(u).getDistanceTo(x, y);
			if (value < minValue) {
				minIdx = u;
				minValue = value;
			}
		}
		return minIdx;
	}

	public RoomNode getFingerNode(int i) {
		if (fingerMap.length > i && roomNodes.size() > i) {
			return roomNodes.get(fingerMap[i]);
		}
		return null;
	}

	public void draw(Canvas canvas) {
		canvas.drawColor(bgColor);

		if (bgColor != Color.WHITE) {

			if (colorFrames++ > 5) {
				bgColor = Color.WHITE;
				colorFrames = 0;
			}
		}

		
		for (int i = 0; i < rooms; i++)
			roomNodes.get(i).draw(canvas);

	}

	public void update(long tpf) {

		if (physics != null) physics.update(tpf);
        if (slotManager != null) slotManager.update(tpf);

		RoomNode node, n2;
		for (int i = 0; i < rooms; i++) {
			node = roomNodes.get(i);
			node.prepDelta();
			node.applyCenterScreenForce(width, height);

			for (int y = 0; y < rooms; y++) {
				n2 = roomNodes.get(y);
				if (!node.equals(n2)) {
					node.makeDeltaForce(n2, tpf);
				}
			}

            if (slotManager != null && !slotManager.contains(node) && graphChanged) {

                Log.d(TAG, "Updating "+node+ " GRAPH CHANGED!");

                float x = node.getX();
                float y = node.getY();

                float h2 = height / 2;
                float h5 = height / 5;

                int rUp = node.getRequestUp();
                if(rUp == 0) {
                    if (y < h2) {
                        moveUp(node);
                    } else {
                        moveDown(node);
                    }
                }
                else if (rUp > 0) {
                    moveUp(node);
                } else {
                    moveDown(node);
                }

            }

			node.update(tpf);

		}

        if (graphChanged) {
            graphChanged = false;
        }
	}

    private void moveDown(RoomNode node) {
        node.moveY(height - node.getMinimizedRadius());
    }

    private void moveUp(RoomNode node) {
        node.moveY(node.getMinimizedRadius());
    }

    @Override
	public void onEvent(GraphNodeEvent graphNodeEvent, GraphNode node) {

		if (graphNodeEvent.getEvent() == GraphNodeEvent.CLICK) {
			// bgColor = Color.BLACK;

			if (node instanceof RoomNode) {

                RoomNode roomNode = ((RoomNode) node);
                //roomNode.toggleMinimized();
                slotManager.add(roomNode);

			} else if (node instanceof DeviceNode) {
				serverEventListener
						.sendMessage((((DeviceNode) node).state ? "on" : "off"));
			}
		} else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE) {
			// bgColor = Color.GREEN;

		} else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_START) {
            // bgColor = Color.GREEN;
            slotManager.remove((RoomNode) node, null);

        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.MOVE_UP) {
            // bgColor = Color.GREEN;
            float h3 = height / 5;

            float y1 = node.getY();
            RoomNode n2 = (RoomNode) node;
            if(y1 > h3 && h3*4 > y1){
                slotManager.add(n2);
            }
            else{
                //n2.place(roomNodes, width);
                graphChanged = true;
            }


        } else if (graphNodeEvent.getEvent() == GraphNodeEvent.LONG_PRESS) {
			// bgColor = Color.RED;
			this.performHapticFeedback(10);

		}

	}

	public void setServerEventListener(ServerEventListener listener) {
		this.serverEventListener = listener;
	}

	@Override
	public ArrayList<RoomNode> getRoomNodes() {
		return roomNodes;
	}

    @Override
    public void onGraphChange(RoomNode node, boolean removed, RoomNode newNode) {

        if(removed) {

            if(newNode != null){
                node.requestUp((newNode.getY() < node.getY() ? -1 : 1));
            }
            else{
                node.requestUp(0);
            }

            node.place(roomNodes, width);
        }
        graphChanged = true;
    }

}