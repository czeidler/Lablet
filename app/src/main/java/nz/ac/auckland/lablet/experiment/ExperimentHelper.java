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
import nz.ac.auckland.lablet.misc.PersistentBundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.List;


/**
 * Abstract base class for experiment plugins.
 */
public class ExperimentHelper {
    static public ExperimentData loadExperimentData(String experimentMainDir) {
        ExperimentData experimentData = new ExperimentData();
        if (!experimentData.load(new File(experimentMainDir, "data")))
            return experimentData;
        return experimentData;
    }

    static public Bundle loadBundleFromFile(File file) {
        Bundle bundle;
        InputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        PersistentBundle persistentBundle = new PersistentBundle();
        try {
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }

        return bundle;
    }

    // Creates a new ExperimentAnalysis and tries to load an existing analysis.
    static public boolean loadSensorAnalysis(IDataAnalysis sensorAnalysis, File storageDir) {
        // try to load old analysis
        File projectFile = new File(storageDir, IDataAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = loadBundleFromFile(projectFile);
        if (bundle == null)
            return false;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return false;

        return sensorAnalysis.loadAnalysisData(analysisDataBundle, storageDir);
    }

    static public void saveAnalysisData(IDataAnalysis sensorAnalysis, File storageDir) throws IOException {
        Bundle experimentData = sensorAnalysis.exportAnalysisData(storageDir);
        Bundle bundle = new Bundle();
        bundle.putBundle("analysis_data", experimentData);

        // save the bundle
        File projectFile = new File(storageDir, IDataAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    static public void packStartExperimentIntent(Intent intent, List<ISensorPlugin> plugins, Bundle options) {
        String[] pluginNames = null;
        if (plugins.size() > 0) {
            pluginNames = new String[plugins.size()];
            for (int i = 0; i < plugins.size(); i++) {
                ISensorPlugin plugin = plugins.get(i);
                pluginNames[i] = plugin.getSensorName();
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
    static public void packStartAnalysisSettingsIntent(Intent intent, ExperimentAnalysis.AnalysisRef analysisRef,
                                                       String experimentPath, Bundle options) {

        intent.putExtra("run_id", analysisRef.runId);
        intent.putExtra("sensor_id", analysisRef.dataId);
        intent.putExtra("analysis_id", analysisRef.analysisId);

        intent.putExtra("experiment_path", experimentPath);
        if (options != null)
            intent.putExtra("options", options);
    }
}
