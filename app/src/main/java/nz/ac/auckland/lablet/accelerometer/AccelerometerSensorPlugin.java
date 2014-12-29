/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import nz.ac.auckland.lablet.experiment.*;


public class AccelerometerSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorName() {
        return AccelerometerExperimentSensor.SENSOR_NAME;
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new AccelerometerExperimentSensor();
    }

}


