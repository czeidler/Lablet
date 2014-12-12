/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;
import nz.ac.auckland.lablet.experiment.IExperimentData;
import nz.ac.auckland.lablet.experiment.ISensorPlugin;

import java.io.File;
import java.io.IOException;


public class CameraSensorPlugin implements ISensorPlugin {
    @Override
    public String getSensorName() {
        return CameraExperimentSensor.SENSOR_NAME;
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new CameraExperimentSensor();
    }

    @Override
    public IExperimentData loadSensorData(Context context, Bundle data, File storageDir) {
        IExperimentData sensorData = new CameraExperimentData(context);
        try {
            sensorData.loadExperimentData(data, storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sensorData;
    }
}
