/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


abstract public class AbstractPlotPainter implements IPlotPainter {
    protected AbstractPlotDataAdapter dataAdapter;
    protected AbstractPlotDataAdapter.IListener listener = null;
    protected PlotPainterContainerView containerView;

    protected IScale xScale = new LinearScale();
    protected IScale yScale = new LinearScale();

    abstract protected AbstractPlotDataAdapter.IListener createListener();

    public void setDataAdapter(AbstractPlotDataAdapter adapter) {
        this.dataAdapter = adapter;
        listener = createListener();
        this.dataAdapter.setListener(listener);
    }

    public void setContainer(PlotPainterContainerView view) {
        this.containerView = view;
    }

    @Override
    public void setXScale(IScale xScale) {
        this.xScale = xScale;
    }

    @Override
    public void setYScale(IScale yScale) {
        this.yScale = yScale;
    }

    @Override
    public void onXRangeChanged() {

    }

    @Override
    public void onYRangeChanged() {

    }
}