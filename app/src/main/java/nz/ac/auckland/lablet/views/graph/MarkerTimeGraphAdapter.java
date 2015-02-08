/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.camera.ITimeData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.misc.Unit;


/**
 * Marker data adapter for the graphs.
 */
public class MarkerTimeGraphAdapter extends MarkerGraphAdapter {
    protected ITimeData timeCalibration;

    public MarkerTimeGraphAdapter(MarkerDataModel data, ITimeData timeCalibration, String title,
                                  MarkerGraphAxis xAxis, MarkerGraphAxis yAxis) {
        super(data, title, xAxis, yAxis);

        this.timeCalibration = timeCalibration;
    }

    public void setTo(MarkerDataModel data,  ITimeData timeCalibration) {
        setTo(data);

        this.timeCalibration = timeCalibration;
    }

    public ITimeData getTimeCalibration() {
        return timeCalibration;
    }

    public static MarkerTimeGraphAdapter createXSpeedAdapter(MarkerDataModel data, ITimeData timeCalibration,
                                                             String title, Unit xUnit, Unit tUnit) {
        return new MarkerTimeGraphAdapter(data, timeCalibration, title, new SpeedTimeMarkerGraphAxis(tUnit),
                new XSpeedMarkerGraphAxis(xUnit, tUnit));
    }

    public static MarkerTimeGraphAdapter createYSpeedAdapter(MarkerDataModel data, ITimeData timeCalibration,
                                                             String title, Unit yUnit, Unit tUnit) {
        return new MarkerTimeGraphAdapter(data, timeCalibration, title, new SpeedTimeMarkerGraphAxis(tUnit),
                new YSpeedMarkerGraphAxis(yUnit, tUnit));
    }
}
