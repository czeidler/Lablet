/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.ExperimentRunData;


/**
 * Table column for the marker data table adapter. Provides a time column for the use in combination with an
 * acceleration columns.
 */
public class AccelerationTimeDataTableColumn extends TimeDataTableColumn {
    @Override
    public int size() {
        return markerDataModel.getMarkerCount() - 2;
    }

    @Override
    public Number getValue(int index) {
        ExperimentRunData experimentRunData = experimentAnalysis.getExperimentRunData();
        float t0 = experimentRunData.getRunValueAt(index);
        float t2 = experimentRunData.getRunValueAt(index + 2);
        return t0 + (t2 - t0) / 2.f;
    }
}
