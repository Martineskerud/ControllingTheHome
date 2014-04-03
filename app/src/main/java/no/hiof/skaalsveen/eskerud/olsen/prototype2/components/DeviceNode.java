package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.CustomSurfaceView;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.GraphNodeEvent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.Log;

import java.util.ArrayList;

public class DeviceNode extends GraphNode {



	public static final String TAG = "DeviceNode";
	private String name;
	private Paint paint;
	public float radius;
	private RoomNode parent;
	private Paint textPaint;
	public boolean state = false;
	private Edge parentLine;
    private int alpha;

    public DeviceNode(String name, RoomNode parent, Paint textPaint) {
		this.name = name;
		this.parent = parent;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.GREEN);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.5f);
		
		this.textPaint = textPaint;
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.CENTER);

        Paint edgePaint = new Paint();
        edgePaint.setAntiAlias(true);
        edgePaint.setColor(Color.GRAY);
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(2.5f);

		radius = 50;
		useTheForce = true;
		
		parentLine = new Edge(edgePaint);
	}
	
	@Override
	public void draw(Canvas canvas) {

		paint.setStyle((state ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE));
		canvas.drawCircle(ox, oy, radius, paint);

        String[] lines = name.split("\n");
        int l2 = lines.length/2;
        float textHeight = 21f;
        float offset = oy +(lines.length==1 ? textHeight/2 : 0);

        for(String line : lines){
            canvas.drawText(line, ox, offset, textPaint);
            offset += textHeight;
        }

		parentLine.draw(canvas);
	}
	
	@Override
	public void update(long tpf) {
		
		//ox = parent.ox + 100;
		//oy = parent.oy + 100;

        ArrayList<DeviceNode> children = parent.getChildren();

        CustomSurfaceView.placeInCircle(parent.getX(), parent.getY(), radius * 2, children);

		super.update(tpf);
	}

	public boolean isInside(float x, float y) {
		
		float distance = (float) Math.sqrt(Math.pow(x - ox, 2)
				+ Math.pow(y - oy, 2));
		
		return (distance < radius);
	}

	@Override
	public void makeDeltaForce(RoomNode n2, long tps) {
		super.makeDeltaForce(n2, tps);
		
		float r = (float) Math.sqrt(Math.pow(parent.ox - ox, 2)
				+ Math.pow(parent.oy - oy, 2));

		float target = 200;

		float cx = (parent.ox - ox);
		float cy = (parent.oy - oy);

		socialfx += (cx * (r - target)) * 0.3f;
		socialfy += (cy * (r - target)) * 0.3f;
	}
	protected float calculateSocialWeight(RoomNode n2, float distance) {
		return 0f;
	}

    @Override
    public void setPlacement(float[] po) {

    }

    public void onClick() {
		state = !state;
		
		parent.graphNodeEvent.setEvent(GraphNodeEvent.CLICK);
		parent.graphNodeListener.onEvent(parent.graphNodeEvent, this);
		
	}

	public void updateParentLine(RoomNode roomNode) {
		parentLine.update(this, roomNode);
	}

    @Override
    public String toString() {

        return "DeviceNode[" + name+ "]";
    }


    public void updateAlpha(long tpf, boolean in) {

        int max = 255;
        int delta = (int)(tpf * 0.600f);

        if(in && alpha < max){
            alpha += delta;
            if(alpha > max) alpha = max;
        }
        else {
            int min = 0;
            if(alpha > min){
                alpha -= delta;
                if(alpha < min) alpha = min;
            }
        }
        setAlpha(alpha);

    }

    private void setAlpha(int a) {
        paint.setAlpha(a);
        textPaint.setAlpha(a);
    }
}
