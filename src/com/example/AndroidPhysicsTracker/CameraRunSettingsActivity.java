package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


public class CameraRunSettingsActivity extends ExperimentActivity {
    private ExperimentPlugin plugin = null;
    private CameraExperiment experiment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadExperiment(getIntent());

        setContentView(R.layout.camera_run_settings);

    }


}