/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


class AccelerometerSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorName() {
        return AccelerometerExperimentSensor.class.getSimpleName();
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new AccelerometerExperimentSensor();
    }

    @Override
    public ISensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return null;
    }
}


class AccelerometerAnalysisPlugin implements IAnalysisPlugin {

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String supportedDataType() {
        return "Vector3D/Accelerometer";
    }

    @Override
    public ISensorAnalysis createSensorAnalysis(ISensorData sensorData) {
        return null;
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef) {
        return null;
    }
}

