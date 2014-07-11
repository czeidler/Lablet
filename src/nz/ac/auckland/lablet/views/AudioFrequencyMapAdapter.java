/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.views.plotview.AbstractPlotDataAdapter;


public class AudioFrequencyMapAdapter extends AbstractPlotDataAdapter {
    private FixSizedBunchArray data = null;
    private int sampleRate = 44100;

    public void clear() {
        if (data != null)
            data.clear();
        notifyAllDataChanged();
    }

    public void addData(float frequencies[]) {
        if (data == null)
            data = new FixSizedBunchArray(frequencies.length);

        int oldSize = data.getBunchCount();
        data.add(frequencies);

        notifyDataAdded(oldSize, 1);
    }

    public AudioFrequencyMapAdapter clone() {
        AudioFrequencyMapAdapter adapter = new AudioFrequencyMapAdapter();
        if (data != null)
            adapter.data = new FixSizedBunchArray(data);
        return adapter;
    }

    public float getX(int index) {
        float time = (float)data.getBunchSize() / sampleRate * index;
        return time;
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
}