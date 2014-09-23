/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.misc.AnimatedTabHostListener;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;
import nz.ac.auckland.lablet.views.*;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FrequencyAnalysisView extends FrameLayout {
    final private FrequencyAnalysis frequencyAnalysis;
    final private MicrophoneSensorData micSensorData;
    private AudioFrequencyMapAdapter audioFrequencyMapAdapter;
    private byte[] wavRawData = null;

    private Spinner sampleSizeSpinner;
    final private List<String> sampleSizeList = new ArrayList<>();
    private Spinner windowOverlapSpinner;
    final private List<OverlapSpinnerEntry> overlapSpinnerEntryList = new ArrayList<>();

    private AudioFrequencyMapPainter audioFrequencyMapPainter;

    private PlotView frequencyView;
    private ViewGroup loadingView;
    private EditText freqResEditText;
    private EditText timeStepEditText;
    private CheckBox renderScriptCheckBox;

    private SeekBar contrastSeekBar;
    private SeekBar brightnessSeekBar;

    final private FreqMapUpdater freqMapUpdater = new FreqMapUpdater();

    class OverlapSpinnerEntry {
        final public float stepFactor;
        final public String label;

        public OverlapSpinnerEntry(float stepFactor, String label) {
            this.stepFactor = stepFactor;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public FrequencyAnalysisView(final Context context, FrequencyAnalysis analysis) {
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

        frequencyView = (PlotView)view.findViewById(R.id.frequencyMapView);
        FrameLayout frameLayout = (FrameLayout)view.findViewById(R.id.plotViewFrameLayout);
        loadingView = (ViewGroup)inflater.inflate(R.layout.loading_overlay, frameLayout, false);
        loadingView.setMinimumWidth(frequencyView.getLayoutParams().width);
        hideLoadingView();
        frameLayout.addView(loadingView);

        setupFrequencyView(frequencyView, audioWavInputStream);

        TabHost.TabContentFactory tabContentFactory = new TabHost.TabContentFactory() {
            @Override
            public View createTabContent(String tag) {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                if (tag.equals("fourier_tab"))
                    return inflater.inflate(R.layout.frequency_fourier_parameters, null, false);
                if (tag.equals("cursor_tab")) {
                    CursorView cursorView = new CursorView(context, frequencyView,
                            frequencyAnalysis.getHCursorMarkerModel(), frequencyAnalysis.getVCursorMarkerModel());
                    cursorView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    return cursorView;
                }
                return null;
            }
        };
        TabHost tabHost = (TabHost)view.findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("fourier_tab").setIndicator("Fourier").setContent(tabContentFactory));
        tabHost.addTab(tabHost.newTabSpec("cursor_tab").setIndicator("Cursors").setContent(tabContentFactory));
        // load tabs now so that we can access the child views now
        tabHost.setCurrentTab(1);
        tabHost.setCurrentTab(0);
        tabHost.setOnTabChangedListener(new AnimatedTabHostListener(tabHost));

        setupFourierControls(view, audioWavInputStream.getSampleRate());

        loadWavFileAsync(audioWavInputStream);
    }

    private void setupFourierControls(ViewGroup view, final int sampleRate) {
        freqResEditText = (EditText)view.findViewById(R.id.freqResEditText);
        timeStepEditText = (EditText)view.findViewById(R.id.timeStepEditText);

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, sampleSizeList);
        sampleSizeSpinner.setAdapter(adapter);
        sampleSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                freqMapUpdater.update();

                int sampleSize = Integer.parseInt(sampleSizeList.get(i));
                float freqResolution = (float) sampleRate / sampleSize;
                freqResEditText.setText(String.format("%.2f", freqResolution));

                updateTimeStepSizeView(sampleSize, sampleRate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sampleSizeSpinner.setSelection(sampleSizeList.indexOf("4096"));

        windowOverlapSpinner = (Spinner)view.findViewById(R.id.steppingSpinner);
        windowOverlapSpinner.setEnabled(false);
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.1f, "90%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.2f, "80%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.3f, "70%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.4f, "60%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.5f, "50%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.6f, "40%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.7f, "30%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.8f, "20%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(0.9f, "10%"));
        overlapSpinnerEntryList.add(new OverlapSpinnerEntry(1.0f, "0%"));
        windowOverlapSpinner.setAdapter(new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, overlapSpinnerEntryList));
        windowOverlapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                freqMapUpdater.update();

                int sampleSize = Integer.parseInt(sampleSizeList.get(sampleSizeSpinner.getSelectedItemPosition()));
                updateTimeStepSizeView(sampleSize, sampleRate);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        windowOverlapSpinner.setSelection(4);

        renderScriptCheckBox = (CheckBox)view.findViewById(R.id.renderScriptCheckBox);
        renderScriptCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                freqMapUpdater.update();
            }
        });
        renderScriptCheckBox.setChecked(true);

        // contrast and brightness
        SeekBar.OnSeekBarChangeListener colorSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateContrastBrightness();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        contrastSeekBar = (SeekBar)view.findViewById(R.id.contrastSeekBar);
        contrastSeekBar.setOnSeekBarChangeListener(colorSeekBarListener);
        brightnessSeekBar = (SeekBar)view.findViewById(R.id.brightnessSeekBar);
        brightnessSeekBar.setOnSeekBarChangeListener(colorSeekBarListener);
        updateContrastBrightness();
    }

    private void updateContrastBrightness() {
        int contrast = contrastSeekBar.getProgress();
        int brightness = brightnessSeekBar.getProgress() - 127;

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[] {
                1, 0, 0, 0, brightness,
                0, 1, 0, 0, brightness,
                0, 0, 1, 0, brightness,
                0, 0, 0, 1, 0 });

        ColorMatrix contrastScaleMatrix = new ColorMatrix();
        float scale = ((float)(contrast - 127)/ 127.f) + 1.f;
        float translate = (.5f -.5f * scale) * 255.f;
        contrastScaleMatrix.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0 });

        colorMatrix.postConcat(contrastScaleMatrix);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        audioFrequencyMapPainter.setOffScreenPaint(paint);
        frequencyView.invalidate();
    }

    private void updateTimeStepSizeView(int sampleSize, int sampleRate) {
        float timeResolution = (float) sampleSize / sampleRate * 1000
                * audioFrequencyMapAdapter.getStepFactor();
        timeStepEditText.setText(String.format("%.2f", timeResolution));
    }

    private void setupFrequencyView(PlotView frequencyMapPlotView, AudioWavInputStream audioWavInputStream) {
        audioFrequencyMapAdapter = new AudioFrequencyMapAdapter(0.5f);
        audioFrequencyMapPainter = new AudioFrequencyMapPainter();
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

        MarkerDataModel hMarkerModel = frequencyAnalysis.getHCursorMarkerModel();
        HCursorDataModelPainter hCursorDataModelPainter = new HCursorDataModelPainter(hMarkerModel);
        frequencyMapPlotView.addPlotPainter(hCursorDataModelPainter);

        MarkerDataModel vMarkerModel = frequencyAnalysis.getVCursorMarkerModel();
        VCursorDataModelPainter vCursorDataModelPainter = new VCursorDataModelPainter(vMarkerModel);
        vCursorDataModelPainter.setMarkerPainterGroup(hCursorDataModelPainter.getMarkerPainterGroup());
        frequencyMapPlotView.addPlotPainter(vCursorDataModelPainter);
    }

    class DataContainer {
        public float[] data;
        public DataContainer(float[] data) {
            this.data = data;
        }
    }

    private void loadWavFileAsync(final AudioWavInputStream audioWavInputStream) {
        showLoadingView("Load WAV file...");

        AsyncTask<Void, DataContainer, Void> asyncTask = new AsyncTask<Void, DataContainer, Void>() {
            byte[] data;
            @Override
            protected Void doInBackground(Void... params) {
                try {
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

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                wavRawData = data;
                hideLoadingView();
                sampleSizeSpinner.setEnabled(true);
                windowOverlapSpinner.setEnabled(true);
                freqMapUpdater.update();
            }
        };
        asyncTask.execute();
    }

    class FreqMapUpdater {
        private int sampleSize = -1;
        AsyncTask<Void, DataContainer, Void> asyncTask = null;

        private boolean useRenderScript = false;

        private boolean isUpToDate() {
            if (wavRawData == null)
                return true;

            final int newSampleSize = Integer.parseInt(sampleSizeList.get(sampleSizeSpinner.getSelectedItemPosition()));
            if (sampleSize != newSampleSize)
                return false;

            final float newStepFactor = overlapSpinnerEntryList.get(
                    windowOverlapSpinner.getSelectedItemPosition()).stepFactor;
            if (audioFrequencyMapAdapter.getStepFactor() != newStepFactor)
                return false;

            if (useRenderScript != renderScriptCheckBox.isChecked())
                return false;

            return true;
        }

        public void update() {
            if (isUpToDate())
                return;
            // old task running? cancel and return, new job is triggered afterwards
            if (asyncTask != null) {
                asyncTask.cancel(false);
                return;
            }

            // do the update
            showLoadingView("Fourier Analysis...");

            sampleSize = -1;
            final int newSampleSize = Integer.parseInt(sampleSizeList.get(sampleSizeSpinner.getSelectedItemPosition()));
            final float newStepFactor = overlapSpinnerEntryList.get(
                    windowOverlapSpinner.getSelectedItemPosition()).stepFactor;
            audioFrequencyMapAdapter.clear();
            audioFrequencyMapAdapter.setStepFactor(newStepFactor);
            useRenderScript = renderScriptCheckBox.isChecked();

            asyncTask = new AsyncTask<Void, DataContainer, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    float amplitudes[] = AudioWavInputStream.toAmplitudeData(wavRawData, wavRawData.length);

                    final FourierRenderScript fourierRenderScript = new FourierRenderScript(getContext());

                    float[] frequencies;
                    if (useRenderScript)
                        frequencies = fourierRenderScript.renderScriptFFT(amplitudes, newSampleSize, audioFrequencyMapAdapter.getStepFactor());
                    else
                        frequencies = Fourier.transform(amplitudes, newSampleSize, audioFrequencyMapAdapter.getStepFactor());

                    int freqSampleSize = newSampleSize / 2;
                    for (int i = 0; i < frequencies.length; i += freqSampleSize) {
                        final float[] bunch = Arrays.copyOfRange(frequencies, i, i + freqSampleSize);
                        if (isCancelled())
                            return null;
                        publishProgress(new DataContainer(bunch));
                    }

                    /*
                    final int step = (int)(newSampleSize * audioFrequencyMapAdapter.getStepFactor());
                    for (int i = 0; i < amplitudes.length; i += step) {
                        float frequencies[] = Fourier.transform(amplitudes, i, newSampleSize);
                        if (isCancelled())
                            return null;
                        publishProgress(new DataContainer(frequencies));
                    }*/
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
            hideLoadingView();
        }
    }

    private void showLoadingView(String message) {
        loadingView.setVisibility(VISIBLE);
        ((TextView)loadingView.findViewById(R.id.loadingTextView)).setText(message);
    }

    private void hideLoadingView() {
        loadingView.setVisibility(INVISIBLE);
    }
}
