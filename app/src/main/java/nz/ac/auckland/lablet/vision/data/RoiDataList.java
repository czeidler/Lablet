/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import android.os.Bundle;

import nz.ac.auckland.lablet.views.marker.AbstractPointDataList;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;

import java.util.Collections;
import java.util.Comparator;


public class RoiDataList extends AbstractPointDataList<RoiData> {
    private MarkerDataModel markerDataModel;
    private MarkerDataModel.IListener listener = new MarkerDataModel.IListener() {
        @Override
        public void onDataAdded(MarkerDataModel model, int index) {

        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
            RoiData roiData = getDataByFrameId(data.getId());
            if (roiData == null)
                return;
            removeData(roiData);
        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {
            int roiIndex = getIndexByFrameId(model.getAt(index).getId());
            if (roiIndex < 0)
                return;
            getAt(roiIndex).centerOnMarker();
            notifyDataChanged(roiIndex, 1);
        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {

        }
    };

    public RoiDataList(MarkerDataModel model) {
        this.markerDataModel = model;
        this.markerDataModel.addListener(listener);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] frameIds = new int[size()];
        float[] lefts = new float[size()];
        float[] tops = new float[size()];
        float[] widths = new float[size()];
        float[] heights = new float[size()];

        for (int i = 0; i < size(); i++) {
            RoiData data = getAt(i);
            frameIds[i] = data.getFrameId();
            lefts[i] = data.getLeft();
            tops[i] = data.getTop();
            widths[i] = data.getWidth();
            heights[i] = data.getHeight();
        }

        bundle.putIntArray("runIds", frameIds);

        bundle.putFloatArray("lefts", lefts);
        bundle.putFloatArray("tops", tops);

        bundle.putFloatArray("widths", widths);
        bundle.putFloatArray("heights", heights);

        return bundle;
    }

    public void fromBundle(Bundle bundle) {
        clear();
        int[] frameIds = bundle.getIntArray("runIds");
        float[] lefts = bundle.getFloatArray("lefts");
        float[] tops = bundle.getFloatArray("tops");

        float[] widths = bundle.getFloatArray("widths");
        float[] heights = bundle.getFloatArray("heights");

        if (frameIds == null || lefts == null || tops == null || widths == null || heights == null)
            return;
        if (frameIds.length != lefts.length || frameIds.length != tops.length || frameIds.length != widths.length
                || frameIds.length != heights.length)
            return;

        for (int i = 0; i < frameIds.length; i++) {
            float left = lefts[i];
            float top = tops[i];
            float width = widths[i];
            float height = heights[i];

            MarkerData markerData = markerDataModel.getMarkerDataById(frameIds[i]);
            RoiData data = new RoiData(markerData);
            data.setTopLeft(new PointF(left, top));
            data.setTopRight(new PointF(left + width, top));
            data.setBtmRight(new PointF(left + width, top - height));
            data.setBtmLeft(new PointF(left, top - height));
            addData(data);
        }
    }

    @Override
    public int addData(RoiData data) {
        super.addDataNoNotify(data);
        Collections.sort(list, new Comparator<RoiData>() {
            @Override
            public int compare(RoiData roiData, RoiData t1) {
                return new Integer(roiData.getFrameId()).compareTo(t1.getFrameId());
            }
        });
        int index = indexOf(data);
        notifyDataAdded(index);
        notifyAllDataChanged();
        return index;
    }

    @Override
    public PointF getPosition(int index) {
        return getAt(index).getMarkerData().getPosition();
    }

    @Override
    public void setPositionNoNotify(PointF point, int index) {
        // ignore
    }

    public int getIndexByFrameId(int frameId) {
        for (int i = 0; i < size(); i++) {
            RoiData data = getAt(i);
            if (data.getFrameId() == frameId)
                return i;
        }
        return -1;
    }

    public RoiData getDataByFrameId(int frameId) {
        return getAt(getIndexByFrameId(frameId));
    }
}
