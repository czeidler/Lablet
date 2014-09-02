/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


class AccelerometerSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorIdentifier() {
        return AccelerometerExperimentSensor.class.getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return "Accelerometer";
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new AccelerometerExperimentSensor();
    }

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return null;
    }
}


class AccelerometerAnalysisPlugin implements IAnalysisPlugin {

    @Override
    public String getIdentifier() {
        return "AccelerometerAnalysisPlugin";
    }

    @Override
    public String supportedDataType() {
        return "Vector3D/Accelerometer";
    }

    @Override
    public ISensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return null;
    }

    @Override
    public boolean hasAnalysisSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public void startAnalysisSettingsActivity(Activity parentActivity, int requestCode, ExperimentAnalysisActivity.AnalysisRef analysisRef, String experimentPath, Bundle options) {

    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysisActivity.AnalysisRef analysisRef) {
        return null;
    }
}

