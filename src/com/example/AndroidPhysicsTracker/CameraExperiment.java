package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;


public class CameraExperiment extends Experiment {
    private String videoFileName = "";
    private int videoHeight;
    private int videoWidth;
    private int numberOfRuns = 0;
    private int duration = 0;

    public CameraExperiment(Context experimentContext) {
        super(experimentContext);
    }

    public CameraExperiment(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);
        setVideoFileName(bundle.getString("videoName"));
    }

    @Override
    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    @Override
    public Bundle getRunAt(int i) {
        if (i < 0 || i >= numberOfRuns)
            return null;

        Bundle bundle = new Bundle();
        bundle.putInt("frame_position", (int)(duration * ((float)i / (numberOfRuns - 1))));

        return bundle;
    }

    public Bundle toBundle() {
        Bundle bundle = super.toBundle();

        bundle.putString("videoName", videoFileName);
        return bundle;
    }

    public void setVideoFileName(String video) {
        if (video == null)
            return;
        videoFileName = video;
        File file = new File(getStorageDir(), videoFileName);

        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(file.getPath()));
        duration = mediaPlayer.getDuration();
        videoHeight = mediaPlayer.getVideoHeight();
        videoWidth = mediaPlayer.getVideoWidth();

        numberOfRuns = 20;
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
}
