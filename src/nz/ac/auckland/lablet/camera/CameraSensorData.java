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
import nz.ac.auckland.lablet.experiment.AbstractSensorData;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;

import java.io.File;


/**
 * Holds all important data for the camera experiment.
 */
public class CameraSensorData extends AbstractSensorData {
    private String videoFileName;

    // milli seconds
    private int videoDuration;
    private int videoWidth;
    private int videoHeight;

    private float timeLapseCaptureRate = -1f;

    public CameraSensorData(Context experimentContext, IExperimentSensor sourceSensor) {
        super(experimentContext, sourceSensor);
    }

    @Override
    public String getDataType() {
        return "Video";
    }

    public CameraSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);

    }

    public float getMaxRawX() {
        return 100.f;
    }

    public float getMaxRawY() {
        float xToYRatio = (float)videoWidth / videoHeight;
        float xMax = getMaxRawX();
        return xMax / xToYRatio;
    }

    @Override
    public boolean loadExperimentData(Bundle bundle, File storageDir) {
        if (!super.loadExperimentData(bundle, storageDir))
            return false;

        setVideoFileName(storageDir, bundle.getString("videoName"));

        timeLapseCaptureRate = bundle.getFloat("timeLapseCaptureRate", -1);
        return true;
    }

    @Override
    protected Bundle experimentDataToBundle() {
        Bundle bundle = super.experimentDataToBundle();

        bundle.putString("videoName", videoFileName);

        if (timeLapseCaptureRate > 0)
            bundle.putFloat("timeLapseCaptureRate", timeLapseCaptureRate);

        return bundle;
    }

    /**
     * Set the file name of the taken video.
     *
     * @param storageDir directory where the video file is stored
     * @param fileName path of the taken video
     */
    public void setVideoFileName(File storageDir, String fileName) {
        this.videoFileName = fileName;

        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(new File(storageDir, fileName)));

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

    public void setTimeLapseCaptureRate(float timeLapseCaptureRate) {
        this.timeLapseCaptureRate = timeLapseCaptureRate;
    }
}
