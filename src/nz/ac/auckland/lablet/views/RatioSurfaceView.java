/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import android.view.SurfaceView;


/**
 * A SurfaceView that has a fixed size ratio.
 */
public class RatioSurfaceView extends SurfaceView {
    private float ratio = 4.f/3;

    public RatioSurfaceView(Context context) {
        super(context);
    }

    public RatioSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        if ((float)(specWidth) / specHeight < ratio) {
            // smaller ratio than the video / full width
            width = specWidth;
            height = (int)((float)width / ratio);
        } else {
            height = specHeight;
            width = (int)((float)height * ratio);
        }

        if (specWidthMode == MeasureSpec.AT_MOST || specHeightMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

            getLayoutParams().width = width;
            getLayoutParams().height = height;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        setMeasuredDimension(width, height);
    }
}
