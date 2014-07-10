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
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.emory.mathcs.jtransforms.dct.FloatDCT_1D;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.AbstractExperimentRun;
import nz.ac.auckland.lablet.experiment.AbstractExperimentRunView;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;
import nz.ac.auckland.lablet.misc.AudioWavOutputStream;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.*;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;


class MicrophoneExperimentRunView extends AbstractExperimentRunView {

    private ViewGroup previewView;
    private ViewGroup playbackView;

    public MicrophoneExperimentRunView(final Context context, final MicrophoneExperimentRun experimentRun) {
        super(context);

        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup view = (ViewGroup)inflater.inflate(R.layout.microphone_run_view, null, false);
        addView(view);

        previewView = (ViewGroup)view.findViewById(R.id.previewView);
        playbackView = (ViewGroup)view.findViewById(R.id.playbackView);

        playbackView.setVisibility(View.INVISIBLE);

        previewState = new AbstractExperimentRun.State() {
            private AudioFrequencyView audioFrequencyView;

            private PlotView audioAmplitudePlotView;
            private AudioAmplitudePlotDataAdapter audioAmplitudePlotAdapter;

            private PlotView frequencyMapPlotView;
            private AudioFrequencyMapAdapter audioFrequencyMapAdapter;

            private int frequencyMapTimeSpan = 60;
            private int amplitudeTimeSpan = 3;

            private MicrophoneExperimentRun.ISensorDataListener listener = new MicrophoneExperimentRun.ISensorDataListener() {
                @Override
                public void onNewAudioData(float[] amplitudes, float[] frequencies) {
                    if (audioAmplitudePlotAdapter.getSize() / experimentRun.SAMPLE_RATE >= amplitudeTimeSpan)
                        audioAmplitudePlotAdapter.clear();
                    audioAmplitudePlotAdapter.addData(amplitudes);

                    audioFrequencyView.addData(frequencies);

                    if (audioFrequencyMapAdapter.getSize() * experimentRun.FRAME_SIZE / experimentRun.SAMPLE_RATE
                            >= frequencyMapTimeSpan)
                        audioFrequencyMapAdapter.clear();
                    audioFrequencyMapAdapter.addData(frequencies);
                }
            };

            {
                audioAmplitudePlotView = (PlotView)view.findViewById(R.id.audioSignalView);
                AudioAmplitudePainter audioAmplitudePainter = new AudioAmplitudePainter();
                audioAmplitudePlotAdapter = new AudioAmplitudePlotDataAdapter();
                audioAmplitudePainter.setDataAdapter(audioAmplitudePlotAdapter);
                audioAmplitudePlotView.addPlotPainter(audioAmplitudePainter);
                audioAmplitudePlotView.setRangeX(0, amplitudeTimeSpan);
                audioAmplitudePlotView.setRangeY(-1, 1);
                audioAmplitudePlotView.getXAxisView().setUnit("s");
                audioAmplitudePlotView.getXAxisView().setLabel("Time");

                audioFrequencyView = (AudioFrequencyView)view.findViewById(R.id.audioFrequencyView);

                frequencyMapPlotView = (PlotView)view.findViewById(R.id.audioFrequencyMapPlot);
                audioFrequencyMapAdapter = new AudioFrequencyMapAdapter();
                AudioFrequencyMapPainter audioFrequencyMapPainter = new AudioFrequencyMapPainter();
                audioFrequencyMapPainter.setDataAdapter(audioFrequencyMapAdapter);
                frequencyMapPlotView.addPlotPainter(audioFrequencyMapPainter);
                frequencyMapPlotView.setRangeX(0, frequencyMapTimeSpan);
                frequencyMapPlotView.setRangeY(0, experimentRun.SAMPLE_RATE / 2);
                frequencyMapPlotView.getYAxisView().setUnit("Hz");
                frequencyMapPlotView.getYAxisView().setLabel("Frequency");
                frequencyMapPlotView.getXAxisView().setUnit("s");
                frequencyMapPlotView.getXAxisView().setLabel("Time");

                experimentRun.setSensorDataListener(listener);
            }

            @Override
            public void start() {
                previewView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean stop() {
                previewView.setVisibility(View.INVISIBLE);
                return true;
            }
        };

        recordingState = previewState;

        playbackState = new AbstractExperimentRun.State() {
            final private ToggleButton startPauseButton;
            final private SeekBar seekBar;
            final private TextView lengthTextView;
            final private PlotView playbackAmplitudeView;
            private AudioAmplitudePlotDataAdapter audioAmplitudePlotAdapter;

            private MediaPlayer mediaPlayer;

            final private Runnable seekBarUpdater = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer == null)
                        return;
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    if (!mediaPlayer.isPlaying())
                        return;
                    new Handler().postDelayed(seekBarUpdater, 100);
                }
            };

            {
                startPauseButton = (ToggleButton)view.findViewById(R.id.startPauseButton);
                seekBar = (SeekBar)view.findViewById(R.id.seekBar);
                lengthTextView = (TextView)view.findViewById(R.id.lengthTextView);
                playbackAmplitudeView = (PlotView)view.findViewById(R.id.playbackAmplitudeView);
                AudioAmplitudePainter audioAmplitudePainter = new AudioAmplitudePainter();
                audioAmplitudePlotAdapter = new AudioAmplitudePlotDataAdapter();
                audioAmplitudePainter.setDataAdapter(audioAmplitudePlotAdapter);
                playbackAmplitudeView.addPlotPainter(audioAmplitudePainter);
                playbackAmplitudeView.setRangeY(-1, 1);
                playbackAmplitudeView.getXAxisView().setUnit("ms");
                playbackAmplitudeView.getXAxisView().setLabel("Time");

                startPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        if (mediaPlayer == null)
                            return;
                        if (checked) {
                            mediaPlayer.start();
                            seekBarUpdater.run();
                        } else
                            mediaPlayer.pause();
                    }
                });

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                        mediaPlayer.seekTo(progress);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
            }

            private void loadWavFileAsync() {
                audioAmplitudePlotAdapter.clear();

                AsyncTask<File, Integer, Void> asyncTask = new AsyncTask<File, Integer, Void>() {
                    private byte[] data;

                    @Override
                    protected Void doInBackground(File... files) {
                        try {
                            AudioWavInputStream audioWavInputStream = new AudioWavInputStream(files[0]);
                            int size = audioWavInputStream.getSize();
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);
                            data = new byte[size];
                            for (int i = 0; i < size; i++)
                                data[i] = (byte)bufferedInputStream.read();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }

                    protected void onPostExecute(Void result) {
                        audioAmplitudePlotAdapter.addData(AudioWavInputStream.toAmplitudeData(data, data.length));
                    }
                };
                asyncTask.execute(experimentRun.getAudioFile());
            }

            @Override
            public void start() {
                playbackView.setVisibility(View.VISIBLE);

                loadWavFileAsync();

                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            startPauseButton.setChecked(false);
                            mediaPlayer.seekTo(0);
                        }
                    });

                    mediaPlayer.setDataSource(experimentRun.getAudioFile().getPath());
                    mediaPlayer.prepare();

                } catch (IOException e) {
                    mediaPlayer = null;
                    e.printStackTrace();
                    return;
                }

                int duration = mediaPlayer.getDuration();
                seekBar.setMax(duration);
                lengthTextView.setText("" + (duration / 1000) + "s");
                playbackAmplitudeView.setRangeX(0, (float)duration / 1000);
            }

            @Override
            public boolean stop() {
                playbackView.setVisibility(View.INVISIBLE);

                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                return false;
            }
        };
    }

    @Override
    public void onSettingsChanged() {

    }
}

public class MicrophoneExperimentRun extends AbstractExperimentRun {
    private Activity activity;

    private WeakReference<ISensorDataListener> softListener = null;

    final public int SAMPLE_RATE = 44100;
    final public int FRAME_SIZE = 4096;//8192;//16384;

    final String audioFileName = "audio.wav";
    private File audioFile = null;

    private MicrophoneExperimentRunData experimentData;

    public interface ISensorDataListener {
        public void onNewAudioData(float[] amplitudes, float[] frequencies);
    }

    public void setSensorDataListener(ISensorDataListener listener) {
        softListener = new WeakReference<>(listener);
    }

    private void notifyNewAudioData(float[] amplitudes) {
        if (softListener == null)
            return;

        float[] frequencies = fourier(amplitudes);

        ISensorDataListener listener = softListener.get();
        if (listener != null)
            listener.onNewAudioData(amplitudes, frequencies);
    }

    @Override
    public View createExperimentView(Context context) {
        AbstractExperimentRunView view = new MicrophoneExperimentRunView(context, this);
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
        this.activity = activity;

        experimentData = new MicrophoneExperimentRunData(activity);

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
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

        if (!saveData)
            deleteTempFiles();
        else {
            if (!moveTempFilesToExperimentDir(storageDir))
                throw new IOException();
            experimentData.setAudioFileName(audioFileName);
            experimentData.saveExperimentDataToFile(storageDir);
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
        return StorageLib.moveFile(audioFile, target);
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
                    if (dataOutput != null) {
                        try {
                            dataOutput.write(buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    final float frame[] = AudioWavInputStream.toAmplitudeData(buffer, bytesToRead);

                    publishData(frame);
                }

                stopAudioRecording();
            }

            private boolean readData(byte[] buffer, int bytesToRead) {
                int bytesRead = 0;
                int missingBytes = bytesToRead;
                while (running.get()) {
                    int result = audioRecord.read(buffer, bytesRead, missingBytes);
                    if (result < 0)
                        break;
                    bytesRead += result;
                    missingBytes = bytesToRead - bytesRead;
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

        private void publishData(final float frame[]) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
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
            int bufferSize = AudioRecord.getMinBufferSize(samplingRate, CHANNEL_CONFIG, FORMAT);
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
    public ExperimentRunData getExperimentData() {
        return null;
    }

    private void hammingWindow(float[] samples) {
        for (int i = 0; i < samples.length; i++)
            samples[i] *= (0.54f - 0.46f * Math.cos(2 * Math.PI * i / (samples.length - 1)));
    }

    private float[] fourier(float[] in) {
        float trafo[] = Arrays.copyOf(in, in.length);

        FloatDCT_1D dct = new FloatDCT_1D(trafo.length);
        // in place window
        hammingWindow(trafo);

        // in place transform: timeData becomes frequency data
        dct.forward(trafo, false);

        return trafo;
    }
}
