/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


class CameraExperimenter extends AbstractPluginExperimenter {
    public CameraExperimenter(IExperimentPlugin plugin) {
        super(plugin);
    }

    @Override
    public IExperimentSensor createExperimentSensor(Activity parentActivity) {
        IExperimentSensor experiment = new CameraExperimentSensor(plugin);
        return experiment;
    }

    @Override
    public void startSensorSettingsActivity(Activity parentActivity, int requestCode,
                                            ExperimentData.SensorDataRef sensorDataRef,
                                            Bundle analysisSpecificData, Bundle options) {
        Intent intent = new Intent(parentActivity, CameraRunSettingsActivity.class);
        ExperimentPluginHelper.packStartRunSettingsIntent(intent, sensorDataRef, analysisSpecificData, options);
        parentActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean hasSensorSettingsActivity(StringBuilder menuName) {
        if (menuName != null)
            menuName.append("Video Settings");
        return true;
    }
}


class CameraAnalysis implements IExperimentPlugin.IAnalysis {
    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return new CameraSensorData(context, data, storageDir);
    }

    @Override
    public SensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return new CameraExperimentAnalysis(sensorData);
    }

    @Override
    public View createSensorAnalysisView(Context context, SensorAnalysis sensorAnalysis) {
        return new CameraAnalysisFragmentView(context, sensorAnalysis);
    }
}

/**
 * The camera experiment plugin.
 */
public class CameraExperimentPlugin implements IExperimentPlugin {
    @Override
    public String getName() {
        return CameraSensorData.class.getSimpleName();
    }


    @Override
    public String toString() {
        return "Camera Experiment";
    }

    @Override
    public IExperimenter getExperimenter() {
        return new CameraExperimenter(this);
    }

    @Override
    public IAnalysis getAnalysis() {
        return new CameraAnalysis();
    }
}
