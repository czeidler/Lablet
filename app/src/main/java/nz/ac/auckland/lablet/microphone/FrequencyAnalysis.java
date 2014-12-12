/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.graphics.RectF;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.views.table.CSVWriter;
import nz.ac.auckland.lablet.views.table.MarkerDataTableAdapter;

import java.io.*;


public class FrequencyAnalysis implements IDataAnalysis {
    final private MicrophoneExperimentData sensorData;

    final private MarkerDataModel hCursorMarkerModel;
    final private MarkerDataModel vCursorMarkerModel;
    final private Unit xUnit = new Unit("s");
    final private Unit yUnit = new Unit("Hz");
    final private FreqMapDisplaySettings freqMapDisplaySettings = new FreqMapDisplaySettings();

    public class FreqMapDisplaySettings {
        private int windowSize = 4096;
        private float stepFactor = 0.5f;
        private int contrast = 127;
        private int brightness = 127;
        final private RectF range = new RectF();

        public int getWindowSize() {
            return windowSize;
        }

        public void setWindowSize(int windowSize) {
            this.windowSize = windowSize;
        }

        public float getStepFactor() {
            return stepFactor;
        }

        public void setStepFactor(float stepFactor) {
            this.stepFactor = stepFactor;
        }

        public int getContrast() {
            return contrast;
        }

        public void setContrast(int contrast) {
            this.contrast = contrast;
        }

        public int getBrightness() {
            return brightness;
        }

        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        public RectF getRange() {
            return range;
        }

        public void setRange(RectF range) {
            this.range.set(range);
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putInt("windowSize", windowSize);
            bundle.putFloat("stepFactor", stepFactor);
            bundle.putInt("contrast", contrast);
            bundle.putInt("brightness", brightness);
            bundle.putFloat("rangeLeft", range.left);
            bundle.putFloat("rangeTop", range.top);
            bundle.putFloat("rangeRight", range.right);
            bundle.putFloat("rangeBottom", range.bottom);
            return bundle;
        }

        public void fromBundle(Bundle bundle) {
            windowSize = bundle.getInt("windowSize", windowSize);
            stepFactor = bundle.getFloat("stepFactor", stepFactor);
            contrast = bundle.getInt("contrast", contrast);
            brightness = bundle.getInt("brightness", brightness);
            range.left = bundle.getFloat("rangeLeft", range.left);
            range.top = bundle.getFloat("rangeTop", range.top);
            range.right = bundle.getFloat("rangeRight", range.right);
            range.bottom = bundle.getFloat("rangeBottom", range.bottom);
        }
    }

    public FrequencyAnalysis(MicrophoneExperimentData sensorData) {
        this.sensorData = sensorData;

        xUnit.setName("time");
        yUnit.setName("frequency");
        hCursorMarkerModel = new MarkerDataModel();
        vCursorMarkerModel = new MarkerDataModel();
    }

    public Unit getXUnit() {
        return xUnit;
    }

    public Unit getYUnit() {
        return yUnit;
    }

    public FreqMapDisplaySettings getFreqMapDisplaySettings() {
        return freqMapDisplaySettings;
    }

    @Override
    public String getDisplayName() {
        return "Frequency Analysis";
    }

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public IExperimentData getData() {
        return sensorData;
    }

    @Override
    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        Bundle hCursorsBundle = bundle.getBundle("hCursors");
        if (hCursorsBundle != null)
            hCursorMarkerModel.fromBundle(hCursorsBundle);
        Bundle vCursorsBundle = bundle.getBundle("vCursors");
        if (vCursorsBundle != null)
            vCursorMarkerModel.fromBundle(vCursorsBundle);

        Bundle freqMapDisplaySettingsBundle = bundle.getBundle("freqMapDisplaySettings");
        if (freqMapDisplaySettingsBundle != null)
            freqMapDisplaySettings.fromBundle(freqMapDisplaySettingsBundle);

        return true;
    }

    @Override
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
        Bundle analysisDataBundle = new Bundle();

        if (hCursorMarkerModel.getMarkerCount() > 0)
            analysisDataBundle.putBundle("hCursors", hCursorMarkerModel.toBundle());
        if (vCursorMarkerModel.getMarkerCount() > 0)
            analysisDataBundle.putBundle("vCursors", vCursorMarkerModel.toBundle());

        analysisDataBundle.putBundle("freqMapDisplaySettings", getFreqMapDisplaySettings().toBundle());
        return analysisDataBundle;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) throws IOException {
        MarkerDataTableAdapter hTableAdapter = new MarkerDataTableAdapter(hCursorMarkerModel);
        hTableAdapter.addColumn(new HCursorColumn());
        hTableAdapter.addColumn(new HCursorDiffToPrevColumn());
        CSVWriter.writeTable(hTableAdapter, writer, ',');

        writer.write("\n");

        MarkerDataTableAdapter vTableAdapter = new MarkerDataTableAdapter(vCursorMarkerModel);
        vTableAdapter.addColumn(new VCursorColumn());
        vTableAdapter.addColumn(new VCursorDiffToPrevColumn());
        CSVWriter.writeTable(vTableAdapter, writer, ',');
    }

    public MarkerDataModel getHCursorMarkerModel() {
        return hCursorMarkerModel;
    }

    public MarkerDataModel getVCursorMarkerModel() {
        return vCursorMarkerModel;
    }
}
