/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;
import android.graphics.PointF;


public class CrossRenderer implements IPointRenderer {
    @Override
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config) {
        float size = config.getMarkerSize() / 2;
        canvas.drawLine(position.x - size, position.y - size, position.x + size, position.y + size,
                config.getMarkerPaint());
        canvas.drawLine(position.x - size, position.y + size, position.x + size, position.y - size,
                config.getMarkerPaint());
    }
}
