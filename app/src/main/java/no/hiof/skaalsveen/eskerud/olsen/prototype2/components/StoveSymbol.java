package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by root on 08.04.14.
 */
public class StoveSymbol {


    private final SymbolEnabledDeviceNode node;
    private final Paint symbolPaint;
    private float[][] coordinates = null;
    private float r = 0;

    public StoveSymbol(SymbolEnabledDeviceNode node, Paint symbolPaint) {
        this.node = node;
        this.symbolPaint = symbolPaint;
    }

    public void draw(Canvas canvas) {

        int i = 1;
        int n = Integer.parseInt(node.name);

        if(coordinates != null) {
            for (float[] co : coordinates) {
                if (i == n) {
                    symbolPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                } else {
                    symbolPaint.setStyle(Paint.Style.STROKE);
                }

                canvas.drawCircle(co[0], co[1], r/1.5f, symbolPaint);
                i++;
            }
        }

    }

    public void update(long tpf) {

        float x = node.getX();
        float y = node.getY();
        r = node.getRadius() * 0.3f;

        coordinates = new float[][]{
                new float[]{x-r,  y-r},
                new float[]{x+r,  y-r},
                new float[]{x-r,  y+r},
                new float[]{x+r,  y+r},
        };

    }
}
