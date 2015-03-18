/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.os.AsyncTask;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;

import java.io.*;
import java.nio.channels.Channels;
import java.util.Arrays;


public class FrequencyMapLoaderFactory {
    final static int MAX_IN_MEMORY_FILE_SIZE = 2 * 1024 * 1024;
    static IFrequencyMapLoader create(AudioFrequencyMapAdapter audioFrequencyMapAdapter, File wavFile) {
        if (wavFile.length() > MAX_IN_MEMORY_FILE_SIZE)
            return new MemoryFrequencyMapLoader(audioFrequencyMapAdapter);

        return new FileFrequencyMapLoader(audioFrequencyMapAdapter);
    }
}

interface IFrequencyMapLoader {
    interface IFrequenciesUpdatedListener {
        void onFrequenciesUpdated(boolean canceled);
    }

    void loadWavFile(AudioWavInputStream audioWavInputStream, Runnable onLoadedCallback);
    void updateFrequencies(Context context, float stepFactor, int windowSize, IFrequenciesUpdatedListener listener);
    void release();
}

class MemoryFrequencyMapLoader implements IFrequencyMapLoader {
    private float[] amplitudes = null;
    final private AudioFrequencyMapAdapter audioFrequencyMapAdapter;

    AsyncTask<Void, DataContainer, Void> updateAsyncTask = null;

    public MemoryFrequencyMapLoader(AudioFrequencyMapAdapter audioFrequencyMapAdapter) {
        this.audioFrequencyMapAdapter = audioFrequencyMapAdapter;
    }

    class DataContainer {
        public float[] data;
        public DataContainer(float[] data) {
            this.data = data;
        }
    }

    @Override
    public void loadWavFile(final AudioWavInputStream audioWavInputStream, final Runnable onLoadedCallback) {
        AsyncTask<Void, DataContainer, Void> asyncTask = new AsyncTask<Void, DataContainer, Void>() {
            float[] data;
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final int size = audioWavInputStream.getSize() / AudioWavInputStream.BYTES_PER_SAMPLE;
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);
                    final byte[] convertByteBuffer = new byte[2];
                    final float[] convertFloatBuffer = new float[1];
                    data = new float[size];
                    for (int i = 0; i < size; i++) {
                        data[i] = AudioWavInputStream.readFloatAmplitude(bufferedInputStream, convertByteBuffer,
                                convertFloatBuffer);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                amplitudes = data;

                onLoadedCallback.run();
            }
        };
        asyncTask.execute();
    }

    @Override
    public void updateFrequencies(final Context context, final float stepFactor, final int windowSize,
                                  final IFrequenciesUpdatedListener listener) {
        // old task running? cancel and return, new job is triggered afterwards
        if (updateAsyncTask != null) {
            updateAsyncTask.cancel(false);
            return;
        }

        audioFrequencyMapAdapter.clear();
        audioFrequencyMapAdapter.setStepFactor(stepFactor);

        final boolean useRenderScript = true;

        updateAsyncTask = new AsyncTask<Void, DataContainer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                float[] frequencies;
                if (useRenderScript) {
                    final FourierRenderScript fourierRenderScript = new FourierRenderScript(context);
                    frequencies = fourierRenderScript.renderScriptFFT(amplitudes, amplitudes.length, windowSize,
                            stepFactor);
                    fourierRenderScript.release();
                } else
                    frequencies = Fourier.transform(amplitudes, windowSize, stepFactor);

                int freqSampleSize = windowSize / 2;
                for (int i = 0; i < frequencies.length; i += freqSampleSize) {
                    final float[] bunch = Arrays.copyOfRange(frequencies, i, i + freqSampleSize);
                    if (isCancelled())
                        return null;
                    publishProgress(new DataContainer(bunch));
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(DataContainer... values) {
                audioFrequencyMapAdapter.addData(values[0].data);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                onFinished(listener, false);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                onFinished(listener, true);
            }
        };
        updateAsyncTask.execute();
    }

    @Override
    public void release() {
        amplitudes = null;
    }

    private void onFinished(IFrequenciesUpdatedListener listener, boolean canceled) {
        updateAsyncTask = null;
        listener.onFrequenciesUpdated(canceled);
    }
}

class FileFrequencyMapLoader implements IFrequencyMapLoader {
    final private AudioFrequencyMapAdapter audioFrequencyMapAdapter;
    private AudioWavInputStream audioWavInputStream;
    private File frequencyFile = null;
    AsyncTask<Void, File, Void> updateAsyncTask = null;

    public FileFrequencyMapLoader(AudioFrequencyMapAdapter audioFrequencyMapAdapter) {
        this.audioFrequencyMapAdapter = audioFrequencyMapAdapter;
    }

    @Override
    public void loadWavFile(AudioWavInputStream audioWavInputStream, Runnable onLoadedCallback) {
        this.audioWavInputStream = audioWavInputStream;
        onLoadedCallback.run();
    }

    @Override
    public void updateFrequencies(final Context context, final float stepFactor, final int windowSize,
                                  final IFrequenciesUpdatedListener listener) {
        // old task running? cancel and return, new job is triggered afterwards
        if (updateAsyncTask != null) {
            updateAsyncTask.cancel(false);
            return;
        }

        audioFrequencyMapAdapter.clear();
        audioFrequencyMapAdapter.setStepFactor(stepFactor);

        updateAsyncTask = new AsyncTask<Void, File, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                File outFile = null;
                try {
                    outFile = convertToFourier(context, audioWavInputStream, windowSize, stepFactor);
                } catch (IOException e) {
                    e.printStackTrace();
                    publishProgress(new File[0]);
                }

                publishProgress(outFile);
                return null;
            }

            @Override
            protected void onProgressUpdate(File... values) {
                if (values.length == 0)
                    return;

                try {
                    audioFrequencyMapAdapter.setDataFile(values[0], windowSize);
                    listener.onFrequenciesUpdated(false);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                listener.onFrequenciesUpdated(true);
            }

            private File convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                         float stepFactor) throws IOException {
                File outputDir = context.getCacheDir();
                File outputFile = File.createTempFile("FrequencyData", ".freq", outputDir);

                if (!convertToFourier(context, audioWavInputStream, windowSize, stepFactor, outputFile)) {
                    outputFile.delete();
                    outputFile = null;
                }

                return outputFile;
            }

            private boolean convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                         float stepFactor, File outFile) throws IOException {
                final FourierRenderScript fourierRenderScript = new FourierRenderScript(context);

                BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);

                final int maxBunchSize = 1024 * 1024;
                final int stepWidth =  (int)(stepFactor * windowSize);
                final int maxSteps = (maxBunchSize - windowSize) / stepWidth + 1;
                // choose the bunch size that big that it fits all the steps
                final int bunchSize =  (maxSteps - 1) * stepWidth + windowSize;
                final float[] buffer = new float[bunchSize];

                final int totalSize = audioWavInputStream.getSize() / AudioWavInputStream.BYTES_PER_SAMPLE;
                final int overlap = windowSize - stepWidth;
                final float[] overlapBuffer = new float[overlap];
                final byte[] convertByteBuffer = new byte[2];

                DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(outFile)));
                try {
                    for (int i = 0; i < totalSize;) {
                        if (isCancelled())
                            return false;

                        final int remainingData = totalSize - i;
                        if (remainingData < windowSize)
                            break;
                        int nRead = Math.min(remainingData, bunchSize);
                        i += nRead - overlap;

                        int a = 0;
                        if (i > 0) {
                            // copy the part that we already read
                            System.arraycopy(buffer, buffer.length - overlap, overlapBuffer, 0, overlap);
                            System.arraycopy(overlapBuffer, 0, buffer, 0, overlap);
                            a = overlap;
                        }
                        for (; a < nRead; a++) {
                            buffer[a] = AudioWavInputStream.readFloatAmplitude(bufferedInputStream, convertByteBuffer,
                                    overlapBuffer);
                        }

                        final float[] frequencies = fourierRenderScript.renderScriptFFT(buffer, nRead, windowSize,
                                stepFactor);
                        for (a = 0; a < frequencies.length; a++)
                            outputStream.writeFloat(frequencies[a]);
                    }
                } finally {
                    outputStream.close();
                }
                return true;
            }
        };
        updateAsyncTask.execute();
    }

    @Override
    public void release() {
        deleteTmpFile();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        release();
    }

    private void deleteTmpFile() {
        if (frequencyFile == null)
            return;
        frequencyFile.delete();
        frequencyFile = null;
    }
}



class FrequencyFileReader implements AudioFrequencyMapAdapter.IDataBackend {
    final RandomAccessFile file;
    final long fileLength;
    final int bunchSize;

    final static int FLOAT_BYTES = 4;

    public FrequencyFileReader(File file, int windowSize) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "r");
        this.fileLength = file.length();
        this.bunchSize = windowSize / 2;
    }

    private FrequencyFileReader(RandomAccessFile file, long fileLength, int bunchSize) {
        this.file = file;
        this.fileLength = fileLength;
        this.bunchSize = bunchSize;
    }

    @Override
    public void clear() {

    }

    @Override
    public void add(float[] frequencies) {

    }

    @Override
    public int getBunchSize() {
        return bunchSize;
    }

    @Override
    public float[] getBunch(int index) {
        float[] buffer = new float[bunchSize];
        try {
            file.seek(index * FLOAT_BYTES * bunchSize);

            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(Channels.newInputStream(
                    file.getChannel())));

            for (int i = 0; i < bunchSize; i++)
                buffer[i] = dataInputStream.readFloat();

            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getBunchCount() {
        return (int)(fileLength / FLOAT_BYTES) / bunchSize;
    }

    @Override
    public AudioFrequencyMapAdapter.IDataBackend clone() {
        return new FrequencyFileReader(file, fileLength, bunchSize);
    }
}

