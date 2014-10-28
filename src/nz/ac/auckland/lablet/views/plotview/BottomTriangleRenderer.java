/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;


public class BottomTriangleRenderer implements IPointRenderer {
    @Override
    public void drawPoint(Canvas canvas, PointF position, DrawConfig config) {
        float size = config.getMarkerSize();
        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(position.x, position.y - size / 2);
        path.lineTo(position.x + size / 2, position.y + size / 2);
        path.lineTo(position.x - size / 2, position.y + size / 2);
        path.lineTo(position.x, position.y - size / 2);
        path.close();
        canvas.drawPath(path, config.getMarkerPaint());
    }
}
