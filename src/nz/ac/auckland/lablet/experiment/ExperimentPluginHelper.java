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
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;

import java.util.List;


/**
 * Abstract base class for experiment plugins.
 */
public class ExperimentPluginHelper {

    static public void packStartExperimentIntent(Intent intent, List<ISensorPlugin> plugins, Bundle options) {
        String[] pluginNames = null;
        if (plugins.size() > 0) {
            pluginNames = new String[plugins.size()];
            for (int i = 0; i < plugins.size(); i++) {
                ISensorPlugin plugin = plugins.get(i);
                pluginNames[i] = plugin.getIdentifier();
            }
        }

        packStartExperimentIntent(intent, pluginNames, options);
    }

    /**
     * Helper method to pack the option bundle correctly.
     * @param intent the Intent where the data should be packed to
     * @param plugins
     * @param options the options for the activity
     */
    static public void packStartExperimentIntent(Intent intent, String[] plugins, Bundle options) {
        if (plugins != null)
            intent.putExtra("plugins", plugins);

        if (options != null)
            intent.putExtra("options", options);
    }

    static public String[] unpackStartExperimentPlugins(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return null;
        return extras.getStringArray("plugins");
    }

    static public Bundle unpackStartExperimentOptions(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return null;
        return extras.getBundle("options");
    }

    /**
     * Helper method to pack the analysis specific data and the options bundles correctly.
     *
     * @param intent the Intent where the data should be packed to
     * @param analysisRef the target analysis
     * @param options the options for the activity
     */
    static public void packStartAnalysisSettingsIntent(Intent intent, ExperimentAnalysisActivity.AnalysisRef analysisRef,
                                                       String experimentPath, Bundle options) {

        intent.putExtra("run_id", analysisRef.run);
        intent.putExtra("sensor_id", analysisRef.sensor);
        intent.putExtra("analysis_id", analysisRef.analysisId);

        intent.putExtra("experiment_path", experimentPath);
        if (options != null)
            intent.putExtra("options", options);
    }
}
