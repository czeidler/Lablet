/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Experiment {
    final private List<ExperimentRunGroup> experimentRunGroups = new ArrayList<>();
    private ExperimentRunGroup currentExperimentRunGroup;
    final private Activity activity;
    final private File storageDirectory;
    private int runGroupId = -1;

    public Experiment(Activity activity, File storageDirectory) {
        this.activity = activity;
        this.storageDirectory = storageDirectory;
    }

    public Activity getActivity() {
        return activity;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("number_of_runs", experimentRunGroups.size());
        if (currentExperimentRunGroup != null)
            outState.putInt("current_run", experimentRunGroups.indexOf(currentExperimentRunGroup));
        int i = 0;
        for (ExperimentRunGroup run : experimentRunGroups) {
            Bundle runBundle = new Bundle();
            run.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        assert(experimentRunGroups.size() == 0);

        int numberOfRuns = savedInstanceState.getInt("number_of_runs", 0);
        for (int i = 0; i < numberOfRuns; i++) {
            String runNumberString = Integer.toString(i);
            Bundle runState = savedInstanceState.getBundle(runNumberString);
            if (runState == null)
                continue;
            ExperimentRunGroup run = new ExperimentRunGroup(this,
                    new File(storageDirectory, "run" + runNumberString));
            run.onRestoreInstanceState(runState);
            addRun(run);
        }

        int currentRunIndex = savedInstanceState.getInt("current_run", -1);
        if (currentRunIndex >= 0)
            setCurrentExperimentRunGroup(experimentRunGroups.get(currentRunIndex));
    }

    public void addRun(ExperimentRunGroup run) {
        if (currentExperimentRunGroup == null)
            currentExperimentRunGroup = run;
        experimentRunGroups.add(run);
    }

    public void setCurrentExperimentRunGroup(ExperimentRunGroup run) {
        currentExperimentRunGroup = run;
    }

    public ExperimentRunGroup getCurrentExperimentRunGroup() {
        return currentExperimentRunGroup;
    }

    public int createRunGroupId() {
        runGroupId++;
        return runGroupId;
    }

    public IExperimentRun getCurrentExperimentRun() {
        ExperimentRunGroup runGroup = getCurrentExperimentRunGroup();
        if (runGroup == null)
            return null;
        return runGroup.getCurrentExperimentRun();
    }
}
