/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.misc.Unit;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.Range;


public class MarkerGraphAdapter extends AbstractGraphAdapter implements MarkerDataModel.IListener {
    protected String title;
    protected MarkerDataModel data;

    public MarkerGraphAdapter(MarkerDataModel data, String title, MarkerGraphAxis xAxis, MarkerGraphAxis yAxis) {
        this.title = title;

        setTo(data);

        xAxis.setMarkerGraphAdapter(this);
        yAxis.setMarkerGraphAdapter(this);
        setXAxis(xAxis);
        setYAxis(yAxis);
    }

    public void release() {
        if (data != null) {
            data.removeListener(this);
            data = null;
        }
    }

    @Override
    protected void finalize() {
        release();
    }

    public void setTo(MarkerDataModel data) {
        if (this.data != null)
            this.data.removeListener(this);

        this.data = data;

        this.data.addListener(this);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public MarkerDataModel getData() {
        return data;
    }

    public static MarkerGraphAdapter createPositionAdapter(MarkerDataModel data, String title, Unit xUnit,
                                                           Unit yUnit, IMinRangeGetter xMinRangeGetter,
                                                           IMinRangeGetter yMinRangeGetter) {
        return new MarkerGraphAdapter(data, title, new XPositionMarkerGraphAxis(xUnit, xMinRangeGetter),
                new YPositionMarkerGraphAxis(yUnit, yMinRangeGetter));
    }

    @Override
    public void onDataAdded(MarkerDataModel model, int index) {
        // see onDataChanged
        //notifyDataAdded(index, 1);
        notifyAllDataChanged();
    }

    @Override
    public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
        notifyDataRemoved(index, 1);
    }

    @Override
    public void onDataChanged(MarkerDataModel model, int index, int number) {
        // when displaying the velocity the marker point index is not equal to the velocity point index
        // for that reason we invalidate all data
        // TODO: this could be optimized
        //notifyDataChanged(index, number);
        notifyAllDataChanged();
    }

    @Override
    public void onAllDataChanged(MarkerDataModel model) {
        notifyAllDataChanged();
    }

    @Override
    public void onDataSelected(MarkerDataModel model, int index) {

    }

    @Override
    public Range getRange(Number leftReal, Number rightReal) {
        return new Range(0, getSize() - 1);
    }

}
