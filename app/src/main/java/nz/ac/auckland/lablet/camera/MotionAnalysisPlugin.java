/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.*;


public class MotionAnalysisPlugin implements IAnalysisPlugin {
    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String supportedDataType() {
        return "Video";
    }

    @Override
    public IDataAnalysis createDataAnalysis(IExperimentData sensorData) {
        assert sensorData instanceof CameraExperimentData;
        return new MotionAnalysis((CameraExperimentData)sensorData);
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef) {
        Fragment fragment = new MotionAnalysisFragment();
        fragment.setArguments(analysisRef.toBundle());
        return fragment;
    }
}
