/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.content.Context;
import android.graphics.*;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import nz.ac.auckland.lablet.views.plotview.*;


/**
 * Displays one or more of marker datasets.
 *
 * <p>
 * The MarkerView also takes track of the currently selected {@link IMarker}.
 * </p>
 */
public class MarkerView extends PlotPainterContainerView {
    final protected Rect viewFrame = new Rect();

    private int parentWidth;
    private int parentHeight;

    private AbstractMarkerPainter.MarkerPainterGroup markerPainterGroup = null;

    public MarkerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MarkerView(Context context) {
        super(context);

        init();
    }

    private void init() {
        setWillNotDraw(false);

        getDrawingRect(viewFrame);
    }

    public boolean isAnyMarkerSelectedForDrag() {
        if (markerPainterGroup == null)
            return false;
        return markerPainterGroup.getSelectedForDragMarker() != null;
    }

    @Override
    public void addPlotPainter(IPlotPainter painter) {
        super.addPlotPainter(painter);

        if (painter instanceof AbstractMarkerPainter) {
            AbstractMarkerPainter markerPainter = (AbstractMarkerPainter)painter;
            if (markerPainterGroup == null)
                markerPainterGroup = markerPainter.getMarkerPainterGroup();
            else
                markerPainter.setMarkerPainterGroup(markerPainterGroup);
        }
    }

    public void setCurrentFrame(int frame, @Nullable PointF insertHint) {
        for (IPlotPainter painter : allPainters) {
            if (!(painter instanceof TagMarkerDataModelPainter))
                continue;
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            tagMarkerDataModelPainter.setCurrentFrame(frame, insertHint);
            // deselect any marker
            tagMarkerDataModelPainter.getMarkerPainterGroup().deselect();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (viewFrame.width() != parentWidth || viewFrame.height() != parentHeight)
            requestLayout();
    }

    public void setSize(int width, int height) {
        parentWidth = width;
        parentHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = parentWidth;
        int height = parentHeight;

        if (specWidthMode == MeasureSpec.AT_MOST || specHeightMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);

            LayoutParams params = getLayoutParams();
            assert params != null;
            params.width = parentWidth;
            params.height = parentHeight;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        setMeasuredDimension(parentWidth, parentHeight);
    }

    public void release() {
        for (IPlotPainter painter : allPainters) {
            if (!(painter instanceof TagMarkerDataModelPainter))
                continue;
            TagMarkerDataModelPainter tagMarkerDataModelPainter = (TagMarkerDataModelPainter)painter;
            tagMarkerDataModelPainter.release();
        }
    }
}