package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import android.os.Bundle;

import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;


public class RoiDataList extends DataList<RoiData> {
    private MarkerDataModel markerDataModel;

    public RoiDataList(MarkerDataModel model) {
        this.markerDataModel = model;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] frameIds = new int[size()];
        float[] lefts = new float[size()];
        float[] tops = new float[size()];
        float[] widths = new float[size()];
        float[] heights = new float[size()];

        for (int i = 0; i < size(); i++) {
            RoiData data = getDataAt(i);
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

    @Override
    public void fromBundle(Bundle bundle) {
        clear();
        int[] frameIds = bundle.getIntArray("runIds");
        float[] lefts = bundle.getFloatArray("lefts");
        float[] tops = bundle.getFloatArray("tops");

        float[] widths = bundle.getFloatArray("widths");
        float[] heights = bundle.getFloatArray("heights");

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
            data.setBtmRight(new PointF(left + width, top + height));
            data.setBtmLeft(new PointF(left, top + height));
            addData(data, false);
        }
    }

    @Override
    public void sortXAscending() {

    }

    @Override
    public void sortYAscending() {

    }

    public int getCurrentFrame() {
        return markerDataModel.getSelectedMarkerData();
    }
}
