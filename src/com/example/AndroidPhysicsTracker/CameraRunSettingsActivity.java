package com.example.AndroidPhysicsTracker;

import android.os.Bundle;


public class CameraRunSettingsActivity extends ExperimentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadExperiment(getIntent());

        CameraExperiment cameraExperiment = (CameraExperiment)getExperiment();

        setContentView(R.layout.camera_run_settings);
    }

    @Override
    public void onPause() {

    }
}