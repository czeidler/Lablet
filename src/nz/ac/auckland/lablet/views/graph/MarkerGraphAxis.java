/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Abstract base class for marker graph axes.
 */
public abstract class MarkerGraphAxis implements AbstractGraphAdapter.IGraphDataAxis {
    protected MarkerGraphAdapter markerGraphAdapter;

    public void setMarkerGraphAdapter(MarkerGraphAdapter adapter) {
        markerGraphAdapter = adapter;
    }

    public MarkerDataModel getData() {
        return markerGraphAdapter.getData();
    }

    public SensorAnalysis getExperimentAnalysis() {
        return markerGraphAdapter.getSensorAnalysis();
    }
}
