/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


public class FrequencyAnalysis implements ISensorAnalysis {
    final private MicrophoneSensorData sensorData;

    public FrequencyAnalysis(MicrophoneSensorData sensorData) {
        this.sensorData = sensorData;
    }

    @Override
    public String getIdentifier() {
        return "FrequencyAnalysis";
    }

    @Override
    public SensorData getData() {
        return sensorData;
    }

    @Override
    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        return false;
    }

    @Override
    public void saveAnalysisData(File directory) throws IOException {

    }

    @Override
    public void exportTagMarkerCSVData(OutputStream outputStream) throws IOException {

    }
}
