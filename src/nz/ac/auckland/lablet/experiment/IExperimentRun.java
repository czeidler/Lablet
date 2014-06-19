/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.IOException;


public interface IExperimentRun {
    public interface IExperimentParent {
        public void startEditingSettings();
        public void finishEditingSettings();
    }

    public View createExperimentView(Context context);

    /**
     * Prepares a option menu for the experiment.
     *
     * @param menuItem the menu item for the option menu
     * @param parent the host for the experiment
     * @return false if there is no option menu
     */
    public boolean onPrepareOptionsMenu(MenuItem menuItem, IExperimentParent parent);

    public void onSaveInstanceState(Bundle outState);
    public void onRestoreInstanceState(Bundle savedInstanceState);

    public ExperimentRunGroup getExperimentRunGroup();
    public void setExperimentRunGroup(ExperimentRunGroup experimentRunGroup);

    public void init(Activity activity, File experimentBaseDir);
    public void destroy();

    public void finish(boolean discardExperiment) throws IOException;

    public void startPreview();
    public void stopPreview();
    public void startRecording() throws Exception;
    /**
     * Stops the recording.
     *
     * @return true if some data has been taken otherwise false
     */
    public boolean stopRecording();
    public void startPlayback();
    public void stopPlayback();

    public ExperimentRunData getExperimentData();
}
