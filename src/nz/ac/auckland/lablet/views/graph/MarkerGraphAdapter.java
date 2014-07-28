/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Marker data adapter for the graphs.
 */
public class MarkerGraphAdapter extends AbstractGraphAdapter implements MarkerDataModel.IMarkerDataModelListener {
    protected String title;
    protected MarkerDataModel data;
    protected SensorAnalysis sensorAnalysis;

    public MarkerGraphAdapter(SensorAnalysis sensorAnalysis, String title, MarkerGraphAxis xAxis,
                              MarkerGraphAxis yAxis) {
        this.title = title;
        setSensorAnalysis(sensorAnalysis);

        xAxis.setMarkerGraphAdapter(this);
        yAxis.setMarkerGraphAdapter(this);
        setXAxis(xAxis);
        setYAxis(yAxis);
    }

    public void setSensorAnalysis(SensorAnalysis sensorAnalysis) {
        if (data != null)
            data.removeListener(this);
        this.sensorAnalysis = sensorAnalysis;
        data = sensorAnalysis.getTagMarkers();
        data.addListener(this);

        notifyAllDataChanged();
    }

    @Override
    protected void finalize() {
        data.removeListener(this);
    }

    public static MarkerGraphAdapter createPositionAdapter(SensorAnalysis sensorAnalysis, String title) {
        return new MarkerGraphAdapter(sensorAnalysis, title, new XPositionMarkerGraphAxis(),
                new YPositionMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createXSpeedAdapter(SensorAnalysis sensorAnalysis, String title) {
        return new MarkerGraphAdapter(sensorAnalysis, title, new SpeedTimeMarkerGraphAxis(),
                new XSpeedMarkerGraphAxis());
    }

    public static MarkerGraphAdapter createYSpeedAdapter(SensorAnalysis sensorAnalysis, String title) {
        return new MarkerGraphAdapter(sensorAnalysis, title, new SpeedTimeMarkerGraphAxis(),
                new YSpeedMarkerGraphAxis());
    }

    public MarkerDataModel getData() {
        return data;
    }

    public SensorAnalysis getSensorAnalysis() {
        return sensorAnalysis;
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
        notifyDataAdded(index, 1);
    }

    @Override
    public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
        notifyDataRemoved(index, 1);
    }

    @Override
    public void onDataChanged(MarkerDataModel model, int index, int number) {
        notifyDataChanged(index, number);
    }

    @Override
    public void onAllDataChanged(MarkerDataModel model) {
        notifyAllDataChanged();
    }

    @Override
    public void onDataSelected(MarkerDataModel model, int index) {

    }
}
