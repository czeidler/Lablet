package nz.ac.auckland.lablet.data;

import android.graphics.PointF;
import android.os.Bundle;

/**
 * Created by Jamie on 26/08/2015.
 */
public class RectDataList extends DataList<RectData> {

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] runIds = new int[getDataCount()];
        float[] centreXs = new float[getDataCount()];
        float[] centreYs = new float[getDataCount()];
        float[] widths = new float[getDataCount()];
        float[] heights = new float[getDataCount()];
        boolean[] visibilities = new boolean[getDataCount()];

        for (int i = 0; i < getDataCount(); i++) {
            RectData data = getDataAt(i);
            runIds[i] = data.getFrameId();
            centreXs[i] = data.getCentre().x;
            centreXs[i] = data.getCentre().y;
            widths[i] = data.getWidth();
            heights[i] = data.getHeight();
            visibilities[i] = data.isVisible();
        }
        bundle.putIntArray("runIds", runIds);
        bundle.putFloatArray("centreXs", centreXs);
        bundle.putFloatArray("centreYs", centreYs);
        bundle.putFloatArray("widths", widths);
        bundle.putFloatArray("heights", heights);
        bundle.putBooleanArray("visibilities", visibilities);

        return bundle;
    }

    @Override
    public void fromBundle(Bundle bundle) {
        clear();
        int[] runIds = bundle.getIntArray("runIds");
        float[] centreXs = bundle.getFloatArray("centreXs");
        float[] centreYs = bundle.getFloatArray("centreYs");
        float[] widths = bundle.getFloatArray("widths");
        float[] heights = bundle.getFloatArray("heights");
        boolean[] visibilities = bundle.getBooleanArray("visibilities");

        for (int i = 0; i < runIds.length; i++) {
            RectData data = new RectData(runIds[i]);
            data.setCentre(new PointF(centreXs[i], centreYs[i]));
            data.setWidth(widths[i]);
            data.setHeight(heights[i]);
            data.setVisible(visibilities[i]);
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
