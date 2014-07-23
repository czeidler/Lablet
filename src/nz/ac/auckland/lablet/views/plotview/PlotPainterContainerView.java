/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.content.Context;
import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class JoinedList<T> implements Iterable<T> {
    private List<List<? extends T>> allLists = new ArrayList<>();

    public void addList(List list) {
        allLists.add(list);
    }

    public JoinedList(List<? extends T>... lists) {
        for (List<? extends T> list : lists)
            addList(list);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            final Iterator<List<? extends T>> allListIterator = allLists.iterator();
            Iterator<? extends T> currentListIterator = null;

            {
                if (allListIterator.hasNext())
                    currentListIterator = allListIterator.next().iterator();
            }

            @Override
            public boolean hasNext() {
                return currentListIterator.hasNext();
            }

            @Override
            public T next() {
                T next = currentListIterator.next();
                if (!currentListIterator.hasNext() && allListIterator.hasNext())
                    currentListIterator = allListIterator.next().iterator();
                return next;
            }

            @Override
            public void remove() {

            }
        };
    }
}

public class PlotPainterContainerView extends RangeDrawingView {
    final private List<IPlotPainter> backgroundPainters = new ArrayList();
    final private List<IPlotPainter> plotPainters = new ArrayList();
    final private List<IPlotPainter> foregroundPainters = new ArrayList();
    final private JoinedList<IPlotPainter> allPainters = new JoinedList(backgroundPainters, plotPainters,
            foregroundPainters);

    public PlotPainterContainerView(Context context) {
        super(context);
    }

    public void addBackgroundPainter(IPlotPainter painter) {
        backgroundPainters.add(painter);
        painter.setContainer(this);
    }

    public void addPlotPainter(IPlotPainter painter) {
        plotPainters.add(painter);
        painter.setContainer(this);
    }

    public void addForegroundPainter(IPlotPainter painter) {
        foregroundPainters.add(painter);
        painter.setContainer(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (IPlotPainter painter : allPainters) {
            canvas.save();
            painter.onDraw(canvas);
            canvas.restore();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        for (IPlotPainter painter : allPainters)
            painter.onSizeChanged(w, h, oldw, oldh);
    }

    public List<IPlotPainter> getPlotPainters() {
        return plotPainters;
    }

    @Override
    public boolean setXRange(float left, float right, boolean keepDistance) {
        float oldLeft = getRangeLeft();
        float oldRight = getRangeRight();

        if (!super.setXRange(left, right, keepDistance))
            return false;

        for (IPlotPainter painter : allPainters)
            painter.onXRangeChanged(getRangeLeft(), getRangeRight(), oldLeft, oldRight);

        return true;
    }

    @Override
    public boolean setYRange(float bottom, float top, boolean keepDistance) {
        float oldBottom = getRangeBottom();
        float oldTop = getRangeTop();

        if (!super.setYRange(bottom, top, keepDistance))
            return false;

        for (IPlotPainter painter : allPainters)
            painter.onYRangeChanged(getRangeBottom(), getRangeTop(), oldBottom, oldTop);

        return true;
    }
}
