package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import android.os.Bundle;

import nz.ac.auckland.lablet.experiment.FrameDataModel;

/**
 * Created by jdip004 on 27/08/2015.
 */
public class RoiDataList extends DataList<RoiData> {

    private FrameDataModel frameDataList;

    public void addFrameDataList(FrameDataModel frameDataList)
    {
        this.frameDataList = frameDataList;
    }

    public FrameDataModel getFrameDataList()
    {
        return frameDataList;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] frameIds = new int[size()];
        float[] centreXs = new float[size()];
        float[] centreYs = new float[size()];
        float[] widths = new float[size()];
        float[] heights = new float[size()];

        for (int i = 0; i < size(); i++) {
            RoiData data = getDataAt(i);
            frameIds[i] = data.getFrameId();
            centreXs[i] = data.getCentre().getPosition().x;
            centreYs[i] = data.getCentre().getPosition().y;
            widths[i] = Math.abs(data.getTopLeft().getPosition().x - data.getTopRight().getPosition().x);
            heights[i] = Math.abs(data.getTopLeft().getPosition().y - data.getBtmLeft().getPosition().y);
        }

        bundle.putIntArray("runIds", frameIds);

        bundle.putFloatArray("centreXs", centreXs);
        bundle.putFloatArray("centreYs", centreYs);

        bundle.putFloatArray("widths", widths);
        bundle.putFloatArray("heights", heights);

        bundle.putBoolean("visibility", this.isVisible());

        return bundle;
    }

    @Override
    public void fromBundle(Bundle bundle) {
        clear();
        int[] frameIds = bundle.getIntArray("runIds");
        float[] centreXs = bundle.getFloatArray("centreXs");
        float[] centreYs = bundle.getFloatArray("centreYs");

        float[] widths = bundle.getFloatArray("widths");
        float[] heights = bundle.getFloatArray("heights");

        boolean visibility = bundle.getBoolean("visibility");
        this.setVisibility(visibility);

        for (int i = 0; i < frameIds.length; i++) {
            float centreX = centreXs[i];
            float centreY = centreYs[i];
            float width = widths[i] / 2;
            float height = heights[i] / 2;

            RoiData data = new RoiData(frameIds[i]);
            data.setCentre(new PointF(centreX, centreY));
            data.setTopLeft(new PointF(centreX - width, centreY + height));
            data.setTopRight(new PointF(centreX + width, centreY + height));
            data.setBtmRight(new PointF(centreX + width, centreY - height));
            data.setBtmLeft(new PointF(centreX - width, centreY - height));
            addData(data, false);
        }
    }

    @Override
    public void sortXAscending() {

    }

    @Override
    public void sortYAscending() {

    }
}
