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

    public float getX(int index) {
        float time = (float)data.getBunchSize() / sampleRate * index;
        return time;
    }

    public float[] getY(int index) {
        return data.getBunch(index);
    }

    public int findIndex(float xValue) {
        return (int)(xValue * sampleRate / data.getBunchSize());
    }

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.getBunchCount();
    }
}