package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

public class Edge<T extends GraphNode, U extends GraphNode> {

	public static final String TAG = "Edge";
    protected String label;
    protected float startX;
    protected float startY;
    protected float stopX;
    protected float stopY;
    protected Paint paint;

	public Edge(Paint p) {
        paint = p;
	}

    public Edge(Paint paint, String label) {
        this.paint = paint;
        this.label = label;
    }
	
	public void update(T nodeA, U nodeB){

		startX = nodeA.ox;
		stopX = nodeB.ox;
		startY = nodeA.oy;
		stopY = nodeB.oy;

        changeEndPoints(nodeA, nodeB);
		
	}

    protected void changeEndPoints(T nodeA, U nodeB) {

        float dx = stopX-startX;
        float dy = stopY-startY;

        double len = Math.sqrt(Math.pow((stopX-startX), 2) + Math.pow((stopY-startY), 2));
        double startR = nodeA.getRadius()/len;
        double stopR = nodeB.getRadius()/len;

        startX += dx*startR;
        startY += dy*startR;

        stopX -= dx*stopR;
        stopY -= dy*stopR;
    }

    public void draw(Canvas canvas){

//        if(true){
//            paint.setPathEffect(new DashPathEffect(new float[]{2, 4}, 0));
//        }

		canvas.drawLine(startX, startY, stopX, stopY, paint);

        if(label != null) {

            Path path = new Path();
            path.moveTo(stopX, stopY);
            path.lineTo(startX, startY);

            canvas.drawTextOnPath(label, path, 0, 0, paint);
        }
		
	}
	
}
