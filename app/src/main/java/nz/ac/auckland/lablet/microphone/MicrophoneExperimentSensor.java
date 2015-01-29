/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.app.Activity;
import android.content.Context;
import android.media.*;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentSensor;
import nz.ac.auckland.lablet.experiment.AbstractExperimentSensorView;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;
import nz.ac.auckland.lablet.misc.AudioWavOutputStream;
import nz.ac.auckland.lablet.misc.StorageLib;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;



public class MicrophoneExperimentSensor extends AbstractExperimentSensor {
    private WeakReference<ISensorDataListener> softListener = null;

    final static public String SENSOR_NAME = "Microphone";

    final public int SAMPLE_RATE = 44100;
    final public int FRAME_SIZE = 4096;

    float[] prevAmplitudes = null;
    final String audioFileName = "audio.wav";
    private File audioFile = null;

    private AudioData experimentData;

    // currently only works with 0.5f
    final private float liveStepFactor = 0.5f;

    public interface ISensorDataListener {
        public void onNewAmplitudeData(float[] amplitudes);
        public void onNewFrequencyData(float[] frequencies);
    }

    public void setSensorDataListener(ISensorDataListener listener) {
        if (listener == null)
            softListener = null;
        else
            softListener = new WeakReference<>(listener);
    }

    public float getLiveStepFactor() {
        return liveStepFactor;
    }

    private void notifyNewAudioData(float[] amplitudes) {
        if (softListener == null)
            return;

        ISensorDataListener listener = softListener.get();
        if (listener == null)
            return;

        listener.onNewAmplitudeData(amplitudes);

        if (prevAmplitudes != null) {
            int stepSize =  (int)(amplitudes.length * liveStepFactor);
            int stepPosition = stepSize;
            while (stepPosition < amplitudes.length) {
                float[] frequencies = Fourier.transformOverlap(prevAmplitudes, amplitudes, stepPosition);
                listener.onNewFrequencyData(frequencies);
                stepPosition += stepSize;
            }
        }
        float[] frequencies = Fourier.transform(amplitudes);
        listener.onNewFrequencyData(frequencies);
        prevAmplitudes = amplitudes;
    }

    @Override
    public String getSensorName() {
        return SENSOR_NAME;
    }

    @Override
    public View createExperimentView(Context context) {
        AbstractExperimentSensorView view = new MicrophoneExperimentSensorView(context, this);
        setListener(view);
        return view;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem) {
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    @Override
    public void init(final Activity activity) {
        experimentData = new AudioData(this);

        previewState = new State() {
            private AudioRecordingTask audioRecordingTask = null;

            @Override
            public void start() {
                audioRecordingTask = new AudioRecordingTask(null, SAMPLE_RATE, FRAME_SIZE);
                audioRecordingTask.start();
            }

            @Override
            public boolean stop() {
                audioRecordingTask.stop();
                audioRecordingTask = null;
                return true;
            }
        };

        recordingState = new State() {
            private AudioRecordingTask audioRecordingTask = null;

            @Override
            public void start() {
                File outputDir = activity.getExternalCacheDir();
                audioFile = new File(outputDir, audioFileName);

                audioRecordingTask = new AudioRecordingTask(audioFile, SAMPLE_RATE, FRAME_SIZE);
                audioRecordingTask.start();
            }

            @Override
            public boolean stop() {
                audioRecordingTask.stop();
                audioRecordingTask = null;
                return true;
            }
        };
    }

    @Override
    public void finishExperiment(boolean saveData, File storageBaseDir) throws IOException {
        super.finishExperiment(saveData, storageBaseDir);

        if (!saveData)
            deleteTempFiles();
        else {
            File storageDir = getSensorDataStorage(storageBaseDir, this.getClass().getSimpleName());
            if (!moveTempFilesToExperimentDir(storageDir))
                throw new IOException();
            experimentData.setAudioFileName(audioFileName);
            experimentData.saveExperimentData(storageDir);
        }
        audioFile = null;
    }

    public File getAudioFile() {
        return audioFile;
    }

    private boolean deleteTempFiles() {
        if (audioFile != null && audioFile.exists())
            return audioFile.delete();
        return true;
    }

    private boolean moveTempFilesToExperimentDir(File storageDir) {
        if (!storageDir.exists())
            if (!storageDir.mkdirs())
                return false;
        File target = new File(storageDir, audioFileName);
        try {
            return StorageLib.moveFile(audioFile, target);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private class AudioRecordingTask {
        private AtomicBoolean running = new AtomicBoolean();

        final private int samplingRate;
        final private int sampleSize;

        private OutputStream dataOutput = null;
        final private File outputFile;
        private AudioRecord audioRecord = null;

        private Handler uiHandler = new Handler();
        private Thread thread = null;

        Runnable pollRunnable = new Runnable() {
            @Override
            public void run() {
                running.set(true);
                try {
                    startAudioRecording(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                while (running.get()) {
                    final int bytesPerSample = 2;
                    final int bytesToRead = bytesPerSample * sampleSize;
                    final byte[] buffer = new byte[bytesToRead];

                    if (!readData(buffer, bytesToRead))
                        break;

                    publishData(buffer, bytesToRead);
                }

                stopAudioRecording();
            }

            private boolean readData(byte[] buffer, final int bytesToRead) {
                int bytesRead = 0;
                int missingBytes = bytesToRead;
                while (running.get()) {
                    int result = audioRecord.read(buffer, bytesRead, missingBytes);
                    if (result < 0)
                        break;
                    bytesRead += result;
                    missingBytes -= result;
                    if (missingBytes == 0)
                        return true;
                }
                return false;
            }
        };

        public AudioRecordingTask(File outputFile, int samplingRate, int sampleSize) {
            this.outputFile = outputFile;
            this.samplingRate = samplingRate;
            this.sampleSize = sampleSize;
        }

        private void publishData(final byte buffer[], final int dataSize) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (dataOutput != null) {
                        try {
                            dataOutput.write(buffer, 0, dataSize);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    final float frame[] = AudioWavInputStream.toAmplitudeData(buffer, dataSize);
                    notifyNewAudioData(frame);
                }
            });
        }

        public void start() {
            thread = new Thread(pollRunnable);
            thread.start();
        }

        public void stop() {
            running.set(false);
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void startAudioRecording(File outputFile) throws IOException {
            if (outputFile != null)
                dataOutput = new AudioWavOutputStream(outputFile, 1, samplingRate);

            final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
            final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
            int bufferSize = AudioRecord.getMinBufferSize(samplingRate, CHANNEL_CONFIG, FORMAT) * 4;
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate, CHANNEL_CONFIG, FORMAT,
                    bufferSize);

            audioRecord.startRecording();
        }

        private void stopAudioRecording() {
            audioRecord.stop();
            if (dataOutput != null) {
                try {
                    dataOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dataOutput = null;
            }
            audioRecord.release();
        }
    }

    @Override
    public ISensorData getExperimentData() {
        return experimentData;
    }
}
