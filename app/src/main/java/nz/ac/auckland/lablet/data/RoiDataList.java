package nz.ac.auckland.lablet.data;

import android.graphics.PointF;
import android.os.Bundle;

/**
 * Created by jdip004 on 27/08/2015.
 */
public class RoiDataList extends DataList<RoiData> {
    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        int[] runIds = new int[getDataCount()];
        float[] topLeftXs = new float[getDataCount()];
        float[] topLeftYs = new float[getDataCount()];

        float[] topRightXs = new float[getDataCount()];
        float[] topRightYs = new float[getDataCount()];

        float[] btmRightXs = new float[getDataCount()];
        float[] btmRightYs = new float[getDataCount()];

        float[] btmLeftXs = new float[getDataCount()];
        float[] btmLeftYs = new float[getDataCount()];

        boolean[] visibilities = new boolean[getDataCount()];

        for (int i = 0; i < getDataCount(); i++) {
            RoiData data = getDataAt(i);
            runIds[i] = data.getFrameId();
            topLeftXs[i] = data.getTopLeft().getPosition().x;
            topLeftYs[i] = data.getTopLeft().getPosition().y;

            topRightXs[i] = data.getTopRight().getPosition().x;
            topRightYs[i] = data.getTopRight().getPosition().y;

            btmRightXs[i] = data.getBtmRight().getPosition().x;
            btmRightYs[i] = data.getBtmRight().getPosition().y;

            btmLeftXs[i] = data.getBtmLeft().getPosition().x;
            btmLeftYs[i] = data.getBtmLeft().getPosition().y;

            visibilities[i] = data.isVisible();
        }

        bundle.putIntArray("runIds", runIds);

        bundle.putFloatArray("topLeftXs", topLeftXs);
        bundle.putFloatArray("topLeftYs", topLeftYs);

        bundle.putFloatArray("topRightXs", topRightXs);
        bundle.putFloatArray("topRightYs", topRightYs);

        bundle.putFloatArray("btmRightXs", btmRightXs);
        bundle.putFloatArray("btmRightYs", btmRightYs);

        bundle.putFloatArray("btmLeftXs", btmLeftXs);
        bundle.putFloatArray("btmLeftYs", btmLeftYs);

        bundle.putBooleanArray("visibilities", visibilities);

        return bundle;
    }

    @Override
    public void fromBundle(Bundle bundle) {
        clear();
        int[] runIds = bundle.getIntArray("runIds");
        float[] topLeftXs = bundle.getFloatArray("topLeftXs");
        float[] topLeftYs = bundle.getFloatArray("topLeftYs");

        float[] topRightXs = bundle.getFloatArray("topRightXs");
        float[] topRightYs = bundle.getFloatArray("topRightYs");

        float[] btmRightXs = bundle.getFloatArray("btmRightXs");
        float[] btmRightYs = bundle.getFloatArray("btmRightYs");

        float[] btmLeftXs = bundle.getFloatArray("btmLeftXs");
        float[] btmLeftYs = bundle.getFloatArray("btmLeftYs");

        boolean[] visibilities = bundle.getBooleanArray("visibilities");

        for (int i = 0; i < runIds.length; i++) {
            RoiData data = new RoiData(runIds[i]);
            data.setTopLeft(new PointF(topLeftXs[i], topLeftYs[i]));
            data.setTopRight(new PointF(topRightXs[i], topRightYs[i]));
            data.setBtmRight(new PointF(btmRightXs[i], btmRightYs[i]));
            data.setBtmLeft(new PointF(btmLeftXs[i], btmLeftYs[i]));
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
