package no.hiof.skaalsveen.eskerud.olsen.prototype2.components;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import no.hiof.skaalsveen.eskerud.olsen.prototype2.i.ActivityEventListener;

/**
 * Created by root on 08.04.14.
 */
public class SymbolEnabledDeviceNode extends DeviceNode {

    private StoveSymbol symbol;
    private boolean symbolVisible = true;


    public SymbolEnabledDeviceNode(String name, ChildEnabledGraphNode parent, ActivityEventListener activityEventListener, Paint textPaint, int childType) {
        super(name, parent, activityEventListener, textPaint, childType);

        Paint symbolPaint = new Paint(edgePaint);
        symbolPaint.setColor(Color.BLACK);
        symbolPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        symbolPaint.setStrokeWidth(2.5f);

        if(name.equals("1") || name.equals("2") || name.equals("3") || name.equals("4")){
            symbol = new StoveSymbol(this, symbolPaint);
        }


    }

    @Override
    protected String getDisplayText() {

        String n = super.getDisplayText();
        if(name != null && name.equals(n)){
            symbolVisible = true;
            return null;
        }
        else {
            symbolVisible = false;
            return n;
        }


    }

    @Override
    public void update(long tpf) {
        super.update(tpf);
        if(symbolVisible){
            symbol.update(tpf);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(symbolVisible) {
            symbol.draw(canvas);
        }

    }
}


