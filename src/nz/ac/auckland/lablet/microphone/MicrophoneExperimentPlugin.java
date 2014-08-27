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


class MicrophoneExperimenter extends AbstractPluginExperimenter {
    public MicrophoneExperimenter(IExperimentPlugin plugin) {
        super(plugin);
    }

    @Override
    public IExperimentSensor createExperimentSensor(Activity parentActivity) {
        return new MicrophoneExperimentSensor(plugin);
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

}


class MicrophoneAnalysis implements IExperimentPlugin.IAnalysis {
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
        return new MicrophoneAnalysisView(context, sensorData);
    }
}


public class MicrophoneExperimentPlugin implements IExperimentPlugin {
    @Override
    public String getName() {
        return MicrophoneSensorData.class.getSimpleName();
    }

    @Override
    public IExperimenter getExperimenter() {
        return new MicrophoneExperimenter(this);
    }

    @Override
    public IAnalysis getAnalysis() {
        return new MicrophoneAnalysis();
    }

    @Override
    public String toString() {
        return "Microphone Experiment";
    }
}
