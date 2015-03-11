/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.graphics.RectF;
import nz.ac.auckland.lablet.views.plotview.*;

import java.io.File;
import java.io.FileNotFoundException;


public class AudioFrequencyMapAdapter extends CloneablePlotDataAdapter {
    public interface IDataBackend {
        void clear();
        void add(float frequencies[]);
        int getBunchSize();
        float[] getBunch(int index);
        int getBunchCount();
        IDataBackend clone();
    }

    static private class MemoryBackend implements IDataBackend {
        FixSizedBunchArray data;

        public MemoryBackend(int bunchSize) {
            data = new FixSizedBunchArray(bunchSize);
        }

        private MemoryBackend(MemoryBackend parent) {
            data = new FixSizedBunchArray(parent.data);
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public void add(float[] frequencies) {
            data.add(frequencies);
        }

        @Override
        public int getBunchSize() {
            return data.getBunchSize();
        }

        @Override
        public float[] getBunch(int index) {
            return data.getBunch(index);
        }

        @Override
        public int getBunchCount() {
            return data.getBunchCount();
        }

        @Override
        public IDataBackend clone() {
            return new MemoryBackend(this);
        }
    }

    private class DiscardMemoryBackend implements IDataBackend {
        FixSizedBunchArray data;
        int discardedBunches = 0;

        public DiscardMemoryBackend(int bunchSize) {
            data = new FixSizedBunchArray(bunchSize);
        }

        private DiscardMemoryBackend(DiscardMemoryBackend parent) {
            data = new FixSizedBunchArray(parent.data);
            discardedBunches = parent.discardedBunches;
        }

        @Override
        public void clear() {
            data.clear();
            discardedBunches = 0;
        }

        @Override
        public void add(float[] frequencies) {
            float validTime = getX(data.getBunchCount());
            if (validTime > discardDataTime * 1.5) {
                // discard data
                float bunchRate = getBunchRate();
                int bunches = (int)Math.ceil(discardDataTime * bunchRate / 1000);
                int bunchesToDiscard = data.getBunchCount() - bunches;
                if (bunchesToDiscard > 0) {
                    for (int i = 0; i < bunchesToDiscard; i++)
                        data.removeBunch(0);
                    discardedBunches += bunchesToDiscard;
                }
            }
            data.add(frequencies);
        }

        @Override
        public int getBunchSize() {
            return data.getBunchSize();
        }

        @Override
        public float[] getBunch(int index) {
            return data.getBunch(index - discardedBunches);
        }

        @Override
        public int getBunchCount() {
            return discardedBunches + data.getBunchCount();
        }

        @Override
        public IDataBackend clone() {
            return new DiscardMemoryBackend(this);
        }
    }

    private IDataBackend data = null;
    private int sampleRate = 44100;
    private float stepFactor;

    private int discardDataTime = -1;

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

    /**
     * If set the data kept in memory is only discardDataTime long.
     *
     * This can, for example, be used to only display the last few seconds while recording.
     * @param discardDataTime
     */
    public void setDiscardDataTime(int discardDataTime) {
        this.discardDataTime = discardDataTime;
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
        leftIndex -= 1;
        rightIndex ++;
        if (leftIndex < 0)
            leftIndex = 0;
        int size = data.getBunchSize();
        if (rightIndex >= size)
            rightIndex = size - 1;

        return new Range(leftIndex, rightIndex);
    }

    public void addData(float frequencies[]) {
        if (data == null) {
            if (discardDataTime < 0)
                data = new MemoryBackend(frequencies.length);
            else
                data = new DiscardMemoryBackend(frequencies.length);
        }

        int oldSize = data.getBunchCount();
        data.add(frequencies);

        notifyDataAdded(oldSize, 1);
    }

    public void setDataFile(File file, int windowSize) throws FileNotFoundException {
        if (data != null)
            data.clear();
        data = new FrequencyFileReader(file, windowSize);
        notifyAllDataChanged();
    }

    public AudioFrequencyMapAdapter clone(Region1D region) {
        AudioFrequencyMapAdapter adapter = new AudioFrequencyMapAdapter(stepFactor);
        if (data != null)
            adapter.data = data.clone();
        return adapter;
    }

    // returns time in milli seconds
    public float getX(int index) {
        // bunch size is half the window size so multiply it by 2
        float time = (float)(data.getBunchSize() * 2) / sampleRate * index * stepFactor;
        return time * 1000;
    }

    /**
     *
     * @return bunches / s
     */
    private float getBunchRate() {
        return sampleRate / (data.getBunchSize() * 2 * stepFactor);
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