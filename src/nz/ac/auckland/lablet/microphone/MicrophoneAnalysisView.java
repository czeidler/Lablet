/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;
import nz.ac.auckland.lablet.views.AudioFrequencyMapAdapter;
import nz.ac.auckland.lablet.views.AudioFrequencyMapPainter;
import nz.ac.auckland.lablet.views.IExperimentFrameView;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;


public class MicrophoneAnalysisView extends FrameLayout implements IExperimentFrameView {
    final private MicrophoneSensorData micSensorData;
    private AudioFrequencyMapAdapter audioFrequencyMapAdapter;

    public MicrophoneAnalysisView(Context context, SensorData sensorData) {
        super(context);

        this.micSensorData = (MicrophoneSensorData)sensorData;

        File audioFile = micSensorData.getAudioFile();
        AudioWavInputStream audioWavInputStream = null;
        try {
            audioWavInputStream = new AudioWavInputStream(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        addView(createFrequencyView(audioWavInputStream));


        loadWavFileAsync(audioWavInputStream);
    }

    private PlotView createFrequencyView(AudioWavInputStream audioWavInputStream) {
        PlotView frequencyMapPlotView = new PlotView(getContext());
        audioFrequencyMapAdapter = new AudioFrequencyMapAdapter();
        AudioFrequencyMapPainter audioFrequencyMapPainter = new AudioFrequencyMapPainter();
        audioFrequencyMapPainter.setDataAdapter(audioFrequencyMapAdapter);
        frequencyMapPlotView.addPlotPainter(audioFrequencyMapPainter);
        frequencyMapPlotView.setXRange(0, audioWavInputStream.getDurationMilliSeconds() / 1000);
        frequencyMapPlotView.setYRange(1, audioWavInputStream.getSampleRate() / 2);
        frequencyMapPlotView.setMaxXRange(0, audioWavInputStream.getDurationMilliSeconds() / 1000);
        frequencyMapPlotView.setMaxYRange(1, audioWavInputStream.getSampleRate() / 2);
        frequencyMapPlotView.setXDraggable(true);
        frequencyMapPlotView.setYDraggable(true);
        frequencyMapPlotView.setXZoomable(true);
        frequencyMapPlotView.setYZoomable(true);
        frequencyMapPlotView.getYAxisView().setUnit("Hz");
        frequencyMapPlotView.getYAxisView().setTitle("Frequency");
        frequencyMapPlotView.getXAxisView().setUnit("s");
        frequencyMapPlotView.getXAxisView().setTitle("Time");

        return frequencyMapPlotView;
    }

    private void loadWavFileAsync(final AudioWavInputStream audioWavInputStream) {
        audioFrequencyMapAdapter.clear();

        class DataContainer {
            public float[] data;

            public DataContainer(float[] data) {
                this.data = data;
            }
        }

        AsyncTask<Void, DataContainer, Void> asyncTask = new AsyncTask<Void, DataContainer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                byte[] data = null;
                try {
                    int size = audioWavInputStream.getSize();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);
                    data = new byte[size];
                    for (int i = 0; i < size; i++)
                        data[i] = (byte)bufferedInputStream.read();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                float amplitudes[] = AudioWavInputStream.toAmplitudeData(data, data.length);
                final int frameSize = 4096;
                for (int i = 0; i < amplitudes.length; i += frameSize) {
                    float frequencies[] = Fourier.transform(amplitudes, i, frameSize);
                    publishProgress(new DataContainer(frequencies));
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(DataContainer... values) {
                audioFrequencyMapAdapter.addData(values[0].data);
            }

        };
        asyncTask.execute();
    }

    @Override
    public void setCurrentFrame(int frame) {

    }

    @Override
    public RectF getDataRange() {
        return new RectF(0, 100, 100, 0);
    }

}
