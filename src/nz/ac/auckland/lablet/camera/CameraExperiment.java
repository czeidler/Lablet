/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.Experiment;

import java.io.File;


/**
 * Holds all important data for the camera experiment.
 */
public class CameraExperiment extends Experiment {
    private String videoFileName;
    private int numberOfRuns;

    // milli seconds
    private int videoDuration;
    private int videoWidth;
    private int videoHeight;

    private int analysisFrameRate;
    // milli seconds
    private int analysisVideoStart;
    private int analysisVideoEnd;

    public CameraExperiment(Context experimentContext) {
        super(experimentContext);
    }

    public CameraExperiment(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);

    }

    @Override
    public String getXBaseUnit() {
        return "m";
    }

    @Override
    public String getYBaseUnit() {
        return "m";
    }

    @Override
    public float getMaxRawX() {
        return 100.f;
    }

    @Override
    public float getMaxRawY() {
        float xToYRatio = (float)videoWidth / videoHeight;
        float xMax = getMaxRawX();
        return xMax / xToYRatio;
    }

    @Override
    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    @Override
    public Bundle getRunAt(int i) {
        if (i < 0 || i >= numberOfRuns)
            return null;

        int position = (int)getRunValueAt(i);

        Bundle bundle = new Bundle();
        bundle.putInt("frame_position", position);

        return bundle;
    }

    @Override
    public float getRunValueAt(int i) {
        return analysisVideoStart + (float)1000 / analysisFrameRate * i;
    }

    @Override
    public String getRunValueBaseUnit() {
        return "s";
    }

    @Override
    public String getRunValueUnitPrefix() {
        return "m";
    }

    @Override
    public String getRunValueLabel() {
        return "time [" + getRunValueUnit() + "]";
    }

    protected boolean loadExperiment(Bundle bundle, File storageDir) {
        if (!super.loadExperiment(bundle, storageDir))
            return false;

        setVideoFileName(bundle.getString("videoName"));

        setAnalysisVideoStart(0);
        setAnalysisVideoEnd(0);
        // must be called here (needs video start and end time as well as the duration)
        setAnalysisFrameRate(0);
        return true;
    }

    public Bundle experimentDataToBundle() {
        Bundle bundle = super.experimentDataToBundle();

        bundle.putString("videoName", videoFileName);

        return bundle;
    }

    /**
     * Set the file name of the taken video.
     *
     * @param filename of the taken video
     */
    public void setVideoFileName(String filename) {
        this.videoFileName = filename;

        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(getVideoFile().getPath()));

        videoDuration = mediaPlayer.getDuration();
        videoWidth = mediaPlayer.getVideoWidth();
        videoHeight = mediaPlayer.getVideoHeight();

        mediaPlayer.release();
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    /**
     * Gets the complete path of the video file.
     *
     * @return the path of the taken video
     */
    public File getVideoFile() {
        return new File(getStorageDir(), getVideoFileName());
    }

    /**
     * The complete duration of the recorded video.
     *
     * @return the duration of the recorded video
     */
    public int getVideoDuration() {
        return videoDuration;
    }

    /**
     * Gets the frame rate at which the video should be analysed.
     *
     * @return the analysis frame rate
     */
    public int getAnalysisFrameRate() {
        return analysisFrameRate;
    }

    /**
     * Set the frame rate at which the video should be analysed.
     * <p>
     * This method should only be called from the {@link nz.ac.auckland.lablet.camera.CameraExperimentAnalysis}. The
     * frame rate determined how many runs are returned by the experiments, i.e., a higher frame rate results in more
     * runs.
     * </p>
     * <p>
     * The analysis frame rate should be smaller or equal to the frame rate of the recorded video. Furthermore, the
     * video frame rate should be dividable byte the analysis frame rate. For example, if the video is recorded with
     * 30fps allowed analysis frame rates are 30, 15, 10, 5, 3, 2, 1.
     * </p>
     * @param frameRate
     */
    public void setAnalysisFrameRate(int frameRate) {
        analysisFrameRate = frameRate;
        if (analysisFrameRate <= 0)
            analysisFrameRate = 10;

        int runTime = analysisVideoEnd - analysisVideoStart;
        numberOfRuns = runTime * analysisFrameRate / 1000 + 1;
    }

    /**
     * Gets the start time at which the video should be analysed.
     *
     * @return the time when the analysis starts
     */
    public int getAnalysisVideoStart() {
        return analysisVideoStart;
    }

    /**
     * Sets the start time of the video analysis.
     * <p>
     * This value should only be set by {@link nz.ac.auckland.lablet.camera.CameraExperimentAnalysis}. The analysis
     * start point affects the number of returned runs.
     * </p>
     * @param startTime the time for the analysis start
     */
    public void setAnalysisVideoStart(int startTime) {
        this.analysisVideoStart = startTime;
    }

    /**
     * Gets the end time for the video analysed.
     *
     * @return the video analysis end time
     */
    public int getAnalysisVideoEnd() {
        return analysisVideoEnd;
    }

    /**
     * Sets the end time for the video analysis.
     * <p>
     * This value should only be set by {@link nz.ac.auckland.lablet.camera.CameraExperimentAnalysis}. The analysis
     * end point affects the number of returned runs.
     * </p>
     * @param endTime the time for the analysis end
     */
    public void setAnalysisVideoEnd(int endTime) {
        analysisVideoEnd = endTime;

        if (analysisVideoEnd <= 0)
            analysisVideoEnd = videoDuration;
    }
}
