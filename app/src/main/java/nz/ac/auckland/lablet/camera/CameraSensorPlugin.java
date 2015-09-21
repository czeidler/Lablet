/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import nz.ac.auckland.lablet.experiment.IExperimentSensor;
import nz.ac.auckland.lablet.experiment.ISensorPlugin;


/**
 * Implementation of a camera sensor plugin.
 */
public class CameraSensorPlugin implements ISensorPlugin {
    @Override
    public String getSensorName() {
        return CameraExperimentSensor.SENSOR_NAME;
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new CameraExperimentSensor();
    }
}
