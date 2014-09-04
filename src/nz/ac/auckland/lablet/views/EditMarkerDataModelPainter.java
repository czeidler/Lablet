/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import nz.ac.auckland.lablet.experiment.CalibratedMarkerDataModel;

import java.util.List;


public class EditMarkerDataModelPainter extends AbstractMarkerPainter {

    public EditMarkerDataModelPainter(CalibratedMarkerDataModel data) {
        super(data);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        return markerList;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentMarkerRow = markerData.getSelectedMarkerData();
        IMarker topMarker = getMarkerForRow(currentMarkerRow);
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);
            if (marker == topMarker)
                continue;
            marker.onDraw(canvas, 1);
        }
        if (topMarker != null)
            topMarker.onDraw(canvas, 1);
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new SimpleMarker();
    }
}
