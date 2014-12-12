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
import nz.ac.auckland.lablet.experiment.AbstractExperimentData;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;

import java.io.File;


public class MicrophoneExperimentData extends AbstractExperimentData {
    private String audioFileName;

    static final public String DATA_TYPE = "Audio";

    @Override
    public String getDataType() {
        return DATA_TYPE;
    }

    public MicrophoneExperimentData(Context experimentContext) {
        super(experimentContext);
    }

    public MicrophoneExperimentData(Context experimentContext, IExperimentSensor sourceSensor) {
        super(experimentContext, sourceSensor);
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
