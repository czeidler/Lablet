/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class PlotView extends ViewGroup {
    private IYAxis yAxisView;
    private PlotPainterContainerView mainView;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        yAxisView = new YAxisView(context, attrs);
        addView((ViewGroup)yAxisView);

        this.mainView = new PlotPainterContainerView(context);
        addView(mainView);
    }

    public void addPlotPainter(IPlotPainter painter) {
        mainView.addPlotPainter(painter);
    }

    public void setRangeY(float bottom, float top) {
        yAxisView.setDataRange(bottom, top);
        mainView.setRangeY(bottom, top);
    }

    public void setRangeX(float left, float right) {
        mainView.setRangeX(left, right);
    }

    public IYAxis getYAxisView() {
        return yAxisView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        int yAxisRight = (int)yAxisView.optimalWidthForHeight(height);
        Rect yAxisRect = new Rect(0, 0, yAxisRight, height);
        Rect mainViewRect = new Rect(yAxisRight, (int)yAxisView.getAxisTopOffset(), width,
                height - (int)yAxisView.getAxisBottomOffset());

        ((ViewGroup)yAxisView).measure(MeasureSpec.makeMeasureSpec(yAxisRect.width(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(yAxisRect.height(), MeasureSpec.EXACTLY));
        ((ViewGroup)yAxisView).layout(yAxisRect.left, yAxisRect.top, yAxisRect.right, yAxisRect.bottom);

        if (mainView != null) {
            mainView.measure(MeasureSpec.makeMeasureSpec(mainViewRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mainViewRect.height(), MeasureSpec.EXACTLY));
            mainView.layout(mainViewRect.left, mainViewRect.top, mainViewRect.right, mainViewRect.bottom);
        }
    }
}
