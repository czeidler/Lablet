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
        public void onExperimentRunGroupAdded(ExperimentRun runGroup);
        public void onExperimentRunGroupRemoved(ExperimentRun runGroup);
        public void onCurrentRunGroupChanged(ExperimentRun newGroup, ExperimentRun oldGroup);
    }

    final private List<ExperimentRun> experimentRuns = new ArrayList<>();
    private ExperimentRun currentExperimentRun;
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
        outState.putInt("number_of_runs", experimentRuns.size());
        if (currentExperimentRun != null)
            outState.putInt("current_run", experimentRuns.indexOf(currentExperimentRun));
        int i = 0;
        for (ExperimentRun run : experimentRuns) {
            Bundle runBundle = new Bundle();
            run.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        assert(experimentRuns.size() == 0);

        int numberOfRuns = savedInstanceState.getInt("number_of_runs", 0);
        for (int i = 0; i < numberOfRuns; i++) {
            String runNumberString = Integer.toString(i);
            Bundle runState = savedInstanceState.getBundle(runNumberString);
            if (runState == null)
                continue;
            ExperimentRun run = new ExperimentRun();
            addExperimentRunGroup(run);
            run.onRestoreInstanceState(runState);
        }

        int currentRunIndex = savedInstanceState.getInt("current_run", -1);
        if (currentRunIndex >= 0)
            setCurrentExperimentRun(experimentRuns.get(currentRunIndex));
    }

    public List<ExperimentRun> getExperimentRuns() {
        return experimentRuns;
    }

    public boolean addExperimentRunGroup(ExperimentRun runGroup) {
        if (runGroup.getExperiment() != null)
            return false;
        if (currentExperimentRun == null)
            currentExperimentRun = runGroup;
        runGroup.setExperiment(this, "run" + Integer.toString(createRunGroupId()));
        experimentRuns.add(runGroup);

        notifyExperimentRunGroupAdded(runGroup);
        return true;
    }

    public void removeExperimentRunGroup(ExperimentRun runGroup) {
        final int removedGroupIndex = experimentRuns.indexOf(runGroup);

        runGroup.setExperiment(null, "");
        experimentRuns.remove(runGroup);

        // find new current group if runGroup was the current group
        if (runGroup == currentExperimentRun) {
            if (removedGroupIndex > 0)
                setCurrentExperimentRun(experimentRuns.get(removedGroupIndex - 1));
            else if (experimentRuns.size() > removedGroupIndex)
                setCurrentExperimentRun(experimentRuns.get(removedGroupIndex));
            else
                setCurrentExperimentRun(null);
        }

        notifyExperimentRunGroupRemoved(runGroup);
    }

    public void setCurrentExperimentRun(ExperimentRun runGroup) {
        ExperimentRun oldGroup = currentExperimentRun;
        currentExperimentRun = runGroup;

        notifyCurrentExperimentRunGroupChanged(runGroup, oldGroup);
    }

    public ExperimentRun getCurrentExperimentRun() {
        return currentExperimentRun;
    }

    public int createRunGroupId() {
        runGroupId++;
        return runGroupId;
    }

    public IExperimentSensor getCurrentExperimentSensor() {
        ExperimentRun runGroup = getCurrentExperimentRun();
        if (runGroup == null)
            return null;
        return runGroup.getCurrentExperimentSensor();
    }

    public File getStorageDir() {
        return storageDirectory;
    }

    public void finishExperiment(boolean saveData) throws IOException {
        int i = 0;
        for (ExperimentRun experimentRun : experimentRuns) {
            experimentRun.finishExperiment(saveData, new File(getStorageDir(), "run" + Integer.toString(i)));
            i++;
        }
    }

    public boolean dataTaken() {
        for (ExperimentRun runGroup : experimentRuns) {
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

    private void notifyCurrentExperimentRunGroupChanged(ExperimentRun runGroup, ExperimentRun oldGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onCurrentRunGroupChanged(runGroup, oldGroup);
    }

    private void notifyExperimentRunGroupAdded(ExperimentRun runGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onExperimentRunGroupAdded(runGroup);
    }

    private void notifyExperimentRunGroupRemoved(ExperimentRun runGroup) {
        for (IExperimentListener listener : getListeners())
            listener.onExperimentRunGroupRemoved(runGroup);
    }
}
