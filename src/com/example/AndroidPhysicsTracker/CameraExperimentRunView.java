package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class CameraExperimentRunView extends VideoView {
    CameraExperiment experiment;

    public CameraExperimentRunView(Context context, Experiment experiment) {
        super(context);

        assert(experiment instanceof CameraExperiment);
        this.experiment = (CameraExperiment)experiment;

        File storageDir = this.experiment.getStorageDir();
        File videoFile = new File(storageDir, this.experiment.getVideoName());

        MediaController mediaController = new MediaController(context);
        mediaController.setKeepScreenOn(true);
        setMediaController(mediaController);
        setVideoPath(videoFile.getPath());
        requestFocus();
        start();
    }


}
