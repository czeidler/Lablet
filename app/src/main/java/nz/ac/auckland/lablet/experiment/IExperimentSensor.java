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


/**
 * Experiment sensor interface to record sensor data.
 */
public interface IExperimentSensor {
    /**
     * Listener interface for the experiment sensor.
     */
    interface IListener {
        void onStartPreview();
        void onStopPreview();
        void onStopRecording();
        void onStartRecording();
        void onStartPlayback();
        void onStopPlayback();
        void onSettingsChanged();
    }

    String getSensorName();

    View createExperimentView(Context context);

    /**
     * Prepares a option menu for the experiment.
     *
     * @param menuItem the menu item for the option menu
     * @return false if there is no option menu
     */
    boolean onPrepareOptionsMenu(MenuItem menuItem);

    void onSaveInstanceState(Bundle outState);
    void onRestoreInstanceState(Bundle savedInstanceState);

    ExperimentRun getExperimentRun();
    void setExperimentRun(ExperimentRun experimentRun);

    void init(Activity activity);
    void destroy();

    /**
     * Finishes the experiment by either saving or discarding the data.
     *
     * We allow a sensor to produce more than one type of data. For example, an external sensor could act as a
     * thermometer and a barometer at the same time. For that reason the sensor is responsible to create one or more
     * directories in the storageBaseDir to store its data.
     *
     * @param saveData flag that indicates if data should be saved
     * @param storageBaseDir the data should be saved into a sub directory of storageBaseDir
     * @throws IOException
     */
    void finishExperiment(boolean saveData, File storageBaseDir) throws IOException;
    boolean dataTaken();

    void startPreview();
    void stopPreview();
    void startRecording() throws Exception;
    /**
     * Stops the recording.
     *
     * @return true if some data has been taken otherwise false
     */
    boolean stopRecording();
    void startPlayback();
    void stopPlayback();

    /**
     * Returns the recorded sensor data.
     *
     * @return the recorded data
     */
    ISensorData getExperimentData();
}
