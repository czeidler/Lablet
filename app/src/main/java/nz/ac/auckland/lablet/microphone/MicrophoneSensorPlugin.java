/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import nz.ac.auckland.lablet.experiment.*;


public class MicrophoneSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorName() {
        return MicrophoneExperimentSensor.SENSOR_NAME;
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new MicrophoneExperimentSensor();
    }

}


