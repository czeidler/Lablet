/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.io.File;


interface ExperimentPlugin {
    public String getName();
    public void startExperimentActivity(Activity parentActivity, int requestCode);

    /**
     *
     * Can be used config the experiment runs, e.g., the camera experiment uses it to set framerate and video start and end point.
     */
    public void startRunSettingsActivity(Experiment experiment, Bundle runSpecificData, Activity parentActivity, int requestCode);
    /** If an run edit activity exist.
     *
     * @param menuName optional if not null the activity name is stored.
     * @return true if there is run edit is supported
     */
    public boolean hasRunEditActivity(StringBuilder menuName);

    public Experiment loadExperiment(Context context, Bundle data, File storageDir);
    public ExperimentAnalysis loadExperimentAnalysis(Experiment experiment);

    public View createExperimentRunView(Context context, Experiment experiment);
}
