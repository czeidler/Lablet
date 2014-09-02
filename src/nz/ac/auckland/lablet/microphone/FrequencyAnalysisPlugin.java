/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.experiment.IAnalysisPlugin;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.SensorData;


public class FrequencyAnalysisPlugin implements IAnalysisPlugin {

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String supportedDataType() {
        return "Audio";
    }

    @Override
    public ISensorAnalysis createSensorAnalysis(SensorData sensorData) {
        assert sensorData instanceof MicrophoneSensorData;
        return new FrequencyAnalysis((MicrophoneSensorData)sensorData);
    }

    @Override
    public boolean hasAnalysisSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public void startAnalysisSettingsActivity(Activity parentActivity, int requestCode, ExperimentAnalysisActivity.AnalysisRef analysisRef, String experimentPath, Bundle options) {

    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysisActivity.AnalysisRef analysisRef) {
        return new FrequencyAnalysisFragment(analysisRef);
    }
}
