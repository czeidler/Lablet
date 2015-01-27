/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import nz.ac.auckland.lablet.views.plotview.*;

import java.util.*;


class FixSizedBunchArray extends AbstractList<Float> {
    final public List<float[]> list;
    final public int bunchSize;
    public int size = 0;

    public FixSizedBunchArray(int bunchSize) {
        this.list = new ArrayList<>();
        this.bunchSize = bunchSize;
    }

    // creates a shallow copy
    public FixSizedBunchArray(FixSizedBunchArray array) {
        this.list = new ArrayList<>(array.list);
        this.bunchSize = array.bunchSize;
        this.size = array.size;
    }

    public void add(float[] data) {
        list.add(data);
        size += bunchSize;
    }

    public void clear() {
        list.clear();
        size = 0;
    }

    @Override
    public Iterator<Float> iterator() {
        return new Iterator<Float>() {
            int position = 0;
            @Override
            public boolean hasNext() {
                return position < size();
            }

            @Override
            public Float next() {
                float value = get(position);
                position++;
                return value;
            }

            @Override
            public void remove() {
                throw new RuntimeException("not allowed");
            }
        };
    }

    @Override
    public Float get(int index) {
        float[] data = list.get(index / bunchSize);
        return data[index % bunchSize];
    }

    @Override
    public int size() {
        return size;
    }

    public float[] getBunch(int bunchIndex) {
        return list.get(bunchIndex);
    }

    public int getBunchCount() {
        return list.size();
    }

    public int getBunchSize() {
        return bunchSize;
    }
}

public class AudioAmplitudePlotDataAdapter extends AbstractXYDataAdapter {
    private FixSizedBunchArray data = null;
    private float amplitudeMax = 65535;
    private int sampleRate = 44100;

    public void addData(float amplitudes[]) {
        if (amplitudes.length == 0)
            return;
        int index = 0;
        if (data == null)
            data = new FixSizedBunchArray(amplitudes.length);
        else
            index = data.size();

        data.add(amplitudes);

        notifyDataAdded(index, amplitudes.length);
    }

    public void clear() {
        if (data == null || data.size() == 0)
            return;
        data.clear();
        notifyAllDataChanged();
    }

    public AudioAmplitudePlotDataAdapter clone(Region1D region) {
        AudioAmplitudePlotDataAdapter adapter = new AudioAmplitudePlotDataAdapter();
        if (data != null)
            adapter.data = new FixSizedBunchArray(data);
        return adapter;
    }

    @Override
    public Number getX(int index) {
        return (float)index / sampleRate * 1000;
    }

    @Override
    public Number getY(int index) {
        return data.get(index) / amplitudeMax;
    }

    @Override
    public Range getRange(Number leftReal, Number rightReal) {
        if (data == null)
            return new Range(0, -1);

        int leftIndex = Math.round(sampleRate * leftReal.floatValue() / 1000);
        int rightIndex = Math.round(sampleRate * rightReal.floatValue() / 1000);
        leftIndex -= 1;
        rightIndex ++;
        if (leftIndex < 0)
            leftIndex = 0;
        int size = data.size();
        if (rightIndex >= size)
            rightIndex = size - 1;

        return new Range(leftIndex, rightIndex);
    }

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.size();
    }

    public int getTotalTime() {
        return getSize() * 1000 / sampleRate;
    }

    @Override
    public DataStatistics createDataStatistics() {
        return new XYDataStatistics(this, true);
    }
}
