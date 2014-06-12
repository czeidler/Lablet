/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.IExperimentRun;

import java.io.File;


/**
 * The camera experiment plugin.
 */
public class CameraExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return CameraExperimentRun.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "Camera Experiment";
    }

    @Override
    public IExperimentRun createExperiment(Activity parentActivity, Intent intent, File experimentBaseDir) {
        IExperimentRun experiment = new CameraExperimentRun();
        experiment.init(parentActivity, intent, experimentBaseDir);
        return experiment;
    }

    @Override
    public void startRunSettingsActivity(Activity parentActivity, int requestCode, ExperimentRunData experimentRunData,
                                         Bundle analysisSpecificData, Bundle options) {
        Intent intent = new Intent(parentActivity, CameraRunSettingsActivity.class);
        packStartRunSettingsIntent(intent, experimentRunData, analysisSpecificData, options);
        parentActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean hasRunSettingsActivity(StringBuilder menuName) {
        if (menuName != null)
            menuName.append("Video Settings");
        return true;
    }

    @Override
    public ExperimentRunData loadExperiment(Context context, Bundle data, File storageDir) {
        return new CameraExperimentRunData(context, data, storageDir);
    }

    @Override
    public ExperimentAnalysis createExperimentAnalysis(ExperimentRunData experimentRunData) {
        return new CameraExperimentAnalysis(experimentRunData);
    }

    @Override
    public View createExperimentRunView(Context context, ExperimentRunData experimentRunData) {
        return new CameraExperimentFrameView(context, experimentRunData);
    }
}
