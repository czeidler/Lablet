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
import java.util.List;


public class Experiment {
    public interface IExperimentListener {
        public void onExperimentRunGroupAdded(ExperimentRunGroup runGroup);
        public void onExperimentRunGroupRemoved(ExperimentRunGroup runGroup);
        public void onCurrentRunGroupChanged(ExperimentRunGroup newGroup, ExperimentRunGroup oldGroup);
    }

    final private List<ExperimentRunGroup> experimentRunGroups = new ArrayList<>();
    private ExperimentRunGroup currentExperimentRunGroup;
    final private Activity activity;
    final private File storageDirectory;
    private int runGroupId = -1;

    private IExperimentListener listener = null;

    public Experiment(Activity activity, File storageDirectory) {
        this.activity = activity;
        this.storageDirectory = storageDirectory;
    }

    public void setListener(IExperimentListener listener) {
        this.listener = listener;
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
            ExperimentRunGroup run = new ExperimentRunGroup();
            addExperimentRunGroup(run);
            run.onRestoreInstanceState(runState);
        }

        int currentRunIndex = savedInstanceState.getInt("current_run", -1);
        if (currentRunIndex >= 0)
            setCurrentExperimentRunGroup(experimentRunGroups.get(currentRunIndex));
    }

    public List<ExperimentRunGroup> getExperimentRunGroups() {
        return experimentRunGroups;
    }

    public boolean addExperimentRunGroup(ExperimentRunGroup runGroup) {
        if (runGroup.getExperiment() != null)
            return false;
        if (currentExperimentRunGroup == null)
            currentExperimentRunGroup = runGroup;
        runGroup.setExperiment(this);
        experimentRunGroups.add(runGroup);

        notifyExperimentRunGroupAdded(runGroup);
        return true;
    }

    public void removeExperimentRunGroup(ExperimentRunGroup runGroup) {
        final int removedGroupIndex = experimentRunGroups.indexOf(runGroup);

        runGroup.setExperiment(null);
        experimentRunGroups.remove(runGroup);

        // find new current group if runGroup was the current group
        if (runGroup == currentExperimentRunGroup) {
            if (removedGroupIndex > 0)
                setCurrentExperimentRunGroup(experimentRunGroups.get(removedGroupIndex - 1));
            else if (experimentRunGroups.size() > removedGroupIndex)
                setCurrentExperimentRunGroup(experimentRunGroups.get(removedGroupIndex));
            else
                setCurrentExperimentRunGroup(null);
        }

        notifyExperimentRunGroupRemoved(runGroup);
    }

    public void setCurrentExperimentRunGroup(ExperimentRunGroup runGroup) {
        ExperimentRunGroup oldGroup = currentExperimentRunGroup;
        currentExperimentRunGroup = runGroup;

        notifyCurrentExperimentRunGroupChanged(runGroup, oldGroup);
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

    public File getStorageDir() {
        return storageDirectory;
    }

    private void notifyCurrentExperimentRunGroupChanged(ExperimentRunGroup runGroup, ExperimentRunGroup oldGroup) {
        if (listener != null)
            listener.onCurrentRunGroupChanged(runGroup, oldGroup);
    }

    private void notifyExperimentRunGroupAdded(ExperimentRunGroup runGroup) {
        if (listener != null)
            listener.onExperimentRunGroupAdded(runGroup);
    }

    private void notifyExperimentRunGroupRemoved(ExperimentRunGroup runGroup) {
        if (listener != null)
            listener.onExperimentRunGroupRemoved(runGroup);
    }
}
