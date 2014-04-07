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
    private boolean crossRooms;
    private boolean isVisible;

    public BezierCurve(Paint paint) {
        super(new Paint(paint));
        setupPaint();
    }

    public void setupPaint(){
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3.0f);
        paint.setAntiAlias(true);
        paint.setARGB(255, 200, 200, 200);
    }

    @Override
    public void update(T nodeA, U nodeB) {

        ChildEnabledGraphNode parentB = nodeB.getParent();
        ChildEnabledGraphNode parentA = nodeA.getParent();


        if(parentA != null && parentB != null){

            if(parentA.childrenVisible && parentB.childrenVisible){
                isVisible = true;
            }
            else{
                isVisible = false;
                parentA.setRoomConnection(parentB, nodeA, nodeB);
            }

            if(parentA.equals(parentB)){ // same parent!

                crossRooms = false;
                parentX = parentA.getX();
                parentY = parentA.getY();
                radius = parentA.getChildRadius()*1.2f;

            }
            else{

                crossRooms = true;
                parentX = (parentA.getX()+parentB.getX())/2;
                parentY = (parentA.getY()+parentB.getY())/2;

                float childDX = (nodeA.getX()+nodeB.getX())/2;
                float dx = (parentX-childDX);

                radius = Math.abs(parentA.getX()-parentX)+dx;

            }

        }
        else{
            isVisible = false;
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

        startX += dx1*startR*1.2f;
        startY += dy1*startR*1.2f;

        stopX -= dx2*stopR*1.2f;
        stopY -= dy2*stopR*1.2f;

    }

    @Override
    public void draw(Canvas canvas) {

        if(!isVisible){
            return;
        }
        final Path path = new Path();
        path.moveTo(stopX, stopY);
        path.quadTo(middleX, middleY, startX, startY);


//        Paint paint = new Paint() {
//            {
//                setStyle(Style.STROKE);
//                setStrokeCap(Cap.ROUND);
//                setStrokeWidth(3.0f);
//                setAntiAlias(true);
//                setARGB(255,200,200,200);
//            }
//        };

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
