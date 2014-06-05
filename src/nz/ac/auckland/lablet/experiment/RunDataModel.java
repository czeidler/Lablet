/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.util.ArrayList;
import java.util.List;


/**
 * Data model for the set of runs of an experiment.
 */
public class RunDataModel {
    public interface IRunDataModelListener {
        public void onRunChanged(int newRun);
        public void onNumberOfRunsChanged();
    }

    private int currentRun;
    private int numberOfRuns;

    private List<IRunDataModelListener> listeners;

    public RunDataModel() {
        listeners = new ArrayList<>();
    }

    public void addListener(IRunDataModelListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(IRunDataModelListener listener) {
        return listeners.remove(listener);
    }

    public int getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(int currentRun) {
        if (currentRun < 0 || currentRun >= getNumberOfRuns())
            return;

        this.currentRun = currentRun;
        notifyRunChanged(currentRun);
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
        notifyNumberOfRunsChanged();
    }

    private void notifyRunChanged(int currentRun) {
        for (IRunDataModelListener listener : listeners)
            listener.onRunChanged(currentRun);
    }

    private void notifyNumberOfRunsChanged() {
        for (IRunDataModelListener listener : listeners)
            listener.onNumberOfRunsChanged();
    }
}