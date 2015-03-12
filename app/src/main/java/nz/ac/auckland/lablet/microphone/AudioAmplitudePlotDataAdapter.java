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
    public int bunchSize = -1;
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
        if (bunchSize == -1)
            bunchSize = data.length;
        if (bunchSize != data.length)
            throw new RuntimeException();
        list.add(data);
        size += bunchSize;
    }

    public void clear() {
        bunchSize = -1;
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

    public void removeBunch(int bunchIndex) {
        list.remove(bunchIndex);
        size -= bunchSize;
    }

    public int getBunchCount() {
        return list.size();
    }

    public int getBunchSize() {
        return bunchSize;
    }
}

public class AudioAmplitudePlotDataAdapter extends AbstractXYDataAdapter {
    private IDataBackend data;
    private float amplitudeMax = 65535;
    private int sampleRate = 44100;

    private int discardDataTime = -1;

    interface IDataBackend {
        int size();
        float get(int index);
        int getBunchSize();
        void add(float[] data);
        void clear();
        IDataBackend clone();
    }

    static class MemoryBackend implements IDataBackend {
        private FixSizedBunchArray data = null;

        public MemoryBackend(int length) {
            data = new FixSizedBunchArray(length);
        }

        private MemoryBackend(MemoryBackend parent) {
            data = new FixSizedBunchArray(parent.data);
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public float get(int index) {
            return data.get(index);
        }

        @Override
        public int getBunchSize() {
            return data.getBunchSize();
        }

        @Override
        public void add(float[] data) {
            this.data.add(data);
        }

        @Override
        public void clear() {
            data.clear();
        }

        @Override
        public IDataBackend clone() {
            return new MemoryBackend(this);
        }
    }

    class DiscardMemoryBackend implements IDataBackend {
        private FixSizedBunchArray data = null;
        int discardedBunches = 0;

        public DiscardMemoryBackend(int length) {
            data = new FixSizedBunchArray(length);
        }

        private DiscardMemoryBackend(DiscardMemoryBackend parent) {
            data = new FixSizedBunchArray(parent.data);
            discardedBunches = parent.discardedBunches;
        }

        private int getDiscardOffset() {
            return data.getBunchSize() * discardedBunches;
        }

        @Override
        public int size() {
            return getDiscardOffset() + data.size();
        }

        @Override
        public float get(int index) {
            return data.get(index - getDiscardOffset());
        }

        @Override
        public int getBunchSize() {
            return data.getBunchSize();
        }

        @Override
        public void add(float[] amplitudes) {
            Number validTime = getX(data.size());
            if (validTime.floatValue() > discardDataTime * 1.5f) {
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

            data.add(amplitudes);
        }

        @Override
        public void clear() {
            data.clear();
            discardedBunches = 0;
        }

        @Override
        public IDataBackend clone() {
            return new DiscardMemoryBackend(this);
        }
    }

    public void addData(float amplitudes[]) {
        if (amplitudes.length == 0)
            return;
        int index = 0;
        if (data == null) {
            if (discardDataTime < 0)
                data = new MemoryBackend(amplitudes.length);
            else
                data = new DiscardMemoryBackend(amplitudes.length);
        } else
            index = data.size();

        data.add(amplitudes);

        notifyDataAdded(index, amplitudes.length);
    }

    public void clear() {
        if (data == null)
            return;
        data.clear();
        data = null;
        notifyAllDataChanged();
    }

    public AudioAmplitudePlotDataAdapter clone(Region1D region) {
        AudioAmplitudePlotDataAdapter adapter = new AudioAmplitudePlotDataAdapter();
        if (data != null)
            adapter.data = data.clone();
        return adapter;
    }

    @Override
    public Number getX(int index) {
        return (float)index / sampleRate * 1000;
    }

    private float getBunchRate() {
        return sampleRate / data.getBunchSize();
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
        if (rightIndex >= size) {
            rightIndex = size - 1;
            if (leftIndex > rightIndex)
                leftIndex = rightIndex - 1;
        }

        return new Range(leftIndex, rightIndex);
    }

    @Override
    public int getSize() {
        if (data == null)
            return 0;
        return data.size();
    }

    public int getTotalTime(int nAmplitudes) {
        return nAmplitudes / sampleRate * 1000;
    }

    public int getTotalTime() {
        return getTotalTime(getSize());
    }

    @Override
    public DataStatistics createDataStatistics() {
        return new XYDataStatistics(this, true);
    }

    public void setDiscardDataTime(int discardDataTime) {
        this.discardDataTime = discardDataTime;
    }
}
