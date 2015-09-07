/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.support.v4.app.Fragment;


/**
 * Plugin interface for a data analysis.
 */
public interface IAnalysisPlugin {
    /**
     * The name of the plugin. For example, the name of the experiment class.
     *
     * @return the identifier of the plugin
     */
    String getIdentifier();

    /**
     * Returns a list of required data types.
     *
     * For example, a combined video and audio analysis must specify that video and audio data is required for the
     * analysis.
     *
     * @return list of required data types.
     */
    String[] requiredDataTypes();

    /**
     * Creates an {@link IDataAnalysis} object for the given list of
     * {@link ISensorData}.
     *
     * @param sensorData usually loaded with the loadSensorData method
     * @return pointer to the created experiment analysis
     */
    IDataAnalysis createDataAnalysis(ISensorData... sensorData);

    /**
     * Creates a fragment that displays the analysis.
     *
     * @param analysisRef analysis ref
     * @return a new fragment
     */
    Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef);

}

