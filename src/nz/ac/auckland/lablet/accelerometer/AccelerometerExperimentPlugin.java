/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.experiment.IExperimentRun;

import java.io.File;


public class AccelerometerExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return AccelerometerExperimentRun.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "Accelerometer Experiment";
    }

    @Override
    public IExperimentRun createExperiment(Activity parentActivity) {
        IExperimentRun experiment = new AccelerometerExperimentRun();
        return experiment;
    }

    @Override
    public void startRunSettingsActivity(Activity parentActivity, int requestCode, ExperimentRunData experimentRunData,
                                         Bundle analysisSpecificData, Bundle options) {

    }

    @Override
    public boolean hasRunSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public ExperimentRunData loadExperimentData(Context context, Bundle data, File storageDir) {
        return null;
    }

    @Override
    public ExperimentAnalysis createExperimentAnalysis(ExperimentRunData experimentRunData) {
        return null;
    }

    @Override
    public View createExperimentRunView(Context context, ExperimentRunData experimentRunData) {
        return null;
    }
}

