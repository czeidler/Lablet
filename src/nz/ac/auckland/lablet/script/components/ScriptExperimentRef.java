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
import nz.ac.auckland.lablet.camera.VideoAnalysis;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentLoader;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Reference to an experiment conducted in the script.
 * Also caches the experiment analysis and notifies listeners if the experiment has been updated.
 */
public class ScriptExperimentRef {
    public interface IScriptExperimentRefListener {
        public void onExperimentAnalysisUpdated();
    }

    private List<WeakReference<IScriptExperimentRefListener>> listeners = new ArrayList<>();
    private String experimentPath = "";
    private VideoAnalysis videoAnalysis;

    public String getExperimentPath() {
        return experimentPath;
    }
    public void setExperimentPath(String path) {
        experimentPath = path;
    }

    public VideoAnalysis getVideoAnalysis(Context context) {
        if (videoAnalysis == null)
            videoAnalysis = loadSensorAnalysis(context);
        return videoAnalysis;
    }

    public void addListener(IScriptExperimentRefListener listener) {
        listeners.add(new WeakReference<>(listener));
    }

    public boolean removeListener(IScriptExperimentRefListener listener) {
        return listeners.remove(listener);
    }

    public void reloadExperimentAnalysis(Context context) {
        videoAnalysis = loadSensorAnalysis(context);
        for (ListIterator<WeakReference<IScriptExperimentRefListener>> it = listeners.listIterator();
             it.hasNext();) {
            IScriptExperimentRefListener listener = it.next().get();
            if (listener != null)
                listener.onExperimentAnalysisUpdated();
            else
                it.remove();
        }
    }

    private VideoAnalysis loadSensorAnalysis(Context context) {
        ExperimentData experimentData = new ExperimentData();
        if (!experimentData.load(context, new File(getExperimentPath())))
            return null;
        SensorData sensorData = experimentData.getRuns().get(0).sensorDataList.get(0);
        if (!(sensorData instanceof CameraSensorData))
            return null;
        VideoAnalysis videoAnalysis = new VideoAnalysis((CameraSensorData)sensorData);
        File analysisDir = ExperimentAnalysisBaseActivity.getAnalysisStorageFor(0, videoAnalysis);
        if (!ExperimentLoader.loadSensorAnalysis(videoAnalysis, analysisDir))
            return null;
        return videoAnalysis;
    }
}
