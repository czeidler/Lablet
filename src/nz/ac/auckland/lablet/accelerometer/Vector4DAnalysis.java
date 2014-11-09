/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.ISensorData;

import java.io.File;
import java.io.IOException;
import java.io.Writer;


public class Vector4DAnalysis implements ISensorAnalysis{
    final private AccelerometerSensorData sensorData;

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
        return false;
    }

    @Override
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
        return null;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) throws IOException {

    }
}
