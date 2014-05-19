/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.table;

import nz.ac.aucklanduni.physics.tracker.experiment.Experiment;
import nz.ac.aucklanduni.physics.tracker.experiment.MarkersDataModel;


public class TimeDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markersDataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        MarkersDataModel markerData = experimentAnalysis.getTagMarkers();
        int runId = markerData.getMarkerDataAt(index).getRunId();
        return experiment.getRunValueAt(runId);
    }

    @Override
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format("%.1f", number.floatValue());
    }

    @Override
    public String getHeader() {
        return "time [" + experimentAnalysis.getExperiment().getRunValueUnit() + "]";
    }
}
