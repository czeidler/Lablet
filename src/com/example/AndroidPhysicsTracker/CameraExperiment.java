package com.example.AndroidPhysicsTracker;

import android.os.Bundle;

import java.io.File;

/**
 * Created by lec on 16/12/13.
 */
public class CameraExperiment extends Experiment {
    private String videoFileName = "video.3gpp";

    public CameraExperiment(Bundle bundle, File storageDir) {
        super(bundle, storageDir);
        videoFileName = bundle.getString("videoName");
    }

    @Override
    public int getNumberOfRuns() {
        return 0;
    }

    @Override
    public ExperimentRun getRunAt(int i) {
        return null;
    }

    public Bundle toBundle() {
        Bundle bundle = super.toBundle();

        bundle.putString("videoName", getVideoName());
        return bundle;
    }

    public String getVideoName() {
        return videoFileName;
    }
}
