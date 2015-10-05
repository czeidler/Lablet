/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.marker.DraggableMarker;


public class HCursorDataModelPainter extends CursorDataModelPainter {

    public HCursorDataModelPainter(MarkerDataModel data) {
        super(data);
    }

    @Override
    public void sort() {
        ((MarkerDataModel)markerData).sortYAscending();
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new HCursorMarker();
    }
}

