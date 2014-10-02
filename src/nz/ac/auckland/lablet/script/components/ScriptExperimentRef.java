/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Context;
import nz.ac.auckland.lablet.ExperimentAnalysisBaseActivity;
import nz.ac.auckland.lablet.camera.CameraSensorData;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.io.File;


/**
 * Reference to an experiment conducted in the script.
 * Also caches the experiment analysis and notifies listeners if the experiment has been updated.
 */
public class ScriptExperimentRef extends WeakListenable<ScriptExperimentRef.IListener> {
    public interface IListener {
        public void onExperimentAnalysisUpdated();
    }

    private String experimentPath = "";
    private MotionAnalysis motionAnalysis;

    public String getExperimentPath() {
        return experimentPath;
    }
    public void setExperimentPath(String path) {
        experimentPath = path;
    }

    public MotionAnalysis getVideoAnalysis(Context context) {
        if (motionAnalysis == null)
            motionAnalysis = loadSensorAnalysis(context);
        return motionAnalysis;
    }
    public void reloadExperimentAnalysis(Context context) {
        motionAnalysis = loadSensorAnalysis(context);
        for (IListener listener : getListeners())
            listener.onExperimentAnalysisUpdated();
    }

    private MotionAnalysis loadSensorAnalysis(Context context) {
        ExperimentData experimentData = ExperimentHelper.loadExperimentData(context, getExperimentPath());
        if (experimentData == null)
            return null;
        ISensorData sensorData = experimentData.getRuns().get(0).sensorDataList.get(0);
        if (!(sensorData instanceof CameraSensorData))
            return null;
        MotionAnalysis motionAnalysis = new MotionAnalysis((CameraSensorData)sensorData);
        File analysisDir = ExperimentAnalysisBaseActivity.getAnalysisStorageFor(experimentData, 0, motionAnalysis);
        if (!ExperimentHelper.loadSensorAnalysis(motionAnalysis, analysisDir))
            return null;
        return motionAnalysis;
    }
}
