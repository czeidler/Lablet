package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import android.os.Bundle;

import nz.ac.auckland.lablet.experiment.FrameDataModel;

/**
 * Created by Jamie on 26/08/2015.
 */
public class RectDataList extends DataList<RectData> {

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
        boolean visibility = this.isVisible();

        for (int i = 0; i < size(); i++) {
            RectData data = getDataAt(i);
            frameIds[i] = data.getFrameId();
            centreXs[i] = data.getCentre().x;
            centreYs[i] = data.getCentre().y;
            widths[i] = data.getWidth();
            heights[i] = data.getHeight();
        }
        bundle.putIntArray("runIds", frameIds);
        bundle.putFloatArray("centreXs", centreXs);
        bundle.putFloatArray("centreYs", centreYs);
        bundle.putFloatArray("widths", widths);
        bundle.putFloatArray("heights", heights);
        bundle.putBoolean("visibility", visibility);

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
            RectData data = new RectData(frameIds[i]);
            data.setCentre(new PointF(centreXs[i], centreYs[i]));
            data.setWidth(widths[i]);
            data.setHeight(heights[i]);
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
