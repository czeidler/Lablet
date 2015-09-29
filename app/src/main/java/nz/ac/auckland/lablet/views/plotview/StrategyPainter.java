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
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

abstract class ConcurrentPainter {
    protected StrategyPainter parent;

    protected void setParent(StrategyPainter parent) {
        this.parent = parent;
    }

    public PlotPainterContainerView getContainerView() {
        return parent.getContainerView();
    }

    /**
     *
     * @param geometryInfoNeeded if true the realDataRect and the screenRect of the RenderPayload as to be set.
     * @param requestedRealRect can be null
     * @return
     */
    abstract public List<StrategyPainter.RenderPayload> collectRenderPayloads(boolean geometryInfoNeeded,
                                                                              RectF requestedRealRect,
                                                                              RectF maxRealRect);

    protected boolean geometryInfoNeededForRendering() {
        return false;
    }

    abstract protected void render(Canvas bitmapCanvas, StrategyPainter.RenderPayload payload);
}


/**
 * Abstract base class for different data rendering strategies.
 */
abstract public class StrategyPainter extends AbstractPlotPainter {
    public static class RenderPayload {
        final private ConcurrentPainter painter;
        final private RectF realDataRect;
        final private RectF screenRect;

        public RenderPayload(ConcurrentPainter painter, RectF realDataRect, RectF screenRect) {
            this.painter = painter;
            this.realDataRect = realDataRect;
            this.screenRect = screenRect;
        }

        public RectF getRealDataRect() {
            return realDataRect;
        }

        public RectF getScreenRect() {
            return screenRect;
        }

        public ConcurrentPainter getPainter() {
            return painter;
        }
    }

    final protected List<ConcurrentPainter> childPainters = new ArrayList<>();

    /**
     * Indicates if the StrategyPainter uses threads.
     *
     * This can be used to determine if {@link nz.ac.auckland.lablet.views.plotview.StrategyPainter.RenderPayload}s have
     * to be thread safe.
     *
     * @return true if threads are used
     */
    abstract public boolean hasThreads();

    /**
     * Indicates if the StrategyPainter is ready to draw.
     *
     * @return true if the StrategyPainter is ready to draw
     */
    abstract public boolean hasFreeRenderingPipe();

    /**
     * Is called if either the data has changed or a real rect on the screen has to be redrawn.
     *
     * @param newDirt if null only some data has changed
     */
    abstract protected void onNewDirtyRegions(@Nullable RectF newDirt);

    public void addChild(ConcurrentPainter painter) {
        childPainters.add(painter);
        painter.setParent(this);
        invalidate();
    }

    public void removedChild(ConcurrentPainter painter) {
        childPainters.remove(painter);
        painter.setParent(null);
    }

    public List<ConcurrentPainter> getChildPainters() {
        return childPainters;
    }

    public Matrix getRangeMatrixCopy() {
        return containerView.getRangeMatrixCopy();
    }

    protected List<RenderPayload> collectAllRenderPayloads(boolean geometryInfoNeeded, RectF requestedRealRect,
                                                           RectF maxRealRect) {
        List<RenderPayload> payloadList = new ArrayList<>();
        for (ConcurrentPainter painter : childPainters)
            payloadList.addAll(painter.collectRenderPayloads(geometryInfoNeeded, requestedRealRect, maxRealRect));

        return payloadList;
    }

    public RectF getDrawingRange() {
        return containerView.getRange();
    }
}
