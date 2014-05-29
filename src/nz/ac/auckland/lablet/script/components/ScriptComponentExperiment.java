/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;


import android.content.Context;
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentLoader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Reference to an experiment conducted in the script.
 * Also caches the experiment analysis and notifies listeners if the experiment has been updated.
 */
public class ScriptComponentExperiment {
    public interface IScriptComponentExperimentListener {
        public void onExperimentAnalysisUpdated();
    }

    private List<WeakReference<IScriptComponentExperimentListener>> listeners
            = new ArrayList<WeakReference<IScriptComponentExperimentListener>>();
    private String experimentPath = "";
    private ExperimentAnalysis experimentAnalysis;

    public String getExperimentPath() {
        return experimentPath;
    }
    public void setExperimentPath(String path) {
        experimentPath = path;

    }

    public ExperimentAnalysis getExperimentAnalysis(Context context) {
        if (experimentAnalysis == null)
            experimentAnalysis = loadExperimentAnalysis(context);
        return experimentAnalysis;
    }

    public void addListener(IScriptComponentExperimentListener listener) {
        listeners.add(new WeakReference<IScriptComponentExperimentListener>(listener));
    }

    public boolean removeListener(IScriptComponentExperimentListener listener) {
        return listeners.remove(listener);
    }

    public void reloadExperimentAnalysis(Context context) {
        experimentAnalysis = loadExperimentAnalysis(context);
        for (ListIterator<WeakReference<IScriptComponentExperimentListener>> it = listeners.listIterator();
             it.hasNext();) {
            IScriptComponentExperimentListener listener = it.next().get();
            if (listener != null)
                listener.onExperimentAnalysisUpdated();
            else
                it.remove();
        }
    }

    private ExperimentAnalysis loadExperimentAnalysis(Context context) {
        return ExperimentLoader.loadExperimentAnalysis(context, getExperimentPath());
    }
}
