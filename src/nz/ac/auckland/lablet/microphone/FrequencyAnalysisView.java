/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;
import nz.ac.auckland.lablet.views.*;
import nz.ac.auckland.lablet.views.graph.*;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;


public class FrequencyAnalysisView extends FrameLayout implements IExperimentFrameView {
    final private FrequencyAnalysis frequencyAnalysis;
    final private MicrophoneSensorData micSensorData;
    private AudioFrequencyMapAdapter audioFrequencyMapAdapter;

    public FrequencyAnalysisView(Context context, FrequencyAnalysis analysis) {
        super(context);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.frequency_analysis, this, true);

        this.frequencyAnalysis = analysis;
        this.micSensorData = (MicrophoneSensorData)analysis.getData();

        File audioFile = micSensorData.getAudioFile();
        AudioWavInputStream audioWavInputStream;
        try {
            audioWavInputStream = new AudioWavInputStream(audioFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        PlotView frequencyView = (PlotView)view.findViewById(R.id.frequencyMapView);
        setupFrequencyView(frequencyView, audioWavInputStream);

        GraphView2D tagMarkerView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        tagMarkerView.setAdapter(new MarkerGraphAdapter(analysis.getTagMarkerModel(), "Position Data",
                new XPositionMarkerGraphAxis(frequencyAnalysis.getXUnit(), null),
                new YPositionMarkerGraphAxis(frequencyAnalysis.getYUnit(), null)));

        loadWavFileAsync(audioWavInputStream);
    }

    private void setupFrequencyView(PlotView frequencyMapPlotView, AudioWavInputStream audioWavInputStream) {
        audioFrequencyMapAdapter = new AudioFrequencyMapAdapter();
        AudioFrequencyMapPainter audioFrequencyMapPainter = new AudioFrequencyMapPainter();
        audioFrequencyMapPainter.setDataAdapter(audioFrequencyMapAdapter);
        frequencyMapPlotView.addPlotPainter(audioFrequencyMapPainter);
        frequencyMapPlotView.setXRange(0, audioWavInputStream.getDurationMilliSeconds());
        frequencyMapPlotView.setYRange(1, audioWavInputStream.getSampleRate() / 2);
        frequencyMapPlotView.setMaxXRange(0, audioWavInputStream.getDurationMilliSeconds());
        frequencyMapPlotView.setMaxYRange(1, audioWavInputStream.getSampleRate() / 2);
        frequencyMapPlotView.setXDraggable(true);
        frequencyMapPlotView.setYDraggable(true);
        frequencyMapPlotView.setXZoomable(true);
        frequencyMapPlotView.setYZoomable(true);
        frequencyMapPlotView.getXAxisView().setUnit("ms");
        frequencyMapPlotView.getXAxisView().setTitle("Time");
        frequencyMapPlotView.getYAxisView().setUnit("Hz");
        frequencyMapPlotView.getYAxisView().setTitle("Frequency");

        MarkerDataModel markerModel = frequencyAnalysis.getTagMarkerModel();
        markerModel.addMarkerData(new MarkerData(0));
        markerModel.addMarkerData(new MarkerData(1));
        markerModel.addMarkerData(new MarkerData(2));
        markerModel.selectMarkerData(0);
        EditMarkerDataModelPainter markerDataModelPainter = new EditMarkerDataModelPainter(markerModel);
        frequencyMapPlotView.addPlotPainter(markerDataModelPainter);
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
                final int half = frameSize / 2;
                for (int i = 0; i < amplitudes.length; i += half) {
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
