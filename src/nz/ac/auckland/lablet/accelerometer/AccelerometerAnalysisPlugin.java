/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.IAnalysisPlugin;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.ISensorData;


public class AccelerometerAnalysisPlugin implements IAnalysisPlugin {

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public String supportedDataType() {
        return AccelerometerSensorData.DATA_TYPE;
    }

    @Override
    public ISensorAnalysis createSensorAnalysis(ISensorData sensorData) {
        assert sensorData instanceof AccelerometerSensorData;
        return new Vector4DAnalysis((AccelerometerSensorData)sensorData);
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef) {
        Fragment fragment = new Vector4DAnalysisFragment();
        fragment.setArguments(analysisRef.toBundle());
        return fragment;
    }
}
