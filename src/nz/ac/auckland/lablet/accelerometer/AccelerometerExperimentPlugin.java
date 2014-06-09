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
import nz.ac.auckland.lablet.camera.*;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.IExperiment;

import java.io.File;


public class AccelerometerExperimentPlugin extends AbstractExperimentPlugin {
    @Override
    public String getName() {
        return AccelerometerExperiment.class.getSimpleName();
    }

    @Override
    public String toString() {
        return "Accelerometer Experiment";
    }

    @Override
    public IExperiment createExperiment(Activity parentActivity, Intent intent, File experimentBaseDir) {
        IExperiment experiment = new AccelerometerExperiment();
        experiment.init(parentActivity, intent, experimentBaseDir);
        return experiment;
    }

    @Override
    public void startRunSettingsActivity(Activity parentActivity, int requestCode, ExperimentData experimentData,
                                         Bundle analysisSpecificData, Bundle options) {

    }

    @Override
    public boolean hasRunSettingsActivity(StringBuilder menuName) {
        return false;
    }

    @Override
    public ExperimentData loadExperiment(Context context, Bundle data, File storageDir) {
        return null;
    }

    @Override
    public ExperimentAnalysis createExperimentAnalysis(ExperimentData experimentData) {
        return null;
    }

    @Override
    public View createExperimentRunView(Context context, ExperimentData experimentData) {
        return null;
    }
}

