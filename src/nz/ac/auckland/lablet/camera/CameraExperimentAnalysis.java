/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.graphics.PointF;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;


/**
 * Class for the camera experiment analysis.
 */
public class CameraExperimentAnalysis extends ExperimentAnalysis {

    public CameraExperimentAnalysis(Experiment experiment) {
        super(experiment);

        updateOriginFromVideoRotation();
    }

    private void updateOriginFromVideoRotation() {
        CameraExperiment cameraExperiment = (CameraExperiment)getExperiment();
        Calibration calibration = getCalibration();

        // read rotation from video
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(cameraExperiment.getVideoFile().getPath());
        String rotationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        PointF origin = new PointF();
        origin.set(calibration.getOrigin());
        float xOffset = origin.x;
        float yOffset = origin.y;
        PointF axis1 = new PointF();
        axis1.set(calibration.getAxis1());
        switch (rotationString) {
            case "90":
                origin.x = cameraExperiment.getMaxRawX() - xOffset;
                origin.y = yOffset;
                axis1.x = origin.x;
                axis1.y = origin.y + 10;
                break;
            case "180":
                origin.x = cameraExperiment.getMaxRawX() - xOffset;
                origin.y = cameraExperiment.getMaxRawY() - yOffset;
                axis1.x = origin.x - 10;
                axis1.y = origin.y;
                break;
            case "270":
                origin.x = xOffset;
                origin.y = cameraExperiment.getMaxRawY() - yOffset;
                axis1.x = origin.x;
                axis1.y = origin.y - 10;
                break;
        }

        setOrigin(origin, axis1);
    }

    @Override
    protected void onRunSpecificDataChanged() {
        CameraExperiment cameraExperiment = (CameraExperiment)getExperiment();
        Bundle experimentSpecificData = getExperimentSpecificData();
        if (experimentSpecificData == null)
            return;
        Bundle runSettings = experimentSpecificData.getBundle("run_settings");
        if (runSettings == null)
            return;

        cameraExperiment.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
        cameraExperiment.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        cameraExperiment.setAnalysisFrameRate(runSettings.getInt("analysis_frame_rate"));

        int numberOfRuns = getExperiment().getNumberOfRuns();
        getRunDataModel().setNumberOfRuns(numberOfRuns);
        if (numberOfRuns <= getRunDataModel().getCurrentRun())
            getRunDataModel().setCurrentRun(numberOfRuns - 1);
    }
}
