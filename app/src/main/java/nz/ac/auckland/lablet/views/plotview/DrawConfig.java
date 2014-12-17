/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import nz.ac.auckland.lablet.misc.DeviceIndependentPixel;


public class DrawConfig {
    private Context context;
    private Paint markerPaint = new Paint();
    private float markerSize;
    private Paint linePaint = new Paint();

    public DrawConfig(Context context) {
        this.context = context;

        markerPaint.setColor(Color.GREEN);
        markerPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.YELLOW);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        setMarkerSizeDP(6f);
    }

    public Paint getMarkerPaint() {
        return markerPaint;
    }

    public void setMarkerPaint(Paint markerPaint) {
        this.markerPaint = markerPaint;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    public float getMarkerSize() {
        return markerSize;
    }

    public void setMarkerSizeDP(float markerSizeDP) {
        this.markerSize = DeviceIndependentPixel.toPixel(markerSizeDP, context);
    }
}
