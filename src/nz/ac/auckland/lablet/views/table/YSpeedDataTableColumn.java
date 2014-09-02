/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.camera.ITimeCalibration;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Table column for the marker data table adapter. Provides the y-speed.
 */
public class YSpeedDataTableColumn extends DataTableColumn {
    @Override
    public int size() {
        return dataModel.getMarkerCount() - 1;
    }

    @Override
    public Number getValue(int index) {
        return getSpeed(index, dataModel, timeCalibration);
    }

    @Override
    public String getHeader() {
        return "velocity [" + dataModel.getCalibrationXY().getYUnit().getUnit() + "/"
                + timeCalibration.getUnit().getBase() + "]";
    }

    public static Number getSpeed(int index, MarkerDataModel markersDataModel, ITimeCalibration timeCalibration) {
        float delta = markersDataModel.getCalibratedMarkerPositionAt(index + 1).y
                - markersDataModel.getCalibratedMarkerPositionAt(index).y;
        float deltaT = timeCalibration.getTimeFromRaw(index + 1) - timeCalibration.getTimeFromRaw(index);
        if (timeCalibration.getUnit().getPrefix().equals("m"))
            deltaT /= 1000;
        return delta / deltaT;
    }
}
