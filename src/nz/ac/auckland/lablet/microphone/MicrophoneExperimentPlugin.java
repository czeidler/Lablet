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
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


public class MicrophoneExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return MicrophoneSensorData.class.getSimpleName();
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
    public void startSensorSettingsActivity(Activity parentActivity, int requestCode,
                                            ExperimentData.SensorDataRef sensorDataRef,
                                            Bundle analysisSpecificData, Bundle options) {

    }

    @Override
    public boolean hasSensorSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return new MicrophoneSensorData(context, data, storageDir);
    }

    @Override
    public SensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return new SensorAnalysis(sensorData);
    }

    @Override
    public View createSensorAnalysisView(Context context, SensorData sensorData) {
        return null;
    }
}
