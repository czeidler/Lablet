/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;

import java.util.List;


abstract public class ArrayOffScreenPlotPainter extends OffScreenPlotPainter {
    protected Region1D dirtyRegion = new Region1D();
    protected int maxDirtyRanges = -1;

    protected class ArrayRenderPayload extends RenderPayload {
        public Matrix rangeMatrix;
        public CloneablePlotDataAdapter adapter;
        public Region1D region;

        public ArrayRenderPayload(RectF realDataRect, Rect screenRect,
                                  Matrix rangeMatrix, CloneablePlotDataAdapter adapter, Region1D region) {
            super(realDataRect, screenRect);
            this.rangeMatrix = rangeMatrix;
            this.adapter = adapter;
            this.region = region;
        }
    }

    abstract protected RectF getRealDataRect(AbstractPlotDataAdapter adapter, int startIndex, int lastIndex);
    abstract protected void drawRange(Canvas bitmapCanvas, ArrayRenderPayload payload, Range range);

    @Override
    protected void render(Canvas bitmapCanvas, RenderPayload payload) {
        ArrayRenderPayload renderPayload = (ArrayRenderPayload)payload;

        List<Range> rangeList = renderPayload.region.getRanges();
        for (Range range : rangeList)
            drawRange(bitmapCanvas, renderPayload, range);
    }

    protected void onOffScreenRenderingFinished(RenderPayload payload) {
        super.onOffScreenRenderingFinished(payload);

        if (!hasFreeRenderingPipe())
            return;

        flushDirtyRegion();
    }

    protected void flushDirtyRegion() {
        CloneablePlotDataAdapter adapter = (CloneablePlotDataAdapter)dataAdapter;
        RectF realDataRect = getRealDataRect(adapter, dirtyRegion.getMin(), dirtyRegion.getMax());
        Rect screenRect = containerView.toScreen(realDataRect);
        ArrayRenderPayload renderPayload = new ArrayRenderPayload(realDataRect, screenRect,
                containerView.getRangeMatrix(), adapter.clone(),
                new Region1D(dirtyRegion));

        triggerOffScreenRendering(renderPayload);

        dirtyRegion.clear();
    }

    /**
     * Set number of max dirty ranges.
     *
     * This can be used to update the view in smaller steps even there is not free rendering pipe available.
     *
     * @param maxDirtyRanges number of max dirty ranges
     */
    public void setMaxDirtyRanges(int maxDirtyRanges) {
        this.maxDirtyRanges = maxDirtyRanges;
    }

    @Override
    protected AbstractPlotDataAdapter.IListener createListener() {
        return new AbstractPlotDataAdapter.IListener() {
            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                dirtyRegion.addRange(index, index + number - 1);

                if ((maxDirtyRanges > 0 && maxDirtyRanges <= dirtyRegion.getSize()) || hasFreeRenderingPipe())
                    flushDirtyRegion();
            }

            @Override
            public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
                triggerRedrawAll();
            }

            @Override
            public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
                triggerRedrawAll();
            }

            @Override
            public void onAllDataChanged(AbstractPlotDataAdapter plot) {
                triggerRedrawAll();
            }

            private void triggerRedrawAll() {
                RectF realDataRect = containerView.getRangeRect();
                Rect screenRect = containerView.toScreen(realDataRect);
                ArrayRenderPayload renderPayload = new ArrayRenderPayload(realDataRect, screenRect,
                        containerView.getRangeMatrix(), ((CloneablePlotDataAdapter)dataAdapter).clone(),
                        new Region1D(0, dataAdapter.getSize() - 1));
                renderPayload.clearParentBitmap = true;
                triggerOffScreenRendering(renderPayload);

                dirtyRegion.clear();
            }
        };
    }
}
