package com.example.AndroidPhysicsTracker;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


public class CameraExperimentAnalysis extends ExperimentAnalysis {

    private class MarkerInfo {
        public float runValue;
        public MarkerData data;

        public MarkerInfo(float runValue, MarkerData data) {
            this.runValue = runValue;
            this.data = data;
        }
    }

    public CameraExperimentAnalysis(Experiment experiment) {
        super(experiment);
    }

    @Override
    protected void onRunSpecificDataChanged() {
        List<MarkerInfo> oldMarkers = new ArrayList<MarkerInfo>();
        MarkersDataModel tagMarkers = getTagMarkers();
        for (int i = 0; i < tagMarkers.getMarkerCount(); i++)
            oldMarkers.add(new MarkerInfo(getExperiment().getRunValueAt(i), tagMarkers.getMarkerDataAt(i)));

        CameraExperiment cameraExperiment = (CameraExperiment)getExperiment();
        Bundle experimentSpecificData = getExperimentSpecificData();
        if (experimentSpecificData == null)
            return;
        Bundle runSettings = experimentSpecificData.getBundle("run_settings");
        if (runSettings == null)
            return;

        cameraExperiment.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
        cameraExperiment.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        cameraExperiment.setFrameRate(runSettings.getInt("analysis_frame_rate"));

        int numberOfRuns = getExperiment().getNumberOfRuns();
        getRunDataModel().setNumberOfRuns(numberOfRuns);
        if (numberOfRuns <= getRunDataModel().getCurrentRun())
            getRunDataModel().setCurrentRun(numberOfRuns - 1);

        updateTagMarkers(oldMarkers);
    }

    /**
     * Try to match existing markers to new run ids.
     *
     * @param oldMarkers list of markers before the run ids changed
     */
    private void updateTagMarkers(List<MarkerInfo> oldMarkers) {
        MarkersDataModel tagMarkers = getTagMarkers();

        // remove additional marker
        for (int i = 0; i < tagMarkers.getMarkerCount(); i++) {
            MarkerData data = getTagMarkers().getMarkerDataAt(i);
            if (data.getRunId() >= getExperiment().getNumberOfRuns()) {
                tagMarkers.removeMarkerData(i);
                i--;
            }
        }

        /* more intelligent approach; not working though...
        tagMarkers.clear();
        if (getExperiment().getNumberOfRuns() <= oldMarkers.size()) {
            // e.g., frame rate decreased
            for (int i = 0; i < getExperiment().getNumberOfRuns(); i++) {
                float newRunValue = getExperiment().getRunValueAt(i);
                float minDiff = Float.MAX_VALUE;
                MarkerData bestMatch = null;
                for (MarkerInfo info : oldMarkers) {
                    float diff = Math.abs(info.runValue - newRunValue);
                    if (diff < minDiff) {
                        minDiff = diff;
                        bestMatch = info.data;
                        bestMatch.setRunId(i);
                    }
                }
                tagMarkers.addMarkerData(bestMatch);
            }
        } else {
            for (MarkerInfo info : oldMarkers) {
                float runValue = info.runValue;
                float minDiff = Float.MAX_VALUE;
                for (int i = 0; i < getExperiment().getNumberOfRuns(); i++) {
                    float diff = Math.abs(runValue - getExperiment().getRunValueAt(i));
                    if (diff < minDiff) {
                        minDiff = diff;
                        info.data.setRunId(i);
                    }

                }
                tagMarkers.addMarkerData(info.data);
            }
        }*/
    }
}
