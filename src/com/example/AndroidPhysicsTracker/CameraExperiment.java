package com.example.AndroidPhysicsTracker;

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
    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    @Override
    public Bundle getRunAt(int i) {
        if (i < 0 || i >= numberOfRuns)
            return null;

        int position = (int)(analysisVideoStart + (float)1000 / analysisFrameRate * i);

        Bundle bundle = new Bundle();
        bundle.putInt("frame_position", position);

        return bundle;
    }

    protected boolean loadExperiment(Bundle bundle, File storageDir) {
        if (!super.loadExperiment(bundle, storageDir))
            return false;

        setVideoFileName(bundle.getString("videoName"));

        setAnalysisVideoStart(bundle.getInt("videoStart"));
        setAnalysisVideoEnd(bundle.getInt("videoEnd"));
        // must be called here (needs video start and end time as well as the duration)
        setFrameRate(bundle.getInt("frameRate"));
        return true;
    }

    public Bundle toBundle() {
        Bundle bundle = super.toBundle();

        bundle.putString("videoName", videoFileName);

        bundle.putInt("frameRate", getAnalysisFrameRate());
        bundle.putInt("videoStart", getAnalysisVideoStart());
        bundle.putInt("videoEnd", getAnalysisVideoEnd());
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
        numberOfRuns = runTime * analysisFrameRate / 1000;
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
