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
    public void setRangeX(float left, float right) {
        float oldLeft = getRangeLeft();
        float oldRight = getRangeRight();

        super.setRangeX(left, right);

        for (IPlotPainter painter : allPainters)
            painter.onXRangeChanged(left, right, oldLeft, oldRight);
    }

    public void setRangeY(float bottom, float top) {
        float oldBottom = getRangeBottom();
        float oldTop = getRangeTop();

        super.setRangeY(bottom, top);

        for (IPlotPainter painter : allPainters)
            painter.onYRangeChanged(bottom, top, oldBottom, oldTop);
    }
}
