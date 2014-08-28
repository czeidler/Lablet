/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import java.io.File;


/**
 * Plugin interface for experiments.
 */
public interface IExperimentPlugin {
    /**
     * The name of the plugin. For example, the name of the experiment class.
     *
     * @return the name of the plugin
     */
    public String getName();

    public IExperimenter getExperimenter();
    public IAnalysis getAnalysis();

    public interface IExperimenter {
        /**
         * Creates an experiment run object.
         * <p>
         * The activity intent can hold the following options:
         * <ul>
         * <li>bundle field "options" -> options</li>
         * </ul>
         * </p>
         *
         * @param parentActivity the parent activity
         * @return the experiment run
         */
        public IExperimentSensor createExperimentSensor(Activity parentActivity);

        /**
         * Starts an activity to config the experiment sensors.
         * <p>
         * For example, the camera experiment uses it to set the framerate and the video start and end point.
         * </p>
         * <p>
         * Important: the analysisSpecificData and the options bundles have to be put as extras into the intent:
         * <ul>
         * <li>bundle field "analysisSpecificData" -> analysisSpecificData</li>
         * <li>bundle field "options" -> options</li>
         * </ul>
         * </p>
         * <p>
         * The following options can be put into the option bundle:
         * <ul>
         * <li>boolean field "start_with_help", to start with help screen</li>
         * </ul>
         * </p>
         * <p>
         * The Activity should return an Intent containing the following fields:
         * <ul>
         * <li>bundle field "run_settings", the updated run settings</li>
         * <li>boolean field "run_settings_changed", if the run settings have been changed</li>
         * </ul>
         * </p>
         *
         * @param parentActivity the parent activity
         * @param requestCode request code for the activity
         * @param sensorDataRef the sensor that should be configured
         * @param analysisSpecificData bundle with the analysis specific data (the analysis settings)
         * @param options bundle with options for the run settings activity
         */
        public void startSensorSettingsActivity(Activity parentActivity, int requestCode,
                                                ExperimentData.SensorDataRef sensorDataRef,
                                                Bundle analysisSpecificData, Bundle options);

        /**
         * Check if a run settings activity exist.
         *
         * @param menuName optional, if not null the name of the run settings activity is put here
         * @return true if there is a run settings activity
         */
        public boolean hasSensorSettingsActivity(StringBuilder menuName);
    }

    public interface IAnalysis {
        /**
         * Load an old experiment from a data bundle.
         * <p>
         * Note that there is no method to create a new experiment from the plugin. When creating a new experiment you
         * usually know what you are doing so just call the normal constructor, e.g. new CameraExperiment(...).
         * </p>
         *
         * @param context of the parent
         * @param data saved state of the experiment
         * @param storageDir directory where additional data may have been stored
         * @return the loaded experiment or null on failure
         */
        public SensorData loadSensorData(Context context, Bundle data, File storageDir);

        /**
         * Creates an {@link SensorAnalysis} object for the given
         * {@link SensorData}.
         *
         * @param sensorData usually loaded with the loadSensorData method
         * @return pointer to the created experiment analysis
         */
        public SensorAnalysis createSensorAnalysis(SensorData sensorData);

        /**
         * Creates the view that displays the results in the
         * {@link nz.ac.auckland.lablet.ExperimentAnalyserActivity}.
         *
         * @param context context of the view
         * @param sensorAnalysis sensor analysis that should be used
         * @return a newly created view
         */
        public View createSensorAnalysisView(Context context, SensorAnalysis sensorAnalysis);

    }
}
