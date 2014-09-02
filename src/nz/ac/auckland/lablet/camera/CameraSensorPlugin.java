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
import nz.ac.auckland.lablet.experiment.ISensorPlugin;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;


public class CameraSensorPlugin implements ISensorPlugin {
    @Override
    public String getSensorIdentifier() {
        return CameraExperimentSensor.class.getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return "Camera";
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new CameraExperimentSensor();
    }

    @Override
    public SensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return new CameraSensorData(context, data, storageDir);
    }
}
