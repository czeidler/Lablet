/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;


public interface IAnalysisPlugin {
    /**
     * The name of the plugin. For example, the name of the experiment class.
     *
     * @return the name of the plugin
     */
    public String getIdentifier();

    public String supportedDataType();

    /**
     * Creates an {@link nz.ac.auckland.lablet.experiment.ISensorAnalysis} object for the given
     * {@link nz.ac.auckland.lablet.experiment.SensorData}.
     *
     * @param sensorData usually loaded with the loadSensorData method
     * @return pointer to the created experiment analysis
     */
    public ISensorAnalysis createSensorAnalysis(SensorData sensorData);

    /**
     * Creates the view that displays the results in the
     * {@link nz.ac.auckland.lablet.ExperimentAnalysisActivity}.
     *
     * @param analysisRef analysis ref
     * @return a newly created view
     */
    public Fragment createSensorAnalysisFragment(ExperimentAnalysisActivity.AnalysisRef analysisRef);

    /**
     * Starts an activity to config the experiment analysis.
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
     * @param analysisRef the analysis that should be configured
     * @param experimentPath path to the experiment data
     * @param options bundle with options for the run settings activity
     */
    public void startAnalysisSettingsActivity(Activity parentActivity, int requestCode,
                                              ExperimentAnalysisActivity.AnalysisRef analysisRef,
                                              String experimentPath, Bundle options);

    /**
     * Check if a run settings activity exist.
     *
     * @param menuName optional, if not null the name of the run settings activity is put here
     * @return true if there is a run settings activity
     */
    public boolean hasAnalysisSettingsActivity(StringBuilder menuName);
}
