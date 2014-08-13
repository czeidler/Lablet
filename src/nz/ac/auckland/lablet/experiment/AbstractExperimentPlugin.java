/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;


import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for experiment plugins.
 */
abstract public class AbstractExperimentPlugin implements IExperimentPlugin {
    /**
     * Helper method to pack the option bundle correctly.
     * @param intent the Intent where the data should be packed to
     * @param plugins
     * @param options the options for the activity
     */
    static public void packStartExperimentIntent(Intent intent, List<IExperimentPlugin> plugins, Bundle options) {
        if (plugins.size() > 0) {
            String[] pluginNames = new String[plugins.size()];
            for (int i = 0; i < plugins.size(); i++) {
                IExperimentPlugin plugin = plugins.get(i);
                pluginNames[i] = plugin.getName();
            }
            intent.putExtra("plugins", pluginNames);
        }

        if (options != null)
            intent.putExtra("options", options);
    }

    /**
     * Helper method to pack the analysis specific data and the options bundles correctly.
     *
     * @param intent the Intent where the data should be packed to
     * @param sensorDataRef the target sensor
     * @param analysisSpecificData analysis specific data bundle
     * @param options the options for the activity
     */
    static public void packStartRunSettingsIntent(Intent intent, ExperimentData.SensorDataRef sensorDataRef,
                                                  Bundle analysisSpecificData, Bundle options) {

        intent.putExtra("run_id", sensorDataRef.run);
        intent.putExtra("sensor_id", sensorDataRef.sensor);

        intent.putExtra("experiment_path", sensorDataRef.experimentData.getStorageDir().getPath());
        if (analysisSpecificData != null)
            intent.putExtra("analysisSpecificData", analysisSpecificData);
        if (options != null)
            intent.putExtra("options", options);
    }
}
