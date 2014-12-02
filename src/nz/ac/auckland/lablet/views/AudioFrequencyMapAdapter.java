/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.graphics.RectF;
import nz.ac.auckland.lablet.views.plotview.*;


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

    protected Range getRange(Number leftReal, Number rightReal) {
        if (data == null)
            return new Range(0, -1);

        int leftIndex = Math.round(leftReal.floatValue() / stepFactor * sampleRate / (2 * data.getBunchSize()) / 1000);
        int rightIndex = Math.round(rightReal.floatValue() / stepFactor * sampleRate / (2 * data.getBunchSize()) / 1000);
        leftIndex -= 2;
        rightIndex ++;
        if (leftIndex < 0)
            leftIndex = 0;
        int size = data.size();
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

    class FrequencyMapDataStatistics extends DataStatistics {
        final private AudioFrequencyMapAdapter adapter;

        private AbstractPlotDataAdapter.IListener listener = new AbstractPlotDataAdapter.IListener() {
            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                float first = getX(index);
                float last = getX(index + number - 1);
                if (dataLimits == null) {
                    dataLimits = new RectF(first, sampleRate / 2, last, 0);
                    notifyLimitsChanged();
                    return;
                }
                boolean changed = includePoint(first);
                if (includePoint(last))
                    changed = true;
                if (changed)
                    notifyLimitsChanged();
            }

            private boolean includePoint(float x) {
                RectF oldLimits = new RectF(dataLimits);

                boolean limitsChanged = false;
                if (dataLimits.left > x) {
                    dataLimits.left = x;
                    limitsChanged = true;
                }
                if (dataLimits.right < x) {
                    dataLimits.right = x;
                    limitsChanged = true;
                }

                if (limitsChanged)
                    previousLimits = oldLimits;

                return limitsChanged;
            }

            @Override
            public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
                onAllDataChanged(plot);
            }

            @Override
            public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
                onAllDataChanged(plot);
            }

            @Override
            public void onAllDataChanged(AbstractPlotDataAdapter plot) {
                if (getSize() > 0)
                    onDataAdded(plot, 0, getSize());
            }
        };

        public FrequencyMapDataStatistics(AudioFrequencyMapAdapter adapter) {
            this.adapter = adapter;
            adapter.addListener(listener);
        }

        @Override
        public void release() {

        }

        @Override
        public AbstractPlotDataAdapter getAdapter() {
            return adapter;
        }
    }

    @Override
    public DataStatistics createDataStatistics() {
        return new FrequencyMapDataStatistics(this);
    }
}