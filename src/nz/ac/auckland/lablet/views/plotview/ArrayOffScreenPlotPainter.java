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
        private Matrix rangeMatrix;
        private CloneablePlotDataAdapter adapter;
        private Region1D region;

        public ArrayRenderPayload(RectF realDataRect, Rect screenRect,
                                  Matrix rangeMatrix, CloneablePlotDataAdapter adapter, Region1D region) {
            super(realDataRect, screenRect);
            this.rangeMatrix = rangeMatrix;
            this.adapter = adapter;
            this.region = region;
        }

        public Matrix getRangeMatrix() {
            return rangeMatrix;
        }

        public void setRangeMatrix(Matrix rangeMatrix) {
            this.rangeMatrix = rangeMatrix;
        }

        public CloneablePlotDataAdapter getAdapter() {
            return adapter;
        }

        public void setAdapter(CloneablePlotDataAdapter adapter) {
            this.adapter = adapter;
        }

        public Region1D getRegion() {
            return region;
        }

        public void setRegion(Region1D region) {
            this.region = region;
        }
    }

    abstract protected RectF getRealDataRect(int startIndex, int lastIndex);
    protected Range getDataRangeFor(float left, float right) {
        return new Range(0, dataAdapter.getSize() - 1);
    }
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
        if (dirtyRegion.getSize() == 0)
            return;

        RectF realDataRect = getRealDataRect(dirtyRegion.getMin(), dirtyRegion.getMax());
        Rect screenRect = containerView.toScreen(realDataRect);

        triggerJob(realDataRect, screenRect, new Region1D(dirtyRegion), false);

        dirtyRegion.clear();
    }

    @Override
    public void invalidate() {
        if (containerView == null || dataAdapter == null)
            return;

        emptyOffScreenRenderingQueue();

        Range dirty = getDataRangeFor(containerView.getRangeLeft(), containerView.getRangeRight());
        Region1D regionToRender = new Region1D(dirty);
        RectF realDataRect = containerView.getRange();
        Rect screenRect = containerView.toScreen(realDataRect);

        triggerJob(realDataRect, screenRect, regionToRender, true);

        dirtyRegion.clear();
    }

    protected void triggerJob(RectF realDataRect, Rect screenRect, Region1D regionToRender, boolean clearParentBitmap) {
        ArrayRenderPayload renderPayload = new ArrayRenderPayload(realDataRect, screenRect,
                containerView.getRangeMatrixCopy(),
                ((CloneablePlotDataAdapter)dataAdapter).clone(regionToRender),
                regionToRender);
        renderPayload.setClearParentBitmap(clearParentBitmap);

        triggerOffScreenRendering(renderPayload);
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
                onSetupOffScreenBitmap();

                int start = index;
                if (index > 0)
                    start -= 1;
                dirtyRegion.addRange(start, index + number - 1);
                if ((maxDirtyRanges > 0 && maxDirtyRanges <= dirtyRegion.getSize()) || hasFreeRenderingPipe())
                    flushDirtyRegion();
            }

            @Override
            public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
                invalidate();
            }

            @Override
            public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
                invalidate();
            }

            @Override
            public void onAllDataChanged(AbstractPlotDataAdapter plot) {
                invalidate();
            }

        };
    }

    @Override
    public void onXRangeChanged(float left, float right, float oldLeft, float oldRight) {
        super.onXRangeChanged(left, right, oldLeft, oldRight);

        invalidate();
    }

    @Override
    public void onYRangeChanged(float bottom, float top, float oldBottom, float oldTop) {
        super.onYRangeChanged(bottom, top, oldBottom, oldTop);

        invalidate();
    }
}
