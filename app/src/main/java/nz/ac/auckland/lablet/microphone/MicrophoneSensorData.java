/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.AbstractSensorData;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;

import java.io.File;


public class MicrophoneSensorData extends AbstractSensorData {
    private String audioFileName;

    static final public String DATA_TYPE = "Audio";

    @Override
    public String getDataType() {
        return DATA_TYPE;
    }

    public MicrophoneSensorData() {
        super();
    }

    public MicrophoneSensorData(IExperimentSensor sourceSensor) {
        super(sourceSensor);
    }

    @Override
    public boolean loadExperimentData(Bundle bundle, File storageDir) {
        if (!super.loadExperimentData(bundle, storageDir))
            return false;

        setAudioFileName(bundle.getString("audioFileName"));

        return true;
    }

    @Override
    public Bundle experimentDataToBundle() {
        Bundle bundle = super.experimentDataToBundle();

        bundle.putString("audioFileName", audioFileName);

        return bundle;
    }

    public void setAudioFileName(String fileName) {
        audioFileName = fileName;
    }

    public File getAudioFile() {
        return new File(getStorageDir(), audioFileName);
    }
}
