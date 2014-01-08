package com.example.AndroidPhysicsTracker;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.io.File;


interface ExperimentPlugin {
    String getName();
    void startExperimentActivity(Activity parentActivity, int requestCode);

    /**
     *
     * Can be used config the experiment runs, e.g., the camera experiment uses it to set framerate and video start and end point.
     */
    void startRunSettingsActivity(Experiment experiment, Activity parentActivity, int requestCode);
    /** If an run edit activity exist.
     *
     * @param menuName optional if not null the activity name is stored.
     * @return true if there is run edit is supported
     */
    boolean hasRunEditActivity(StringBuilder menuName);

    Experiment loadExperiment(Context context, Bundle data, File storageDir);
    View createExperimentRunView(Context context, Experiment experiment);
}
