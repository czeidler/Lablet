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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.AbstractExperimentRun;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.views.AudioAmplitudeView;
import nz.ac.auckland.lablet.views.AudioFrequencyView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


class MicrophoneExperimentRunView extends FrameLayout {
    private AudioAmplitudeView audioSignalView;
    private AudioFrequencyView audioFrequencyView;

    private MicrophoneExperimentRun.ISensorDataListener listener = new MicrophoneExperimentRun.ISensorDataListener() {
        @Override
        public void onNewAudioData(float[] amplitude) {
            audioSignalView.addData(amplitude);
            audioSignalView.invalidate();

            audioFrequencyView.addData(amplitude);
            audioFrequencyView.invalidate();
        }
    };

    public MicrophoneExperimentRunView(Context context, MicrophoneExperimentRun experimentRun) {
        super(context);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.microphone_run_view, null, false);
        addView(view);

        audioSignalView = (AudioAmplitudeView)view.findViewById(R.id.audioSignalView);
        audioFrequencyView = (AudioFrequencyView)view.findViewById(R.id.audioFrequencyView);

        experimentRun.setListener(listener);
    }

}

public class MicrophoneExperimentRun extends AbstractExperimentRun {
    private WeakReference<ISensorDataListener> softListener = null;
    private AudioRecordingTask audioRecordingTask = null;

    final int SAMPLE_RATE = 44100;
    final int FRAME_SIZE = 128;

    public interface ISensorDataListener {
        public void onNewAudioData(float[] amplitude);
    }

    public void setListener(ISensorDataListener listener) {
        softListener = new WeakReference<ISensorDataListener>(listener);
    }

    private void notifyNewAudioData(float[] amplitude) {
        if (softListener == null)
            return;
        ISensorDataListener listener = softListener.get();
        if (listener != null)
            listener.onNewAudioData(amplitude);
    }

    @Override
    public View createExperimentView(Context context) {
        return new MicrophoneExperimentRunView(context, this);
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
    public void init(Activity activity) {

    }

    @Override
    public void destroy() {

    }

    private class AudioRecordingTask extends AsyncTask<String, float[], Integer> {
        private AudioRecord audioRecord = null;
        private AtomicBoolean running = new AtomicBoolean();

        final String outputPath;
        final private int samplingRate;
        final private int sampleSize;
        private int bufferSize;

        AudioRecordingTask(String outputPath, int samplingRate, int sampleSize) {
            this.outputPath = outputPath;
            this.samplingRate = samplingRate;
            this.sampleSize = sampleSize;
        }

        @Override
        protected Integer doInBackground(String... none) {
            running.set(true);
            try {
                startAudioRecording(outputPath);
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }

            while (running.get()) {
                final int bytesPerSample = 2;
                final byte[] buffer = new byte[bytesPerSample * sampleSize];
                final int bufferReadResult = audioRecord.read(buffer, 0, bytesPerSample * sampleSize);
                final float frame[] = new float[bufferReadResult / bytesPerSample];

                int frameIndex = 0;
                for (int index = 0; index < bufferReadResult - bytesPerSample + 1; index += bytesPerSample) {
                    float sample = 0;
                    for (int b = 0; b < bytesPerSample; b++) {
                        int v = buffer[index + b];
                        if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                            v &= 0xFF;
                        }
                        sample += v << (b * 8);
                    }
                    frame[frameIndex] = sample;
                    frameIndex++;
                }

                publishProgress(frame);
            }

            stopAudioRecording();
            return 0;
        }

        @Override
        protected void onProgressUpdate(float[]... data) {
            notifyNewAudioData(data[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
        }

        public void stop() {
            running.set(false);
        }

        private void startAudioRecording(String outputFile) throws IOException {
            final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
            final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
            bufferSize = AudioRecord.getMinBufferSize(samplingRate, CHANNEL_CONFIG, FORMAT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, samplingRate,
                    CHANNEL_CONFIG, FORMAT, bufferSize);
            audioRecord.startRecording();
        }

        private void stopAudioRecording() {
            audioRecord.stop();
        }
    }

    @Override
    public void startPreview() {
        super.startPreview();

        audioRecordingTask = new AudioRecordingTask("/dev/null", SAMPLE_RATE, FRAME_SIZE);
        audioRecordingTask.execute();
    }

    @Override
    public void stopPreview() {
        super.stopPreview();

        audioRecordingTask.stop();
        audioRecordingTask = null;
    }

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

    }

    @Override
    public ExperimentRunData getExperimentData() {
        return null;
    }
}
