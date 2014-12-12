/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.IAnalysisPlugin;
import nz.ac.auckland.lablet.experiment.IDataAnalysis;
import nz.ac.auckland.lablet.experiment.IExperimentData;


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
    public IDataAnalysis createDataAnalysis(IExperimentData sensorData) {
        assert sensorData instanceof MicrophoneExperimentData;
        return new FrequencyAnalysis((MicrophoneExperimentData)sensorData);
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef) {
        Fragment fragment = new FrequencyAnalysisFragment();
        fragment.setArguments(analysisRef.toBundle());
        return fragment;
    }
}
