package com.example.AndroidPhysicsTracker;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

public class CameraExperimentPlugin implements ExperimentPlugin {
    public String getName() {
        return "Camera Experiment";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void startExperimentActivity(Activity parentActivity, int requestCode) {
        Intent intent = new Intent(parentActivity, CameraExperimentActivity.class);
        parentActivity.startActivityForResult(intent, requestCode);
    }
}
