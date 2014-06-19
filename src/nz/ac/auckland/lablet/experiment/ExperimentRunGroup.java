/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ExperimentRunGroup {
    String description = "";
    Bundle runInformation = new Bundle();

    final private List<IExperimentRun> experimentRuns = new ArrayList<>();
    private IExperimentRun currentExperimentRun = null;
    final private Experiment experiment;
    final private File storageDirectory;

    public ExperimentRunGroup(Experiment experiment, File storageDirectory) {
        this.experiment = experiment;
        this.storageDirectory = storageDirectory;
    }

    public int getExperimentRunCount() {
        return experimentRuns.size();
    }

    public void setCurrentExperimentRun(int i) {
        currentExperimentRun = getExperimentRunAt(i);
    }

    public boolean setCurrentExperimentRun(IExperimentRun experimentRun) {
        if (!experimentRuns.contains(experimentRun))
            return false;
        currentExperimentRun = experimentRun;
        return true;
    }

    public IExperimentRun getCurrentExperimentRun() {
        return currentExperimentRun;
    }

    public IExperimentRun getExperimentRunAt(int i) {
        return experimentRuns.get(i);
    }

    public List<IExperimentRun> getExperimentRuns() {
        return experimentRuns;
    }

    public void addExperimentRun(IExperimentRun experimentRun) {
        experimentRuns.add(experimentRun);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bundle getRunInformation() {
        return runInformation;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString("description", getDescription());
        outState.putBundle("runInformation", getRunInformation());

        int i = 0;
        for (IExperimentRun experimentRun : experimentRuns) {
            Bundle runBundle = new Bundle();
            experimentRun.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }

        // store plugin information
        Bundle experimentRunClasses = new Bundle();
        i = 0;
        for (IExperimentRun experimentRun : experimentRuns) {
            experimentRunClasses.putString(Integer.toString(i), experimentRun.getClass().getSimpleName());
            i++;
        }
        outState.putBundle("runClasses", experimentRunClasses);
        outState.putInt("runClassesCount", experimentRuns.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        description = savedInstanceState.getString("description");
        runInformation = savedInstanceState.getBundle("runInformation");

        int runClassesCount = savedInstanceState.getInt("runClassesCount");
        Bundle experimentRunClasses = savedInstanceState.getBundle("runClasses");
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (int i = 0; i < runClassesCount; i++) {
            String runName = experimentRunClasses.getString(Integer.toString(i));
            IExperimentPlugin plugin = factory.findExperimentPlugin(runName);
            IExperimentRun experimentRun = plugin.createExperiment(experiment.getActivity(), storageDirectory);
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentRun.onRestoreInstanceState(state);
            experimentRuns.add(experimentRun);
        }
    }
}
