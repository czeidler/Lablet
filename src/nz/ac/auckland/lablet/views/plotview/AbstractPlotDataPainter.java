/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


abstract public class AbstractPlotDataPainter extends AbstractPlotPainter {
    protected AbstractPlotDataAdapter dataAdapter;
    protected AbstractPlotDataAdapter.IListener listener = null;

    abstract protected AbstractPlotDataAdapter.IListener createListener();

    public void setDataAdapter(AbstractPlotDataAdapter adapter) {
        if (this.dataAdapter != null)
            this.dataAdapter.removeListener(listener);

        this.dataAdapter = adapter;

        if (this.dataAdapter != null) {
            listener = createListener();
            this.dataAdapter.addListener(listener);
        }

        invalidate();
    }

    public AbstractPlotDataAdapter getDataAdapter() {
        return dataAdapter;
    }
}