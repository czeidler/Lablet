/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.experiment.*;


public class VideoAnalysisPlugin implements IAnalysisPlugin {
    @Override
    public String getIdentifier() {
        return "CameraAnalysis";
    }

    @Override
    public String supportedDataType() {
        return "Video";
    }

    @Override
    public ISensorAnalysis createSensorAnalysis(SensorData sensorData) {
        return new CameraExperimentAnalysis(sensorData);
    }

    @Override
    public Fragment createSensorAnalysisFragment(ExperimentAnalysisActivity.AnalysisRef analysisRef) {
        return new CameraAnalysisFragment(analysisRef);
    }

    @Override
    public void startAnalysisSettingsActivity(Activity parentActivity, int requestCode,
                                              ExperimentAnalysisActivity.AnalysisRef analysisRef,
                                              String experimentPath, Bundle options) {
        Intent intent = new Intent(parentActivity, CameraRunSettingsActivity.class);
        ExperimentPluginHelper.packStartAnalysisSettingsIntent(intent, analysisRef, experimentPath, options);
        parentActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean hasAnalysisSettingsActivity(StringBuilder menuName) {
        if (menuName != null)
            menuName.append("Video Settings");
        return true;
    }
}
