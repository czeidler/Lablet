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
import android.view.View;
import android.view.ViewGroup;


public class PlotView extends ViewGroup {
    private IXAxis xAxisView;
    private IYAxis yAxisView;
    private PlotPainterContainerView mainView;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        xAxisView = new XAxisView(context);
        addView((ViewGroup)xAxisView);

        yAxisView = new YAxisView(context);
        addView((ViewGroup)yAxisView);

        this.mainView = new PlotPainterContainerView(context);
        addView(mainView);
    }

    public void addPlotPainter(IPlotPainter painter) {
        mainView.addPlotPainter(painter);
    }

    public void setRangeY(float bottom, float top) {
        if (hasYAxis())
            yAxisView.setDataRange(bottom, top);
        mainView.setRangeY(bottom, top);

        invalidate();
    }

    public void setRangeX(float left, float right) {
        if (hasXAxis())
            xAxisView.setDataRange(left, right);
        mainView.setRangeX(left, right);

        invalidate();
    }

    public void invalidate() {
        mainView.invalidate();
        if (xAxisView != null)
            ((View)xAxisView).invalidate();
        if (yAxisView != null)
            ((View)yAxisView).invalidate();
    }

    public IYAxis getYAxisView() {
        return yAxisView;
    }

    public IXAxis getXAxisView() {
        return xAxisView;
    }

    private boolean hasXAxis() {
        return xAxisView != null && ((ViewGroup)xAxisView).getVisibility() == View.VISIBLE;
    }

    private boolean hasYAxis() {
        return yAxisView != null && ((ViewGroup)yAxisView).getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = right - left;
        final int height = bottom - top;
        int xAxisTop = height;
        int xAxisLeftOffset = 0;
        int xAxisRightOffset = 0;
        if (hasXAxis()) {
            xAxisTop -= (int)xAxisView.optimalHeight();
            xAxisRightOffset = (int)xAxisView.getAxisRightOffset();
            xAxisLeftOffset = (int)xAxisView.getAxisLeftOffset();
        }
        final int mainAreaHeight = xAxisTop - (int)yAxisView.getAxisTopOffset();
        final int yAxisRight = (int)yAxisView.optimalWidthForHeight(mainAreaHeight);

        final Rect xAxisRect = new Rect(yAxisRight - xAxisLeftOffset, xAxisTop, width, height);
        final Rect yAxisRect = new Rect(0, 0, yAxisRight, xAxisTop + (int)yAxisView.getAxisBottomOffset());
        final Rect mainViewRect = new Rect(yAxisRight, (int)yAxisView.getAxisTopOffset(),
                width - xAxisRightOffset, xAxisTop);

        if (hasXAxis()) {
            ((ViewGroup) xAxisView).measure(MeasureSpec.makeMeasureSpec(xAxisRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(xAxisRect.height(), MeasureSpec.EXACTLY));
            ((ViewGroup) xAxisView).layout(xAxisRect.left, xAxisRect.top, xAxisRect.right, xAxisRect.bottom);
        }

        if (hasYAxis()) {
            ((ViewGroup) yAxisView).measure(MeasureSpec.makeMeasureSpec(yAxisRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(yAxisRect.height(), MeasureSpec.EXACTLY));
            ((ViewGroup) yAxisView).layout(yAxisRect.left, yAxisRect.top, yAxisRect.right, yAxisRect.bottom);
        }

        if (mainView != null) {
            mainView.measure(MeasureSpec.makeMeasureSpec(mainViewRect.width(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mainViewRect.height(), MeasureSpec.EXACTLY));
            mainView.layout(mainViewRect.left, mainViewRect.top, mainViewRect.right, mainViewRect.bottom);
        }
    }
}
