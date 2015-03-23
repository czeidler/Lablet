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
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Main class to perform an experiment.
 *
 * An experiment can have multiple runs and each run can have multiple sensors.
 *
 * A run can be used to perform a similar experiment multiple times.
 * For example, measuring a value multiple times.
 * For that reason each run should have the same set of sensors.
 */
public class Experiment extends WeakListenable<Experiment.IListener> {
    public interface IListener {
        public void onExperimentRunAdded(ExperimentRun runGroup);
        public void onExperimentRunRemoved(ExperimentRun runGroup);
        public void onCurrentRunChanged(ExperimentRun newGroup, ExperimentRun oldGroup);
    }

    final private List<ExperimentRun> experimentRuns = new ArrayList<>();
    private ExperimentRun currentExperimentRun;
    final private Activity activity;

    final static private String NUMBER_OF_RUNS_KEY = "number_of_runs";
    final static private String CURRENT_RUN_KEY = "current_run";

    public Experiment(Activity activity) {
        this.activity = activity;
    }

    /**
     * Generated a uid string containing the date and all sensors of the first run.
     *
     * @return a uid string.
     */
    public String generateNewUid() {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_HH-mm-ss", new java.util.Date());

        String sensorsString = "";
        if (experimentRuns.size() > 0) {
            List<IExperimentSensor> sensorList = experimentRuns.get(0).getExperimentSensors();
            for (IExperimentSensor sensor : sensorList)
                sensorsString += "_" + sensor.getSensorName();
        }
        return dateString + sensorsString;
    }

    public Activity getActivity() {
        return activity;
    }

    /**
     * Saves the experiment state and the state of all runs to a bundle.
     *
     * @param outState the bundle to write the state to
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(NUMBER_OF_RUNS_KEY, experimentRuns.size());
        if (currentExperimentRun != null)
            outState.putInt(CURRENT_RUN_KEY, experimentRuns.indexOf(currentExperimentRun));
        int i = 0;
        for (ExperimentRun run : experimentRuns) {
            Bundle runBundle = new Bundle();
            run.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }
    }

    /**
     * Restores the experiment state and the state of all runs.
     *
     * @param savedInstanceState the bundle that holds the state information
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        assert(experimentRuns.size() == 0);

        int numberOfRuns = savedInstanceState.getInt(NUMBER_OF_RUNS_KEY, 0);
        for (int i = 0; i < numberOfRuns; i++) {
            String runNumberString = Integer.toString(i);
            Bundle runState = savedInstanceState.getBundle(runNumberString);
            if (runState == null)
                continue;
            ExperimentRun run = new ExperimentRun();
            addExperimentRun(run);
            run.onRestoreInstanceState(runState);
        }

        int currentRunIndex = savedInstanceState.getInt(CURRENT_RUN_KEY, -1);
        if (currentRunIndex >= 0)
            setCurrentExperimentRun(experimentRuns.get(currentRunIndex));
    }

    public List<ExperimentRun> getExperimentRuns() {
        return experimentRuns;
    }

    public boolean addExperimentRun(ExperimentRun experimentRun) {
        if (experimentRun.getExperiment() != null)
            return false;
        if (currentExperimentRun == null)
            currentExperimentRun = experimentRun;
        experimentRun.setExperiment(this);
        experimentRuns.add(experimentRun);

        notifyExperimentRunAdded(experimentRun);
        return true;
    }

    public void removeExperimentRun(ExperimentRun experimentRun) {
        final int removedGroupIndex = experimentRuns.indexOf(experimentRun);

        experimentRun.setExperiment(null);
        experimentRuns.remove(experimentRun);

        // find new current group if runGroup was the current group
        if (experimentRun == currentExperimentRun) {
            if (removedGroupIndex > 0)
                setCurrentExperimentRun(experimentRuns.get(removedGroupIndex - 1));
            else if (experimentRuns.size() > removedGroupIndex)
                setCurrentExperimentRun(experimentRuns.get(removedGroupIndex));
            else
                setCurrentExperimentRun(null);
        }

        notifyExperimentRunRemoved(experimentRun);
    }

    /**
     * Mark a run as current.
     *
     * @param experimentRun
     */
    public void setCurrentExperimentRun(ExperimentRun experimentRun) {
        ExperimentRun oldRun = currentExperimentRun;
        currentExperimentRun = experimentRun;

        notifyCurrentExperimentRunChanged(experimentRun, oldRun);
    }

    public ExperimentRun getCurrentExperimentRun() {
        return currentExperimentRun;
    }

    /**
     * Get the current sensor of the current run.
     *
     * @return the current sensor of the current run
     */
    public IExperimentSensor getCurrentExperimentSensor() {
        ExperimentRun runGroup = getCurrentExperimentRun();
        if (runGroup == null)
            return null;
        return runGroup.getCurrentExperimentSensor();
    }

    /**
     * Finishes the experiment and all its runs.
     *
     * This means either the taken data (if any) is saved of discarded.
     *
     * @param saveData indicates if taken data should be saved or discarded
     * @param storageDir the directory where to save the experiment data
     * @throws IOException
     */
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        storageDir = new File(storageDir, "data");
        int i = 0;
        for (ExperimentRun experimentRun : experimentRuns) {
            experimentRun.finishExperiment(saveData, new File(storageDir, "run" + Integer.toString(i)));
            i++;
        }
    }

    /**
     * Indicates if at least one run has recorded some data.
     *
     * @return true if data has been taken in at least one run
     */
    public boolean dataTaken() {
        for (ExperimentRun run : experimentRuns) {
            if (run.dataTaken())
                return true;
        }
        return false;
    }

    private void notifyCurrentExperimentRunChanged(ExperimentRun runGroup, ExperimentRun oldGroup) {
        for (IListener listener : getListeners())
            listener.onCurrentRunChanged(runGroup, oldGroup);
    }

    private void notifyExperimentRunAdded(ExperimentRun runGroup) {
        for (IListener listener : getListeners())
            listener.onExperimentRunAdded(runGroup);
    }

    private void notifyExperimentRunRemoved(ExperimentRun runGroup) {
        for (IListener listener : getListeners())
            listener.onExperimentRunRemoved(runGroup);
    }
}
