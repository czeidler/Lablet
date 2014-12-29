/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.support.v4.app.Fragment;


public interface IAnalysisPlugin {
    /**
     * The name of the plugin. For example, the name of the experiment class.
     *
     * @return the name of the plugin
     */
    public String getIdentifier();

    public String supportedDataType();

    /**
     * Creates an {@link IDataAnalysis} object for the given
     * {@link ISensorData}.
     *
     * @param sensorData usually loaded with the loadSensorData method
     * @return pointer to the created experiment analysis
     */
    public IDataAnalysis createDataAnalysis(ISensorData sensorData);

    /**
     * Creates the view that displays the results in the
     * {@link nz.ac.auckland.lablet.ExperimentAnalysisActivity}.
     *
     * @param analysisRef analysis ref
     * @return a newly created view
     */
    public Fragment createSensorAnalysisFragment(ExperimentAnalysis.AnalysisRef analysisRef);

}
