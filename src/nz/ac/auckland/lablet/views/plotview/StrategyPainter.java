/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

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
                                                                              RectF requestedRealRect);

    abstract protected void render(Canvas bitmapCanvas, StrategyPainter.RenderPayload payload);
}


abstract public class StrategyPainter extends AbstractPlotPainter {
    public static class RenderPayload {
        final private ConcurrentPainter painter;
        final private RectF realDataRect;
        final private Rect screenRect;
        private boolean completeRedraw = false;

        public RenderPayload(ConcurrentPainter painter, RectF realDataRect, Rect screenRect) {
            this.painter = painter;
            this.realDataRect = realDataRect;
            this.screenRect = screenRect;
        }

        public RectF getRealDataRect() {
            return realDataRect;
        }

        public Rect getScreenRect() {
            return screenRect;
        }

        public boolean isCompleteRedraw() {
            return completeRedraw;
        }

        public void setCompleteRedraw(boolean completeRedraw) {
            this.completeRedraw = completeRedraw;
        }

        public ConcurrentPainter getPainter() {
            return painter;
        }
    }

    final protected List<ConcurrentPainter> childPainters = new ArrayList<>();

    abstract public boolean hasThreads();

    abstract public boolean hasFreeRenderingPipe();

    abstract protected void onNewDirtyRegions();

    public void addChild(ConcurrentPainter painter) {
        childPainters.add(painter);
        painter.setParent(this);
    }

    public void removedChild(ConcurrentPainter painter) {
        childPainters.remove(painter);
        painter.setParent(null);
    }

    public List<ConcurrentPainter> getChildPainters() {
        return childPainters;
    }
}
