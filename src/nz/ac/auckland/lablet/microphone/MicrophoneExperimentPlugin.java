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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


class MicrophoneSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorIdentifier() {
        return MicrophoneExperimentSensor.class.getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return "Microphone";
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new MicrophoneExperimentSensor();
    }

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return new MicrophoneSensorData(context, data, storageDir);
    }
}


class MicrophoneAnalysisPlugin implements IAnalysisPlugin {

    @Override
    public String getIdentifier() {
        return "MicrophoneAnalysisPlugin";
    }

    @Override
    public String supportedDataType() {
        return null;
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
