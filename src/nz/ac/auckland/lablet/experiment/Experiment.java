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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
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

    private List<WeakReference<IExperimentListener>> listeners = new ArrayList<>();

    public Experiment(Activity activity, File storageDirectory) {
        this.activity = activity;
        this.storageDirectory = new File(storageDirectory, generateNewUid());
    }

    private String generateNewUid() {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        return "Experiment_" + dateString;
    }

    public void addListener(IExperimentListener listener) {
        listeners.add(new WeakReference<IExperimentListener>(listener));
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
        runGroup.setExperiment(this, "run" + Integer.toString(createRunGroupId()));
        experimentRunGroups.add(runGroup);

        notifyExperimentRunGroupAdded(runGroup);
        return true;
    }

    public void removeExperimentRunGroup(ExperimentRunGroup runGroup) {
        final int removedGroupIndex = experimentRunGroups.indexOf(runGroup);

        runGroup.setExperiment(null, "");
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

    public IExperimentSensor getCurrentExperimentRun() {
        ExperimentRunGroup runGroup = getCurrentExperimentRunGroup();
        if (runGroup == null)
            return null;
        return runGroup.getCurrentExperimentRun();
    }

    public File getStorageDir() {
        return storageDirectory;
    }

    public void finishExperiment(boolean saveData) throws IOException {
        int i = 0;
        for (ExperimentRunGroup experimentRunGroup : experimentRunGroups) {
            experimentRunGroup.finishExperiment(saveData, new File(getStorageDir(), "run" + Integer.toString(i)));
            i++;
        }
    }

    public boolean dataTaken() {
        for (ExperimentRunGroup runGroup : experimentRunGroups) {
            if (runGroup.dataTaken())
                return true;
        }
        return false;
    }

    private List<IExperimentListener> getListeners() {
        List<IExperimentListener> hardListeners = new ArrayList<>();

        Iterator<WeakReference<IExperimentListener>> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            IExperimentListener listener = iterator.next().get();
            if (listener != null)
                hardListeners.add(listener);
            else
                iterator.remove();
        }

        return hardListeners;
    }

    private void notifyCurrentExperimentRunGroupChanged(ExperimentRunGroup runGroup, ExperimentRunGroup oldGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onCurrentRunGroupChanged(runGroup, oldGroup);
    }

    private void notifyExperimentRunGroupAdded(ExperimentRunGroup runGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onExperimentRunGroupAdded(runGroup);
    }

    private void notifyExperimentRunGroupRemoved(ExperimentRunGroup runGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onExperimentRunGroupRemoved(runGroup);
    }
}
