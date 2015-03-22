/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;


public class RatioGLSurfaceView extends GLSurfaceView {
    private float ratio = 4.f/3;

    public RatioGLSurfaceView(Context context) {
        super(context);

        init();
    }

    public RatioGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        setPreserveEGLContextOnPause(true);
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int specWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = View.MeasureSpec.getSize(heightMeasureSpec);

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

        if (specWidthMode == View.MeasureSpec.AT_MOST || specHeightMode == View.MeasureSpec.AT_MOST) {
            widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);

            getLayoutParams().width = width;
            getLayoutParams().height = height;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        setMeasuredDimension(width, height);
    }
}
