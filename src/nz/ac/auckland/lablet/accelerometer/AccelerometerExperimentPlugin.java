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
import android.view.View;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


class AccelerometerExperimenter extends AbstractPluginExperimenter {
    public AccelerometerExperimenter(IExperimentPlugin plugin) {
        super(plugin);
    }

    @Override
    public IExperimentSensor createExperimentSensor(Activity parentActivity) {
        IExperimentSensor experiment = new AccelerometerExperimentSensor(plugin);
        return experiment;
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

class AccelerometerAnalysis implements IExperimentPlugin.IAnalysis {

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return null;
    }

    @Override
    public SensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return null;
    }

    @Override
    public View createSensorAnalysisView(Context context, SensorAnalysis sensorAnalysis) {
        return null;
    }
}


public class AccelerometerExperimentPlugin implements IExperimentPlugin {
    @Override
    public String getName() {
        return AccelerometerSensorData.class.getSimpleName();
    }

    @Override
    public IExperimenter getExperimenter() {
        return new AccelerometerExperimenter(this);
    }

    @Override
    public IAnalysis getAnalysis() {
        return new AccelerometerAnalysis();
    }

    @Override
    public String toString() {
        return "Accelerometer Experiment";
    }
}

