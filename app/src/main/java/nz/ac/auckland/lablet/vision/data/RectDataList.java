package nz.ac.auckland.lablet.vision.data;

import android.graphics.PointF;
import android.os.Bundle;

import nz.ac.auckland.lablet.views.marker.AbstractPointDataList;


public class RectDataList extends AbstractPointDataList<RectData> {
    private boolean visibility;

    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] frameIds = new int[size()];
        float[] centreXs = new float[size()];
        float[] centreYs = new float[size()];
        float[] widths = new float[size()];
        float[] heights = new float[size()];
        boolean visibility = this.isVisible();

        for (int i = 0; i < size(); i++) {
            RectData data = getAt(i);
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
            addData(data);
        }
    }

    @Override
    public PointF getPosition(int index) {
        return getAt(index).getCentre();
    }

    @Override
    public void setPositionNoNotify(PointF point, int index) {
        getAt(index).setCentre(point);
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
        notifyAllDataChanged();
    }

    public boolean isVisible() {
        return visibility;
    }
}
