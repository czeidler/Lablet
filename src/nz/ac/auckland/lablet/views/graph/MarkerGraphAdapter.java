package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.MarkerData;
/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


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

    @Override
    protected void finalize() {
        data.removeListener(this);
    }

    public void setTo(MarkerDataModel data) {
        if (this.data != null)
            this.data.removeListener(this);

        this.data = data;

        data.addListener(this);
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

    public static MarkerGraphAdapter createPositionAdapter(MarkerDataModel data, String title) {
        return new MarkerGraphAdapter(data, title, new XPositionMarkerGraphAxis(),
                new YPositionMarkerGraphAxis());
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
}
