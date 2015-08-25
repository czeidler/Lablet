/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.experiment.PointDataModel;


public class VCursorDataModelPainter extends CursorDataModelPainter {

    public VCursorDataModelPainter(PointDataModel data) {
        super(data);
    }

    @Override
    public void sort() {
        markerData.sortXAscending();
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new VCursorMarker();
    }
}
