package com.example.AndroidPhysicsTracker;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.View;

import java.io.File;

public class CameraExperimentPlugin implements ExperimentPlugin {
    public String getName() {
        return CameraExperiment.class.getSimpleName();
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

    @Override
    public Experiment loadExperiment(Context context, Bundle data, File storageDir) {
        return new CameraExperiment(context, data, storageDir);
    }

    @Override
    public View createExperimentRunView(Context context, Experiment experiment) {
        return new CameraExperimentRunView(context, experiment);
    }
}
