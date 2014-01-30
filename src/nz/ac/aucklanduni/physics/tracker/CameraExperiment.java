/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;


public class CameraExperiment extends Experiment {
    private String videoFileName;
    private int videoHeight;
    private int videoWidth;
    private int numberOfRuns;

    // milli seconds
    private int videoDuration;

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
    public String getXUnit() {
        return "m";
    }

    @Override
    public String getYUnit() {
        return "m";
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

    protected boolean loadExperiment(Bundle bundle, File storageDir) {
        if (!super.loadExperiment(bundle, storageDir))
            return false;

        setVideoFileName(bundle.getString("videoName"));

        setAnalysisVideoStart(0);
        setAnalysisVideoEnd(0);
        // must be called here (needs video start and end time as well as the duration)
        setFrameRate(0);
        return true;
    }

    public Bundle experimentDataToBundle() {
        Bundle bundle = super.experimentDataToBundle();

        bundle.putString("videoName", videoFileName);

        return bundle;
    }

    public void setVideoFileName(String video) {
        this.videoFileName = video;
        File file = new File(getStorageDir(), videoFileName);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file.getPath()));

        videoDuration = mediaPlayer.getDuration();
        videoHeight = mediaPlayer.getVideoHeight();
        videoWidth = mediaPlayer.getVideoWidth();

        mediaPlayer.release();
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getAnalysisFrameRate() {
        return analysisFrameRate;
    }

    public void setFrameRate(int rate) {
        analysisFrameRate = rate;
        if (analysisFrameRate <= 0)
            analysisFrameRate = 10;

        int runTime = analysisVideoEnd - analysisVideoStart;
        numberOfRuns = runTime * analysisFrameRate / 1000 + 1;
    }

    public int getVideoDuration() {
        return videoDuration;
    }

    public int getAnalysisVideoStart() {
        return analysisVideoStart;
    }

    public void setAnalysisVideoStart(int videoStart) {
        this.analysisVideoStart = videoStart;
    }

    public int getAnalysisVideoEnd() {
        return analysisVideoEnd;
    }

    public void setAnalysisVideoEnd(int end) {
        analysisVideoEnd = end;

        if (analysisVideoEnd <= 0)
            analysisVideoEnd = videoDuration;
    }
}
