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
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


public class AccelerometerSensorPlugin implements ISensorPlugin {

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


