package com.example.AndroidPhysicsTracker;


import android.app.Activity;


interface ExperimentPlugin {
    String getName();
    void startExperimentActivity(Activity parentActivity, int requestCode);
}
