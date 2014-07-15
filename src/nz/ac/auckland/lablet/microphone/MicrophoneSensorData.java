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
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;


public class MicrophoneSensorData extends SensorData {
    private String audioFileName = "";

    public MicrophoneSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);
    }

    public MicrophoneSensorData(Context experimentContext) {
        super(experimentContext);
    }

    @Override
    public float getMaxRawX() {
        return 100;
    }

    @Override
    public float getMaxRawY() {
        return 100;
    }

    @Override
    public int getNumberOfRuns() {
        return 0;
    }

    @Override
    public Bundle getRunAt(int i) {
        return null;
    }

    @Override
    public float getRunValueAt(int i) {
        return 0;
    }

    @Override
    public String getRunValueBaseUnit() {
        return "";
    }

    @Override
    public String getRunValueUnitPrefix() {
        return "";
    }

    @Override
    public String getRunValueLabel() {
        return "time";
    }

    @Override
    protected boolean loadExperimentData(Bundle bundle, File storageDir) {
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
}
