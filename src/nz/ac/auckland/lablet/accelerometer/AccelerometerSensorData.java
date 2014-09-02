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
import nz.ac.auckland.lablet.experiment.IExperimentSensor;
import nz.ac.auckland.lablet.experiment.ISensorPlugin;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;


public class AccelerometerSensorData extends SensorData {
    @Override
    public String getDataType() {
        return "3DVector/accelerometer";
    }

    public AccelerometerSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);
    }

    public AccelerometerSensorData(Context experimentContext, IExperimentSensor sourceSensor) {
        super(experimentContext, sourceSensor);
    }

}
