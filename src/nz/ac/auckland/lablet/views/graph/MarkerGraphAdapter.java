/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.camera.ITimeCalibration;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Marker data adapter for the graphs.
 */
public class MarkerGraphAdapter extends AbstractGraphAdapter implements MarkerDataModel.IListener {
    protected String title;
    protected MarkerDataModel data;
    protected ITimeCalibration timeCalibration;

    public MarkerGraphAdapter(MarkerDataModel data, ITimeCalibration timeCalibration, String title,
                              MarkerGraphAxis xAxis, MarkerGraphAxis yAxis) {
        this.title = title;

        setTo(data, timeCalibration);

        xAxis.setMarkerGraphAdapter(this);
        yAxis.setMarkerGraphAdapter(this);
        setXAxis(xAxis);
        setYAxis(yAxis);
    }

    public void setTo(MarkerDataModel data, ITimeCalibration timeCalibration) {
        if (this.data != null)
            this.data.removeListener(this);

        this.data = data;
        this.timeCalibration = timeCalibration;

        data.addListener(this);
    }

    @Override
    protected void finalize() {
        data.removeListener(this);
    }

    public static MarkerGraphAdapter createPositionAdapter(MarkerDataModel data, ITimeCalibration timeCalibration, String title) {
        return new MarkerGraphAdapter(data, timeCalibration, title, new XPositionMarkerGraphAxis(),
                new YPositionMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createXSpeedAdapter(MarkerDataModel data, ITimeCalibration timeCalibration, String title) {
        return new MarkerGraphAdapter(data, timeCalibration, title, new SpeedTimeMarkerGraphAxis(),
                new XSpeedMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createYSpeedAdapter(MarkerDataModel data, ITimeCalibration timeCalibration, String title) {
        return new MarkerGraphAdapter(data, timeCalibration, title, new SpeedTimeMarkerGraphAxis(),
                new YSpeedMarkerGraphAxis());
    }

    public MarkerDataModel getData() {
        return data;
    }

    public ITimeCalibration getTimeCalibration() {
        return timeCalibration;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String getTitle() {
        return title;
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
