/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.views.plotview.AbstractPlotDataAdapter;

import java.util.ArrayList;
import java.util.List;


class FixSizedBunchArray {
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

    public float get(int index) {
        float[] data = list.get(index / bunchSize);
        return data[index % bunchSize];
    }

    public int getSize() {
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

public class AudioAmplitudePlotDataAdapter extends AbstractPlotDataAdapter {
    private FixSizedBunchArray data = null;
    private float amplitudeMax = 65535;
    private int sampleRate = 44100;

    public void addData(float amplitudes[]) {
        if (data == null)
            data = new FixSizedBunchArray(amplitudes.length);

        int oldSize = data.getSize();

        data.add(amplitudes);

        notifyDataAdded(oldSize, amplitudes.length);
    }

    public void clear() {
        data.clear();
        notifyAllDataChanged();
    }

    public AudioAmplitudePlotDataAdapter clone() {
        AudioAmplitudePlotDataAdapter adapter = new AudioAmplitudePlotDataAdapter();
        adapter.data = new FixSizedBunchArray(data);
        return adapter;
    }

    public float getX(int index) {
        return (float)index / sampleRate;
    }

    public float getY(int index) {
        return data.get(index) / amplitudeMax;
    }

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.getSize();
    }
}
