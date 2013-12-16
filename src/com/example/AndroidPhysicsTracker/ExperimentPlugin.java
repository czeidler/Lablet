package com.example.AndroidPhysicsTracker;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.io.File;


interface ExperimentPlugin {
    String getName();
    void startExperimentActivity(Activity parentActivity, int requestCode);

    Experiment loadExperiment(Bundle data, File storageDir);
    View createExperimentRunView(Context context, Experiment experiment);
}
