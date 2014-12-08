/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;


abstract public class ArrayConcurrentPainter extends ConcurrentPainter {
    protected class ArrayRenderPayload extends StrategyPainter.RenderPayload {
        final private Matrix rangeMatrix;
        private CloneablePlotDataAdapter adapter;
        private Region1D region;

        public ArrayRenderPayload(ConcurrentPainter painter, RectF realDataRect, RectF screenRect,
                                  Matrix rangeMatrix, CloneablePlotDataAdapter adapter, Region1D region) {
            super(painter, realDataRect, screenRect);
            this.rangeMatrix = rangeMatrix;
            this.adapter = adapter;
            this.region = region;
        }

        public Matrix getRangeMatrix() {
            return rangeMatrix;
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

    final protected CloneablePlotDataAdapter dataAdapter;

    public ArrayConcurrentPainter(CloneablePlotDataAdapter dataAdapter) {
        this.dataAdapter = dataAdapter;
        dataAdapter.addListener(dataListener);
    }

    public CloneablePlotDataAdapter getAdapter() {
        return dataAdapter;
    }

    @Override
    public List<StrategyPainter.RenderPayload> collectRenderPayloads(boolean geometryInfoNeeded,
                                                                     RectF requestedRealRect, RectF maxRealRect) {
        List<StrategyPainter.RenderPayload> payloads = new ArrayList<>();

        if (dataAdapter == null || dataAdapter.getSize() == 0)
            return payloads;
        if (dirtyRegion.getSize() == 0 && requestedRealRect == null)
            return payloads;

        // do we need the geometry for ourselves?
        if (geometryInfoNeededForRendering())
            geometryInfoNeeded = true;

        RectF realDataRect = null;
        RectF screenRect = null;
        if (requestedRealRect != null) {
            Range dirty = getDataRangeFor(requestedRealRect.left, requestedRealRect.right);
            dirtyRegion.addRange(dirty);
        } else {
            Range maxDirt = getDataRangeFor(maxRealRect.left, maxRealRect.right);
            if (dirtyRegion.getMin() < maxDirt.min || dirtyRegion.getMax() > maxDirt.max) {
                maxDirt.min = Math.min(dirtyRegion.getMin(), maxDirt.min);
                maxDirt.max = Math.max(dirtyRegion.getMax(), maxDirt.max);
                dirtyRegion.clear();
                dirtyRegion.addRange(maxDirt);
            }
        }

        if (geometryInfoNeeded) {
            realDataRect = getRealDataRect(dirtyRegion.getMin(), dirtyRegion.getMax());
            screenRect = getContainerView().toScreen(realDataRect);
        }

        StrategyPainter.RenderPayload payload = makeRenderPayload(realDataRect, screenRect, dirtyRegion);
        payloads.add(payload);

        // clear
        dirtyRegion = new Region1D();

        return payloads;
    }

    @Override
    protected void render(Canvas bitmapCanvas, StrategyPainter.RenderPayload payload) {
        ArrayRenderPayload renderPayload = (ArrayRenderPayload)payload;

        List<Range> rangeList = renderPayload.region.getRanges();
        for (Range range : rangeList)
            drawRange(bitmapCanvas, renderPayload, range);
    }

    protected Region1D dirtyRegion = new Region1D();
    protected int maxDirtyRanges = -1;

    abstract protected RectF getRealDataRect(int startIndex, int lastIndex);
    abstract protected Range getDataRangeFor(float left, float right);

    abstract protected void drawRange(Canvas bitmapCanvas, ArrayRenderPayload payload, Range range);

    protected ArrayRenderPayload makeRenderPayload(RectF realDataRect, RectF screenRect, Region1D regionToRender) {
        // optimization: just use the normal adapter if we don't use threads
        CloneablePlotDataAdapter adapter = dataAdapter;
        if (parent.hasThreads())
            adapter = dataAdapter.clone(regionToRender);

        ArrayRenderPayload renderPayload = new ArrayRenderPayload(this, realDataRect, screenRect,
                parent.getRangeMatrixCopy(),
                adapter,
                regionToRender);

        return renderPayload;
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

    private AbstractPlotDataAdapter.IListener dataListener = new AbstractPlotDataAdapter.IListener() {
        @Override
        public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
            //TODO find out what that was good for: onSetupOffScreenBitmap();
            int start = index;
            if (index > 0)
                start -= 1;
            dirtyRegion.addRange(start, index + number - 1);
            if ((maxDirtyRanges > 0 && maxDirtyRanges <= dirtyRegion.getSize()) || parent.hasFreeRenderingPipe())
                parent.onNewDirtyRegions(null);
        }

        @Override
        public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
            invalidateAll();
        }

        @Override
        public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
            invalidateAll();
        }

        @Override
        public void onAllDataChanged(AbstractPlotDataAdapter plot) {
            invalidateAll();
        }
    };

    private void invalidateAll() {
        parent.invalidate();
    }
}
