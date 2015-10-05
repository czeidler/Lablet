/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;

import android.graphics.PointF;
import android.support.annotation.Nullable;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Painter for tagged data. For example, the tagged data from a camera experiment.
 */
public class TagMarkerDataModelPainter extends MarkerDataModelPainter<MarkerData> {

    public TagMarkerDataModelPainter(MarkerDataModel data) {
        super(data);
    }

    public void setCurrentFrame(int frame, @Nullable PointF insertHint) {
        lastInsertMarkerManager.onCurrentFrameChanging(markerData);

        // check if we have the run in the data list
        MarkerData data = null;
        int index = ((MarkerDataModel)markerData).findMarkerDataByRun(frame);
        if (index >= 0) {
            data = markerData.getAt(index);
            markerData.selectMarkerData(index);
        }

        if (data == null) {
            data = new MarkerData(frame);
            if (insertHint != null) {
                sanitizeScreenPoint(insertHint);
                PointF insertHintReal = new PointF();
                containerView.fromScreen(insertHint, insertHintReal);
                data.setPosition(insertHintReal);
            } else {
                if (markerData.size() > 0 && markerData.getSelectedMarkerData() > 0) {
                    int selectedIndex = markerData.getSelectedMarkerData();
                    MarkerData prevData = markerData.getAt(selectedIndex);
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
            }

            int newIndex = markerData.addData(data);
            markerData.selectMarkerData(newIndex);

            lastInsertMarkerManager.onNewMarkerInserted(newIndex, data);
        }
    }
}
