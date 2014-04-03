package no.hiof.skaalsveen.eskerud.olsen.prototype2;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.components.RoomNode;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.Controller;
import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.GraphChangeListener;

/**
 * Created by root on 01.04.14.
 */
public class SlotManager extends ArrayList<RoomNode> implements Controller{


    private static final String TAG = "SlotManager";
    private final float width;
    private final float height;
    private float[][] pos = new float[2][2];
    private boolean changed = true;
    private GraphChangeListener onGraphChangeListener;
    private boolean shotgun = false;
    private int seatTaken = 0;

    public SlotManager(float screenW, float screenH){
        super();

        width = screenW;
        height = screenH;

        pos[0] = new float[]{width/4, height/2};
        pos[1] = new float[]{width/4*3, height/2};
    }

    @Override
    public boolean add(RoomNode newNode) {
        if(contains(newNode)) return false;

        Log.d(TAG, "Adding " + newNode);

        while (size() >= pos.length){
            RoomNode outNode = getNearestNode(newNode);
            //outNode.setPlacement(new float[]{width / 2, height-(outNode.getRadius()/2)});

            Log.d(TAG, "Removing "+outNode+ " from slot");
            remove(outNode, newNode);
        }
        changed = true;
        onGraphChangeListener.onGraphChange(newNode, false, newNode);
        newNode.setChildrenVisible(true);

        if(size() == 1 && newNode.getX() < width){

            RoomNode oldNode = super.get(0);
            super.remove(0);
            super.add(newNode);
            super.add(oldNode);
            return true;

        }
        else{
            return super.add(newNode);
        }

    }

    private RoomNode getNearestNode(RoomNode node) {

        double d1 = node.getDistanceTo(get(0));
        double d2 = node.getDistanceTo(get(1));



        return (d1 < d2 ? get(0) : get(1));
    }

    public void remove(RoomNode node, RoomNode newNode){
        if (contains(node)){
            node.setChildrenVisible(false);
            super.remove(node);
            changed = true;
            onGraphChangeListener.onGraphChange(node, true, newNode);
        }
    }

    @Override
    public void update(float tpf) {

        if(changed) {
            int i = 0;
            shotgun = false;

            for (RoomNode node : this) {
                node.setPlacement(getPlacement(i, node));
                i++;
            }
            changed = false;
        }

    }

    private float[] getPlacement(int i, RoomNode node) {

        if(size() == 1 ){
            return new float[]{width/2, height/2};
        }
        else if(!shotgun) {

            double d1 = Math.abs(node.getX() - pos[0][0]);
            double d2 = Math.abs(node.getX() - pos[1][0]);


            if (d1 < d2) {
                seatTaken = 0;
            }
            else{
                seatTaken = 1;
            }

            shotgun = true;
            return pos[seatTaken];
        }
        else{
            int idx = Math.abs(seatTaken - 1);
            return pos[idx];
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // do nothing
    }

    public void setOnGraphChangeListener(GraphChangeListener onGraphChangeListener) {
        this.onGraphChangeListener = onGraphChangeListener;
    }

}
