/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;


/**
 * Background for the info side bar.
 */
public class InfoBarBackgroundDrawable extends Drawable {
    private Paint paint = new Paint();;
    private Paint linePaint = new Paint();

    public InfoBarBackgroundDrawable(int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(2);
        linePaint.setColor(android.graphics.Color.argb(255, 200, 200, 200));
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = new Rect(getBounds());

        // right border
        canvas.drawLine(rect.right, 0, rect.right, rect.bottom, linePaint);

        // golden ratio
        rect.bottom = rect.top + (int)((float)(rect.height()) * 0.618);
        rect.right = rect.left + (int)((float)(rect.width()) * 0.5);

        canvas.drawRect(rect, paint);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
