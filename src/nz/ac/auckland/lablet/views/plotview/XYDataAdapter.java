/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import java.util.ArrayList;
import java.util.List;


public class XYDataAdapter extends AbstractXYDataAdapter {
    // for the clone
    final private int startIndex;
    private List<Float> xValues = new ArrayList<>();
    private List<Float> yValues = new ArrayList<>();

    public XYDataAdapter() {
        this.startIndex = 0;
    }

    public XYDataAdapter(int startIndex) {
        this.startIndex = startIndex;
    }

    public void addData(Float xValue, Float yValue) {
        int newIndex = getSize();
        xValues.add(xValue);
        yValues.add(yValue);

        notifyDataAdded(newIndex, 1);
    }

    public void clear() {
        xValues.clear();
        yValues.clear();

        notifyAllDataChanged();
    }

    @Override
    public float getX(int index) {
        return xValues.get(index - startIndex);
    }

    @Override
    public float getY(int index) {
        return yValues.get(index - startIndex);
    }

    @Override
    public int getSize() {
        return startIndex + xValues.size();
    }

    @Override
    public CloneablePlotDataAdapter clone(Region1D region) {
        int start = region.getMin();
        if (start > 0)
            start--;
        int end = region.getMax();

        XYDataAdapter clone = new XYDataAdapter(start);

        clone.xValues = new ArrayList<>(xValues.subList(start, end + 1));
        clone.yValues = new ArrayList<>(yValues.subList(start, end + 1));
        return clone;
    }
}
