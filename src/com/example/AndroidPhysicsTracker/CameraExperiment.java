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
    private int duration;

    private int frameRate;
    // milli seconds
    private int videoStart;
    private int videoEnd;

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

        int position = (int)(videoStart + (float)1000 / frameRate * i);

        Bundle bundle = new Bundle();
        bundle.putInt("frame_position", position);

        return bundle;
    }

    protected boolean loadExperiment(Bundle bundle, File storageDir) {
        if (!super.loadExperiment(bundle, storageDir))
            return false;

        setVideoFileName(bundle.getString("videoName"));

        setVideoStart(bundle.getInt("videoStart"));
        setVideoEnd(bundle.getInt("videoEnd"));
        // must be called here (needs video start and end time as well as the duration)
        setFrameRate(bundle.getInt("frameRate"));
        return true;
    }

    public Bundle toBundle() {
        Bundle bundle = super.toBundle();

        bundle.putString("videoName", videoFileName);

        bundle.putInt("frameRate", getFrameRate());
        bundle.putInt("videoStart", getVideoStart());
        bundle.putInt("videoEnd", getVideoEnd());
        return bundle;
    }

    public void setVideoFileName(String video) {
        this.videoFileName = video;
        File file = new File(getStorageDir(), videoFileName);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file.getPath()));
        duration = mediaPlayer.getDuration();
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

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int rate) {
        frameRate = rate;
        if (frameRate <= 0)
            frameRate = 10;

        int runTime = videoEnd - videoStart;
        numberOfRuns = runTime * frameRate / 1000;
    }

    public int getVideoStart() {
        return videoStart;
    }

    public void setVideoStart(int videoStart) {
        this.videoStart = videoStart;
    }

    public int getVideoEnd() {
        return videoEnd;
    }

    public void setVideoEnd(int end) {
        videoEnd = end;

        if (videoEnd <= 0)
            videoEnd = duration;
    }
}
