/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import java.util.ArrayList;
import java.util.List;


public class FrequencyAnalysisView extends FrameLayout {
    final private FrequencyAnalysis frequencyAnalysis;
    final private MicrophoneSensorData micSensorData;
    private AudioFrequencyMapAdapter audioFrequencyMapAdapter;
    private byte[] wavRawData = null;

    private Spinner sampleSizeSpinner;
    final private List<String> sampleSizeList = new ArrayList<>();

    final private FreqMapUpdater freqMapUpdater = new FreqMapUpdater();

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
        final int sampleRate = audioWavInputStream.getSampleRate();

        GraphView2D tagMarkerView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        tagMarkerView.setAdapter(new MarkerGraphAdapter(analysis.getTagMarkerModel(), "Position Data",
                new XPositionMarkerGraphAxis(frequencyAnalysis.getXUnit(), null),
                new YPositionMarkerGraphAxis(frequencyAnalysis.getYUnit(), null)));

        final EditText freqResEditText = (EditText)view.findViewById(R.id.freqResEditText);
        final EditText timeStepEditText = (EditText)view.findViewById(R.id.timeStepEditText);

        sampleSizeSpinner = (Spinner)view.findViewById(R.id.sampleSizeSpinner);
        sampleSizeSpinner.setEnabled(false);
        sampleSizeList.add("128");
        sampleSizeList.add("256");
        sampleSizeList.add("512");
        sampleSizeList.add("1024");
        sampleSizeList.add("2048");
        sampleSizeList.add("4096");
        sampleSizeList.add("8192");
        sampleSizeList.add("16384");
        sampleSizeList.add("32768");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, sampleSizeList);
        sampleSizeSpinner.setAdapter(adapter);
        sampleSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                freqMapUpdater.update();

                int sampleSize = Integer.parseInt(sampleSizeList.get(i));

                float freqResolution = (float) sampleRate / sampleSize;
                float timeResolution = (float) sampleSize / sampleRate * 1000
                        * audioFrequencyMapAdapter.getStepFactor();

                freqResEditText.setText(String.format("%.2f", freqResolution));
                timeStepEditText.setText(String.format("%.2f", timeResolution));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sampleSizeSpinner.setSelection(sampleSizeList.indexOf("4096"));

        loadWavFileAsync(audioWavInputStream);
    }

    private void setupFrequencyView(PlotView frequencyMapPlotView, AudioWavInputStream audioWavInputStream) {
        audioFrequencyMapAdapter = new AudioFrequencyMapAdapter(0.5f);
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

    class DataContainer {
        public float[] data;
        public DataContainer(float[] data) {
            this.data = data;
        }
    }

    private void loadWavFileAsync(final AudioWavInputStream audioWavInputStream) {
        AsyncTask<Void, DataContainer, Void> asyncTask = new AsyncTask<Void, DataContainer, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    int size = audioWavInputStream.getSize();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);
                    wavRawData = new byte[size];
                    for (int i = 0; i < size; i++)
                        wavRawData[i] = (byte)bufferedInputStream.read();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                sampleSizeSpinner.setEnabled(true);
                freqMapUpdater.update();
            }
        };
        asyncTask.execute();
    }

    class FreqMapUpdater {
        private int sampleSize = -1;
        AsyncTask<Void, DataContainer, Void> asyncTask = null;

        public void update() {
            if (wavRawData == null)
                return;
            final int newSampleSize = Integer.parseInt(sampleSizeList.get(sampleSizeSpinner.getSelectedItemPosition()));
            if (sampleSize == newSampleSize)
                return;
            // old task running? cancel and return, new job is triggered afterwards
            if (asyncTask != null) {
                asyncTask.cancel(false);
                return;
            }

            // do the update
            audioFrequencyMapAdapter.clear();

            asyncTask = new AsyncTask<Void, DataContainer, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    float amplitudes[] = AudioWavInputStream.toAmplitudeData(wavRawData, wavRawData.length);

                    final int step = (int)(newSampleSize * audioFrequencyMapAdapter.getStepFactor());
                    for (int i = 0; i < amplitudes.length; i += step) {
                        float frequencies[] = Fourier.transform(amplitudes, i, newSampleSize);
                        if (isCancelled())
                            return null;
                        publishProgress(new DataContainer(frequencies));
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
                    sampleSize = newSampleSize;
                    onFinished();
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    onFinished();
                }
            };
            asyncTask.execute();
        }

        private void onFinished() {
            asyncTask = null;
            update();
        }
    }

}
