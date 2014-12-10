/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.graphics.RectF;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.views.plotview.PlotView;

import java.io.File;
import java.io.IOException;
import java.io.Writer;


public class Vector4DAnalysis implements ISensorAnalysis{
    final private AccelerometerSensorData sensorData;
    final private DisplaySettings displaySettings = new DisplaySettings();

    public class DisplaySettings {
        final RectF range = new RectF();

        public void setRange(RectF range) {
            this.range.set(range);
        }

        public RectF getRange() {
            return range;
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putFloat("rangeLeft", range.left);
            bundle.putFloat("rangeTop", range.top);
            bundle.putFloat("rangeRight", range.right);
            bundle.putFloat("rangeBottom", range.bottom);
            return bundle;
        }

        public void fromBundle(Bundle bundle) {
            range.left = bundle.getFloat("rangeLeft", range.left);
            range.top = bundle.getFloat("rangeTop", range.top);
            range.right = bundle.getFloat("rangeRight", range.right);
            range.bottom = bundle.getFloat("rangeBottom", range.bottom);
        }
    }

    public Vector4DAnalysis(AccelerometerSensorData sensorData) {
        this.sensorData = sensorData;
    }

    @Override
    public String getIdentifier() {
        return "Vector4DAnalysis";
    }

    @Override
    public ISensorData getData() {
        return sensorData;
    }

    @Override
    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        Bundle displaySettingsBundle = bundle.getBundle("displaySettingsBundle");
        if (displaySettingsBundle != null)
            displaySettings.fromBundle(displaySettingsBundle);

        return true;
    }

    @Override
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
        Bundle bundle = new Bundle();
        bundle.putBundle("displaySettingsBundle", getDisplaySettings().toBundle());

        return bundle;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) throws IOException {

    }

    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }
}
