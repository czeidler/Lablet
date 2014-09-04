/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import nz.ac.auckland.lablet.views.plotview.CloneablePlotDataAdapter;
import nz.ac.auckland.lablet.views.plotview.Region1D;


public class AudioFrequencyMapAdapter extends CloneablePlotDataAdapter {
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

    public AudioFrequencyMapAdapter clone(Region1D region) {
        AudioFrequencyMapAdapter adapter = new AudioFrequencyMapAdapter();
        if (data != null)
            adapter.data = new FixSizedBunchArray(data);
        return adapter;
    }

    // return time in milli seconds
    public float getX(int index) {
        float time = (float)data.getBunchSize() / 2 / sampleRate * index;
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
}