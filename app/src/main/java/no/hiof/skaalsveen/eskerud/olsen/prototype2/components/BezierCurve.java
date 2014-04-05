package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by root on 04.04.14.
 */
public class BezierCurve<T extends ChildNode, U extends ChildNode> extends Edge<T, U> {

    public static final float STROKE_WITH = 2.5f;
    private float parentY;
    private float parentX;
    private float radius;
    private float middleY;
    private float middleX;

    public BezierCurve(Paint paint) {
        super(paint);
    }

    @Override
    public void update(T nodeA, U nodeB) {

        RoomNode parentB = nodeB.getParent();
        RoomNode parentA = nodeA.getParent();

        if(parentA != null && parentB != null){

            if(parentA.equals(parentB)){ // same parent!

                parentX = parentA.getX();
                parentY = parentA.getY();
                radius = parentA.getChildRadius()*1.2f;

            }
            else{

                parentX = (parentA.getX()+parentB.getX())/2;
                parentY = (parentA.getY()+parentB.getY())/2;
                radius = 10;

            }

        }

        float mx = (stopX + startX) / 2;
        float my = (stopY + startY) / 2;
        double angleP = getAngle(parentX, parentY, mx, my);

        middleX = (float) (parentX + (radius * Math.cos(angleP)));
        middleY = (float) (parentY + (radius * Math.sin(angleP)));

        super.update(nodeA, nodeB);
    }

    @Override
    protected void changeEndPoints(T nodeA, U nodeB) {

        float dx1 = middleX - startX;
        float dy1 = middleY - startY;
        double len1 = Math.sqrt(Math.pow(dx1, 2) + Math.pow(dy1, 2));

        float dx2 = stopX - middleX;
        float dy2 = stopY - middleY;
        double len2 = Math.sqrt(Math.pow(dx2, 2) + Math.pow(dy2, 2));

        double startR = (nodeA.getRadius()+STROKE_WITH)/len1;
        double stopR = (nodeB.getRadius()+STROKE_WITH)/len2;

        startX += dx1*startR*1.1f;
        startY += dy1*startR*1.1f;

        stopX -= dx2*stopR;
        stopY -= dy2*stopR;

    }

    @Override
    public void draw(Canvas canvas) {


        final Path path = new Path();
        path.moveTo(startX, startY);
        path.quadTo(middleX, middleY, stopX, stopY);


        Paint paint = new Paint() {
            {
                setStyle(Style.STROKE);
                setStrokeCap(Cap.ROUND);
                setStrokeWidth(3.0f);
                setAntiAlias(true);
                setARGB(255,200,200,200);
            }
        };
        canvas.drawPath(path, paint);

        paint.setTextSize(20);
        canvas.drawTextOnPath("<", path, 0, 7, paint);
    }

    private double getAngle(float px, float py, float x, float y) {

        float dx = px - x;
        float dy = py - y;

        if(dx == 0){
            return (dy>0 ? Math.PI/2*3 : Math.PI/2);
        }

        return Math.atan(dy/dx)+(dx>0 ? Math.PI : 0);

    }
}
