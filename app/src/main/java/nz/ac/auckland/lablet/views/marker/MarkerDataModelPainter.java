/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.Canvas;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;


public class MarkerDataModelPainter<T> extends AbstractMarkerPainter<T> {
    private int MAX_DISPLAYED_MARKERS = 100;

    protected LastInsertMarkerManager lastInsertMarkerManager = new LastInsertMarkerManager();

    public MarkerDataModelPainter(AbstractPointDataModel<T> data) {
        super(data);
    }

    @Override
    public List<IMarker> getSelectableMarkerList() {
        List<IMarker> selectableMarkers = new ArrayList<>();
        int index = markerData.getSelectedMarkerData();
        if (index < 0)
            return selectableMarkers;
        IMarker selectedMarker = markerList.get(index);
        if (selectedMarker != null)
            selectableMarkers.add(selectedMarker);
        return selectableMarkers;
    }

    public IMarker getMarkerAtScreenPosition(PointF screenPosition) {
        int currentMarkerRow = markerData.getSelectedMarkerData();

        int start = currentMarkerRow - MAX_DISPLAYED_MARKERS / 2 + 1;
        if (start < 0)
            start = 0;
        int end = currentMarkerRow + MAX_DISPLAYED_MARKERS / 2 + 1;
        if (end > markerList.size())
            end = markerList.size();

        for (int i = start; i < end; i++) {
            IMarker marker = markerList.get(i);
            if (!(marker instanceof DraggableMarker))
                continue;
            if (((DraggableMarker)marker).isPointOnSelectArea(screenPosition))
                return marker;
        }
        return null;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int currentMarkerRow = markerData.getSelectedMarkerData();
        IMarker topMarker = getMarkerForRow(currentMarkerRow);

        int start = currentMarkerRow - MAX_DISPLAYED_MARKERS / 2 + 1;
        if (start < 0)
            start = 0;
        int end = currentMarkerRow + MAX_DISPLAYED_MARKERS / 2 + 1;
        if (end > markerList.size())
            end = markerList.size();

        for (int i = start; i < end; i++) {
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
    protected class LastInsertMarkerManager {
        private int markerInsertedInLastRun = -1;
        private PointF lastMarkerPosition = new PointF();

        public void onCurrentFrameChanging(AbstractPointDataModel<T> markersDataModel) {
            // Index could be out of bounds, e.g., when the marker data has been cleared.
            if (markerInsertedInLastRun >= markerData.size()) {
                markerInsertedInLastRun =-1;
                return;
            }

            if (markerInsertedInLastRun >= 0) {
                if (markerData.getPosition(markerInsertedInLastRun).equals(lastMarkerPosition)) {
                    markerData.removeData(markerInsertedInLastRun);
                    int selectedIndex = markerInsertedInLastRun - 1;
                    if (selectedIndex < 0)
                        selectedIndex = 0;
                    markersDataModel.selectMarkerData(selectedIndex);
                }
                markerInsertedInLastRun = -1;
            }
        }

        public void onNewMarkerInserted(int index, MarkerData data) {
            markerInsertedInLastRun = index;
            lastMarkerPosition.set(data.getPosition());
        }
    }
}
