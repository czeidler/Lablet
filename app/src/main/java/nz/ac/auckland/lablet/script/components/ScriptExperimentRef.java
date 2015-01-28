/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.experiment.IDataAnalysis;
import nz.ac.auckland.lablet.microphone.FrequencyAnalysis;
import nz.ac.auckland.lablet.misc.WeakListenable;


/**
 * Reference to an experiment conducted in the script.
 * Also caches the experiment analysis and notifies listeners if the experiment has been updated.
 */
public class ScriptExperimentRef extends WeakListenable<ScriptExperimentRef.IListener> {
    public interface IListener {
        public void onExperimentAnalysisUpdated();
    }

    private String experimentPath = "";
    private ExperimentAnalysis experimentAnalysis;

    public String getExperimentPath() {
        return experimentPath;
    }
    public void setExperimentPath(String path) {
        experimentPath = path;
    }

    public MotionAnalysis getMotionAnalysis(int run) {
        return getAnalysis(run, "MotionAnalysis");
    }

    public FrequencyAnalysis getFrequencyAnalysis(int run) {
        return getAnalysis(run, "FrequencyAnalysis");
    }

    protected <T extends IDataAnalysis> T getAnalysis(int run, String analysisIdentifier) {
        if (experimentAnalysis == null)
            experimentAnalysis = loadExperimentAnalysis();
        if (experimentAnalysis == null || experimentAnalysis.getNumberOfRuns() <= run)
            return null;

        ExperimentAnalysis.AnalysisRunEntry experimentRun = experimentAnalysis.getAnalysisRunAt(run);
            for (ExperimentAnalysis.AnalysisEntry analysisEntry : experimentRun.analysisList) {
                if (analysisEntry.analysis.getIdentifier().equals(analysisIdentifier))
                    return (T)(analysisEntry.analysis);
            }
        return null;
    }

    public void reloadExperimentAnalysis() {
        experimentAnalysis = loadExperimentAnalysis();

        for (IListener listener : getListeners())
            listener.onExperimentAnalysisUpdated();
    }

    private ExperimentAnalysis loadExperimentAnalysis() {
        ExperimentData experimentData = ExperimentHelper.loadExperimentData(getExperimentPath());
        if (experimentData == null)
            return null;

        ExperimentAnalysis analysis = new ExperimentAnalysis();
        analysis.setExperimentData(experimentData);
        return analysis;
    }
}
