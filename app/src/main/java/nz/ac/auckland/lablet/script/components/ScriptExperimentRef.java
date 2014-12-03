/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Context;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
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

    public MotionAnalysis getMotionAnalysis(Context context, int run) {
        return getAnalysis(context, run, "MotionAnalysis");
    }

    public FrequencyAnalysis getFrequencyAnalysis(Context context, int run) {
        return getAnalysis(context, run, "FrequencyAnalysis");
    }

    protected <T extends ISensorAnalysis> T getAnalysis(Context context, int run, String analysisIdentifier) {
        if (experimentAnalysis == null)
            experimentAnalysis = loadExperimentAnalysis(context);
        if (experimentAnalysis == null || experimentAnalysis.getNumberOfRuns() <= run)
            return null;

        ExperimentAnalysis.AnalysisRunEntry experimentRun = experimentAnalysis.getAnalysisRunAt(run);
        for (ExperimentAnalysis.AnalysisDataEntry analysisDataEntry : experimentRun.analysisDataList) {
            for (ExperimentAnalysis.AnalysisEntry analysisEntry : analysisDataEntry.analysisList) {
                if (analysisEntry.analysis.getIdentifier().equals(analysisIdentifier))
                    return (T)(analysisEntry.analysis);
            }
        }
        return null;
    }

    public void reloadExperimentAnalysis(Context context) {
        experimentAnalysis = loadExperimentAnalysis(context);

        for (IListener listener : getListeners())
            listener.onExperimentAnalysisUpdated();
    }

    private ExperimentAnalysis loadExperimentAnalysis(Context context) {
        ExperimentData experimentData = ExperimentHelper.loadExperimentData(context, getExperimentPath());
        if (experimentData == null)
            return null;

        ExperimentAnalysis analysis = new ExperimentAnalysis();
        analysis.setExperimentData(experimentData);
        return analysis;
    }
}
