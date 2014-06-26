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
    public interface IExperimentRunListener {
        public void onStartPreview();
        public void onStopPreview();
        public void onStartRecording();
        public void onStopRecording();
        public void onStartPlayback();
        public void onStopPlayback();
        public void onSettingsChanged();
    }

    public View createExperimentView(Context context);

    /**
     * Prepares a option menu for the experiment.
     *
     * @param menuItem the menu item for the option menu
     * @return false if there is no option menu
     */
    public boolean onPrepareOptionsMenu(MenuItem menuItem);

    public void onSaveInstanceState(Bundle outState);
    public void onRestoreInstanceState(Bundle savedInstanceState);

    public ExperimentRunGroup getExperimentRunGroup();
    public void setExperimentRunGroup(ExperimentRunGroup experimentRunGroup);

    public void init(Activity activity);
    public void destroy();

    public void finishExperiment(boolean saveData, File storageDir) throws IOException;
    public boolean dataTaken();

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
