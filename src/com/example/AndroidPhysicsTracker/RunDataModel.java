package com.example.AndroidPhysicsTracker;

import java.util.ArrayList;
import java.util.List;

class RunDataModel {
    interface IRunDataModelListener {
        public void onRunChanged(int newRun);
        public void onNumberOfRunsChanged();
    }

    private int currentRun;
    private int numberOfRuns;

    private List<IRunDataModelListener> listeners;

    public RunDataModel() {
        listeners = new ArrayList<IRunDataModelListener>();
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