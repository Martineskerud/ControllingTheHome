package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

public class Edge {

	public static final String TAG = "Edge";
	private float startX;
	private float startY;
	private float stopX;
	private float stopY;
	private Paint paint;

	public Edge(Paint paint) {
		this.paint = paint;
	}
	
	public void update(GraphNode nodeA, GraphNode nodeB){

		startX = nodeA.ox;
		stopX = nodeB.ox;
		startY = nodeA.oy;
		stopY = nodeB.oy;
				
		float dx = stopX-startX;
		float dy = stopY-startY;
		
		double len = Math.sqrt(Math.pow((stopX-startX), 2) + Math.pow((stopY-startY), 2));
		double startR = nodeA.radius/len;
		double stopR = nodeB.radius/len;
			
		startX += dx*startR;
		startY += dy*startR;
		
		stopX -= dx*stopR;
		stopY -= dy*stopR;
		
	}
	
	public void draw(Canvas canvas){

        if(true){
            paint.setPathEffect(new DashPathEffect(new float[]{2, 4}, 0));
        }

		canvas.drawLine(startX, startY, stopX, stopY, paint);
		
	}
	
}
