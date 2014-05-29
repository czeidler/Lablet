/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.Experiment;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * position columns.
 */
public class TimeDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        Experiment experiment = experimentAnalysis.getExperiment();
        MarkerDataModel markerData = experimentAnalysis.getTagMarkers();
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
