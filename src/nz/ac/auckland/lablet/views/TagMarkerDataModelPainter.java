/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.Canvas;
import android.graphics.PointF;
import nz.ac.auckland.lablet.experiment.CalibratedMarkerDataModel;
import nz.ac.auckland.lablet.experiment.MarkerData;

import java.util.ArrayList;
import java.util.List;

/**
 * Painter for tagged data. For example, the tagged data from a camera experiment.
 */
public class TagMarkerDataModelPainter extends AbstractMarkerPainter {
    private LastInsertMarkerManager lastInsertMarkerManager = new LastInsertMarkerManager();

    public TagMarkerDataModelPainter(CalibratedMarkerDataModel data) {
        super(data);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        List<IMarker> selectableMarkers = new ArrayList<>();
        IMarker selectedMarker = markerList.get(markerData.getSelectedMarkerData());
        selectableMarkers.add(selectedMarker);
        return selectableMarkers;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentMarkerRow = markerData.getSelectedMarkerData();
        IMarker topMarker = getMarkerForRow(currentMarkerRow);
        for (int i = 0; i < markerList.size(); i++) {
            IMarker marker = markerList.get(i);
            if (marker == topMarker)
                continue;

            float runDistance = Math.abs(currentMarkerRow - i);
            float currentPriority = (float)(0.35 - 0.1 * runDistance);
            if (currentPriority > 1.0)
                currentPriority = (float)1.0;
            if (currentPriority < 0.1)
                currentPriority = (float)0.1;

            marker.onDraw(canvas, currentPriority);
        }
        if (topMarker != null)
            topMarker.onDraw(canvas, (float)1.0);
    }

    @Override
    protected DraggableMarker createMarkerForRow(int row) {
        return new SimpleMarker();
    }

    /**
     * If the last inserted marker hasn't moved remove it again.
     */
    private class LastInsertMarkerManager {
        private int markerInsertedInLastRun = -1;
        private PointF lastMarkerPosition = new PointF();

        void onCurrentRunChanging(CalibratedMarkerDataModel markersDataModel) {
            // Index could be out of bounds, e.g., when the marker data has been cleared.
            if (markerInsertedInLastRun >= markerData.getMarkerCount()) {
                markerInsertedInLastRun =-1;
                return;
            }

            if (markerInsertedInLastRun >= 0) {
                MarkerData lastMarkerData = markerData.getMarkerDataAt(markerInsertedInLastRun);
                if (lastMarkerData.getPosition().equals(lastMarkerPosition)) {
                    markerData.removeMarkerData(markerInsertedInLastRun);
                    int selectedIndex = markerInsertedInLastRun - 1;
                    if (selectedIndex < 0)
                        selectedIndex = 0;
                    markersDataModel.selectMarkerData(selectedIndex);
                }
                markerInsertedInLastRun = -1;
            }
        }

        void onNewMarkerInserted(int index, MarkerData data) {
            markerInsertedInLastRun = index;
            lastMarkerPosition.set(data.getPosition());
        }
    }

    public void setCurrentRun(int run) {
        lastInsertMarkerManager.onCurrentRunChanging((CalibratedMarkerDataModel)markerData);

        // check if we have the run in the data list
        MarkerData data = null;
        int index = markerData.findMarkerDataByRun(run);
        if (index >= 0) {
            data = markerData.getMarkerDataAt(index);
            markerData.selectMarkerData(index);
        }

        if (data == null) {
            data = new MarkerData(run);
            if (markerData.getMarkerCount() > 0) {
                int selectedIndex = markerData.getSelectedMarkerData();
                MarkerData prevData = markerData.getMarkerDataAt(selectedIndex);
                data.setPosition(prevData.getPosition());
                data.getPosition().x += 5;

                // sanitize the new marker position
                PointF screenPos = new PointF();
                containerView.toScreen(data.getPosition(), screenPos);
                sanitizeScreenPoint(screenPos);
                containerView.fromScreen(screenPos, data.getPosition());
            } else {
                // center the first marker
                PointF initPosition = new PointF();
                initPosition.x = (containerView.getRangeRight() - containerView.getRangeLeft()) * 0.5f;
                initPosition.y = (containerView.getRangeTop() - containerView.getRangeBottom()) * 0.5f;
                data.setPosition(initPosition);
            }

            int newIndex = markerData.addMarkerData(data);
            markerData.selectMarkerData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}
