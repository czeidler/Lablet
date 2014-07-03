package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;


public class PlotView extends ViewGroup {
    private IYAxis yAxisView;
    private RangeDrawingView mainView;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);

        yAxisView = new YAxisView(context, attrs);
        addView((ViewGroup)yAxisView);
    }

    public void setMainView(RangeDrawingView mainView) {
        this.mainView = mainView;
        if (this.mainView != null)
            removeView(mainView);
        addView(mainView);
    }

    public void setRangeY(float bottom, float top) {
        yAxisView.setDataRange(bottom, top);
        mainView.setRangeY(bottom, top);
    }

    public IYAxis getYAxisView() {
        return yAxisView;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int yAxisRight = (int)yAxisView.optimalWidthForHeight(bottom - top);
        Rect yAxisRect = new Rect(0, 0, yAxisRight, bottom);
        Rect mainViewRect = new Rect(yAxisRight, (int)yAxisView.getAxisTopOffset(), right,
                bottom - (int)yAxisView.getAxisBottomOffset());

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
