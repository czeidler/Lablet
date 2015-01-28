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

    final static private String PLUGIN_ID_KEY = "plugin_id";
    final static private String ANALYSIS_UID_KEY = "analysis_uid";
    final static private String USED_DATA_KEY = "used_data";
    final static private String SENSOR_DATA_LIST_KEY = "sensor_data_list";

    // Tries to load an existing analysis.
    static public ExperimentAnalysis.AnalysisEntry loadSensorAnalysis(File storageDir, List<ISensorData> allSensorData) {
        // try to load old analysis
        File projectFile = new File(storageDir, IDataAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = loadBundleFromFile(projectFile);
        if (bundle == null)
            return null;

        Bundle analysisDataBundle = bundle.getBundle(USED_DATA_KEY);
        if (analysisDataBundle == null)
            return null;

        int[] integerList = bundle.getIntArray(SENSOR_DATA_LIST_KEY);
        ISensorData[] dataList = new ISensorData[integerList.length];
        for (int i = 0; i < dataList.length; i++)
            dataList[i] = allSensorData.get(integerList[i]);

        String analysisPluginId = bundle.getString(PLUGIN_ID_KEY);
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        IAnalysisPlugin plugin = factory.findAnalysisPlugin(analysisPluginId);
        IDataAnalysis dataAnalysis = plugin.createDataAnalysis(dataList);

        if (!dataAnalysis.loadAnalysisData(analysisDataBundle, storageDir))
            return null;

        String analysisUid = bundle.getString(ANALYSIS_UID_KEY);

        return new ExperimentAnalysis.AnalysisEntry(dataAnalysis, analysisUid, plugin, storageDir);
    }

    static public void saveAnalysisData(ExperimentAnalysis.AnalysisEntry analysisEntry, IDataAnalysis sensorAnalysis,
                                        File storageDir, List<ISensorData> allSensorData) throws IOException {
        Bundle bundle = new Bundle();
        // save plugin
        bundle.putString(PLUGIN_ID_KEY, analysisEntry.plugin.getIdentifier());
        bundle.putString(ANALYSIS_UID_KEY, analysisEntry.plugin.getIdentifier());

        // save used data
        ISensorData[] dataList = sensorAnalysis.getData();
        int integerList[] = new int[dataList.length];
        for (int i = 0; i < integerList.length; i++)
            integerList[i] = allSensorData.indexOf(dataList[i]);
        bundle.putIntArray(SENSOR_DATA_LIST_KEY, integerList);

        // save experiment data
        Bundle experimentData = sensorAnalysis.exportAnalysisData(storageDir);
        bundle.putBundle(USED_DATA_KEY, experimentData);

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
        intent.putExtra("analysis_id", analysisRef.analysisUid);

        intent.putExtra("experiment_path", experimentPath);
        if (options != null)
            intent.putExtra("options", options);
    }
}
