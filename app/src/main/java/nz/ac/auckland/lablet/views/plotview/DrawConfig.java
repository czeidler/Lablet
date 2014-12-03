/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Color;
import android.graphics.Paint;


public class DrawConfig {
    private Paint markerPaint = new Paint();
    private float markerSize = 6f;
    private Paint linePaint = new Paint();

    public DrawConfig() {
        markerPaint.setColor(Color.GREEN);
        markerPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.YELLOW);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
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

    public void setMarkerSize(float markerSize) {
        this.markerSize = markerSize;
    }
}
