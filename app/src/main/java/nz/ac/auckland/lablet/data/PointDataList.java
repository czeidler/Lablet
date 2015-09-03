/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.data;

import android.graphics.PointF;
import android.os.Bundle;

import java.util.Comparator;


public class PointDataList extends DataList<PointData> {

    public PointF getRealMarkerPositionAt(int index) {
        PointData data = getDataAt(index);
        return data.getPosition();
    }

    public void setMarkerPosition(PointF position, int index) {
        PointData data = getDataAt(index);
        data.setPosition(position);
        notifyDataChanged(index, 1);
    }

    public void sortXAscending() {
        sort(new Comparator<PointData>() {
            @Override
            public int compare(PointData pointData, PointData pointData2) {
                return (int)(pointData.getPosition().x - pointData2.getPosition().x);
            }
        });
    }

    public void sortYAscending() {
        sort(new Comparator<PointData>() {
            @Override
            public int compare(PointData pointData, PointData pointData2) {
                return (int) (pointData.getPosition().y - pointData2.getPosition().y);
            }
        });
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        int[] runIds = new int[size()];
        float[] xPositions = new float[size()];
        float[] yPositions = new float[size()];
        for (int i = 0; i < size(); i++) {
            PointData data = getDataAt(i);
            runIds[i] = data.getFrameId();
            xPositions[i] = data.getPosition().x;
            yPositions[i] = data.getPosition().y;
        }
        bundle.putIntArray("runIds", runIds);
        bundle.putFloatArray("xPositions", xPositions);
        bundle.putFloatArray("yPositions", yPositions);
        bundle.putBoolean("visibility", this.visibility);
        return bundle;
    }

    public void fromBundle(Bundle bundle) {
        clear();
        int[] runIds = bundle.getIntArray("runIds");
        float[] xPositions = bundle.getFloatArray("xPositions");
        float[] yPositions = bundle.getFloatArray("yPositions");

        if (runIds != null && xPositions != null && yPositions != null && runIds.length == xPositions.length
                && xPositions.length == yPositions.length) {
            for (int i = 0; i < runIds.length; i++) {
                PointData data = new PointData(runIds[i]);
                data.getPosition().set(xPositions[i], yPositions[i]);
                addData(data, false);
            }
        }
    }
}