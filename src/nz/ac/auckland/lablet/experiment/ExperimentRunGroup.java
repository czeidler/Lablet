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
    private Experiment experiment;
    private File storageDirectory;

    public File getStorageDir() {
        return storageDirectory;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
        int groupId = experiment.createRunGroupId();
        this.storageDirectory = new File(experiment.getStorageDir(), "run" + Integer.toString(groupId));
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

    public boolean addExperimentRun(IExperimentRun experimentRun) {
        if (experimentRun.getExperimentRunGroup() != null)
            return false;
        experimentRuns.add(experimentRun);
        experimentRun.setExperimentRunGroup(this);
        return true;
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
            IExperimentRun experimentRun = plugin.createExperiment(experiment.getActivity());
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentRun.onRestoreInstanceState(state);
            experimentRuns.add(experimentRun);
        }
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void removeExperimentRun(IExperimentRun experimentRun) {
        // update the current experiment run first if necessary
        if (experimentRun == getCurrentExperimentRun()) {
            int index = experimentRuns.indexOf(experimentRun);
            if (index > 0)
                setCurrentExperimentRun(0);
            else if (index + 1 < experimentRuns.size())
                setCurrentExperimentRun(index + 1);
            else
                setCurrentExperimentRun(null);
        }
        experimentRuns.remove(experimentRun);
    }
}
