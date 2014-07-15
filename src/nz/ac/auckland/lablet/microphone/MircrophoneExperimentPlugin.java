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
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;


public class MircrophoneExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return MicrophoneExperimentSensor.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "Microphone Experiment";
    }

    @Override
    public IExperimentSensor createExperimentSensor(Activity parentActivity) {
        return new MicrophoneExperimentSensor();
    }

    @Override
    public void startRunSettingsActivity(Activity parentActivity, int requestCode, SensorData sensorData, Bundle analysisSpecificData, Bundle options) {

    }

    @Override
    public boolean hasSensorSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return null;
    }

    @Override
    public SensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return null;
    }

    @Override
    public View createSensorAnalysisView(Context context, SensorData sensorData) {
        return null;
    }
}
