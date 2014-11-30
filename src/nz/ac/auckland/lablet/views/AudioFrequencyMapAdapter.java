/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.views.plotview.CloneablePlotDataAdapter;
import nz.ac.auckland.lablet.views.plotview.DataStatistics;
import nz.ac.auckland.lablet.views.plotview.Range;
import nz.ac.auckland.lablet.views.plotview.Region1D;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AudioFrequencyMapAdapter extends CloneablePlotDataAdapter {
    private FixSizedBunchArray data = null;
    private int sampleRate = 44100;
    private float stepFactor;

    public AudioFrequencyMapAdapter(float stepFactor) {
        setStepFactor(stepFactor);
    }

    /**
     * Sets the step factor for the sampling window overlap.
     *
     * A step factor of 0.5 starts the next window at the half of the previous window. A factor of 1 starts it exactly
     * after the previous window.
     *
     * @param stepFactor should be between 0.01 and 1
     */
    public void setStepFactor(float stepFactor) {
        if (stepFactor > 1)
            stepFactor = 1;
        else if (stepFactor < 0.01f)
            stepFactor = 0.01f;

        this.stepFactor = stepFactor;
    }

    public float getStepFactor() {
        return stepFactor;
    }

    public void clear() {
        if (data != null) {
            data.clear();
            data = null;
        }
        notifyAllDataChanged();
    }

    public Range getRange(float leftReal, float rightReal) {
        if (data == null)
            return new Range(0, -1);

        Comparator<Float> numberComparator =  new Comparator<Float>() {
            @Override
            public int compare(Float number1, Float number2) {
                return Float.compare(number1, number2);
            }
        };

        List<Float> timeListHelper = new AbstractList<Float>() {
            @Override
            public Float get(int i) {
                return getX(i);
            }

            @Override
            public int size() {
                return getSize();
            }
        };

        int leftIndex = Math.abs(Collections.binarySearch(timeListHelper, leftReal, numberComparator));
        int rightIndex = Math.abs(Collections.binarySearch(timeListHelper, rightReal, numberComparator));

        leftIndex -= 2;
        rightIndex ++;
        if (leftIndex < 0)
            leftIndex = 0;
        int size = getSize();
        if (rightIndex >= size)
            rightIndex = size - 1;

        return new Range(leftIndex, rightIndex);
    }

    public void addData(float frequencies[]) {
        if (data == null)
            data = new FixSizedBunchArray(frequencies.length);

        int oldSize = data.getBunchCount();
        data.add(frequencies);

        notifyDataAdded(oldSize, 1);
    }

    public AudioFrequencyMapAdapter clone(Region1D region) {
        AudioFrequencyMapAdapter adapter = new AudioFrequencyMapAdapter(stepFactor);
        if (data != null)
            adapter.data = new FixSizedBunchArray(data);
        return adapter;
    }

    // returns time in milli seconds
    public float getX(int index) {
        // bunch size is half the window size so multiply it by 2
        float time = (float)(data.getBunchSize() * 2) / sampleRate * index * stepFactor;
        return time * 1000;
    }

    public float[] getY(int index) {
        return data.getBunch(index);
    }

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.getBunchCount();
    }

    @Override
    public DataStatistics createDataStatistics() {
        return null;
    }
}