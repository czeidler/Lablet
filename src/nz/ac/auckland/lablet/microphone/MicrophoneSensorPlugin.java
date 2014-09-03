/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;


public class MicrophoneSensorPlugin implements ISensorPlugin {

    @Override
    public String getSensorIdentifier() {
        return MicrophoneExperimentSensor.class.getSimpleName();
    }

    @Override
    public String getDisplayName() {
        return "Microphone";
    }

    @Override
    public IExperimentSensor createExperimentSensor() {
        return new MicrophoneExperimentSensor();
    }

    @Override
    public ISensorData loadSensorData(Context context, Bundle data, File storageDir) {
        return new MicrophoneSensorData(context, data, storageDir);
    }
}


