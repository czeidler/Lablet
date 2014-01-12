package com.example.AndroidPhysicsTracker;


import android.os.Bundle;


public class CameraExperimentAnalysis extends ExperimentAnalysis {

    public CameraExperimentAnalysis(Experiment experiment) {
        super(experiment);
    }

    @Override
    protected void onRunSpecificDataChanged() {
        CameraExperiment cameraExperiment = (CameraExperiment)getExperiment();
        Bundle experimentSpecificData = getExperimentSpecificData();
        if (experimentSpecificData == null)
            return;

        Bundle runSettings = experimentSpecificData.getBundle("run_settings");

        cameraExperiment.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
        cameraExperiment.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        cameraExperiment.setFrameRate(runSettings.getInt("analysis_frame_rate"));

        int numberOfRuns = getExperiment().getNumberOfRuns();
        getRunDataModel().setNumberOfRuns(numberOfRuns);
        if (numberOfRuns <= getRunDataModel().getCurrentRun())
            getRunDataModel().setCurrentRun(numberOfRuns - 1);
    }

}
