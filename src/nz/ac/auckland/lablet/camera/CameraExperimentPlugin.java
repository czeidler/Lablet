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
import nz.ac.auckland.lablet.ExperimentActivity;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.IExperiment;

import java.io.File;


/**
 * The camera experiment plugin.
 */
public class CameraExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return CameraExperimentData.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "Camera Experiment";
    }

    @Override
    public IExperiment createExperiment(Activity parentActivity, Intent intent, File experimentBaseDir) {
        IExperiment experiment = new CameraExperiment();
        experiment.init(parentActivity, intent, experimentBaseDir);
        return experiment;
    }

    @Override
    public void startRunSettingsActivity(Activity parentActivity, int requestCode, ExperimentData experimentData,
                                         Bundle analysisSpecificData, Bundle options) {
        Intent intent = new Intent(parentActivity, CameraRunSettingsActivity.class);
        packStartRunSettingsIntent(intent, experimentData, analysisSpecificData, options);
        parentActivity.startActivityForResult(intent, requestCode);
    }

    @Override
    public boolean hasRunSettingsActivity(StringBuilder menuName) {
        if (menuName != null)
            menuName.append("Video Settings");
        return true;
    }

    @Override
    public ExperimentData loadExperiment(Context context, Bundle data, File storageDir) {
        return new CameraExperimentData(context, data, storageDir);
    }

    @Override
    public ExperimentAnalysis createExperimentAnalysis(ExperimentData experimentData) {
        return new CameraExperimentAnalysis(experimentData);
    }

    @Override
    public View createExperimentRunView(Context context, ExperimentData experimentData) {
        return new CameraExperimentRunView(context, experimentData);
    }
}
