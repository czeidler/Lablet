/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class XYDataAdapter extends AbstractXYDataAdapter {
    // for the clone
    final private int startIndex;
    private List<Number> xValues;
    private List<Number> yValues;

    public XYDataAdapter(List<Number> xValues, List<Number> yValues) {
        this.startIndex = 0;
        this.xValues = xValues;
        this.yValues = yValues;
    }

    public XYDataAdapter() {
        this.startIndex = 0;
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
    }

    public XYDataAdapter(int startIndex) {
        this.startIndex = startIndex;
        xValues = new ArrayList<>();
        yValues = new ArrayList<>();
    }

    public void addData(Number xValue, Number yValue) {
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
    public Number getX(int index) {
        return xValues.get(index - startIndex);
    }

    @Override
    public Number getY(int index) {
        return yValues.get(index - startIndex);
    }

    @Override
    public Range getRange(Number leftReal, Number rightReal) {
        Comparator<Number> numberComparator =  new Comparator<Number>() {
            @Override
            public int compare(Number number, Number t1) {
                return Float.compare(number.floatValue(), t1.floatValue());
            }
        };

        int leftIndex = Math.abs(Collections.binarySearch(xValues, leftReal, numberComparator));
        int rightIndex = Math.abs(Collections.binarySearch(xValues, rightReal, numberComparator));

        leftIndex -= 2;
        rightIndex ++;
        if (leftIndex < 0)
            leftIndex = 0;
        int size = xValues.size();
        if (rightIndex >= size)
            rightIndex = size - 1;

        return new Range(leftIndex, rightIndex);
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
