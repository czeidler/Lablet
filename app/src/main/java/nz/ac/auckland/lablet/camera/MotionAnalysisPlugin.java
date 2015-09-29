/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.*;


/**
 * A motion analysis plugin.
 */
public class MotionAnalysisPlugin implements IAnalysisPlugin {
    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String[] requiredDataTypes() {
        return new String[]{VideoData.DATA_TYPE};
    }

    @Override
    public IDataAnalysis createDataAnalysis(ISensorData... sensorData) {
        assert sensorData.length == 1;
        assert sensorData[0] instanceof VideoData;
        return new MotionAnalysis((VideoData)sensorData[0]);
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef) {
        Fragment fragment = new MotionAnalysisFragment();
        fragment.setArguments(analysisRef.toBundle());
        return fragment;
    }
}
