/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.content.Context;
import android.os.Bundle;
import nz.ac.auckland.lablet.misc.PersistentBundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;

/**
 * Helper class to load an {@link SensorData} or an
 * {@link SensorAnalysis}.
 */
public class ExperimentLoader {
    /**
     * The results of an experiment load attempt.
     */
    static public class Result {
        public ExperimentData experimentData;
        public String loadError;
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

    private static ExperimentData.SensorEntry loadExperimentRun(Context context, File runDirectory, Result result) {
        ExperimentData.SensorEntry sensorEntry = new ExperimentData.SensorEntry();

        Bundle bundle = null;

        File file = new File(runDirectory, SensorData.EXPERIMENT_DATA_FILE_NAME);
        bundle = ExperimentLoader.loadBundleFromFile(file);

        if (bundle == null) {
            result.loadError = "can't read experiment file";
            return null;
        }

        String experimentIdentifier = bundle.getString("experiment_identifier");
        if (experimentIdentifier == null) {
            result.loadError = "invalid experiment data";
            return null;
        }

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        sensorEntry.plugin = factory.findExperimentPlugin(experimentIdentifier);
        if (sensorEntry.plugin == null) {
            result.loadError = "unknown experiment type";
            return null;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            result.loadError = "failed to load experiment data";
            return null;
        }
        sensorEntry.sensorData = sensorEntry.plugin.loadSensorData(context, experimentData, runDirectory);
        if (sensorEntry.sensorData == null) {
            result.loadError = "can't load experiment";
            return null;
        }

        return sensorEntry;
    }

    private static ExperimentData.RunEntry loadRunGroup(Context context, File groupDir, Result result) {
        ExperimentRunData groupData = new ExperimentRunData();
        File groupFile = new File(groupDir, ExperimentRunGroup.EXPERIMENT_RUN_GROUP_FILE_NAME);
        try {
            groupData.loadFromFile(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ExperimentData.RunEntry runEntry = new ExperimentData.RunEntry();
        runEntry.runData = groupData;

        for (File runDirectory : groupDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ExperimentData.SensorEntry sensorEntry = loadExperimentRun(context, runDirectory, result);
            if (sensorEntry == null)
                return null;
            runEntry.runs.add(sensorEntry);
        }
        return runEntry;
    }

    public static boolean loadExperiment(Context context, String experimentPath, Result result) {
        File storageDir = new File(experimentPath);

        result.experimentData = new ExperimentData();

        for (File groupDir : storageDir.listFiles()) {
            if (!groupDir.isDirectory())
                continue;

            ExperimentData.RunEntry runEntry = loadRunGroup(context, groupDir, result);
            if (runEntry == null)
                return false;
            result.experimentData.runs.add(runEntry);
        }

        return true;
    }

    // Creates a new ExperimentAnalysis and tries to load an existing analysis.
    public static SensorAnalysis getExperimentAnalysis(ExperimentData.SensorEntry sensorEntry) {
        SensorData runData = sensorEntry.sensorData;
        SensorAnalysis sensorAnalysis = sensorEntry.plugin.createSensorAnalysis(runData);

        // try to load old analysis
        File projectFile = new File(runData.getStorageDir(), SensorAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = ExperimentLoader.loadBundleFromFile(projectFile);
        if (bundle == null)
            return sensorAnalysis;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return sensorAnalysis;

        sensorAnalysis.loadAnalysisData(analysisDataBundle, runData.getStorageDir());

        return sensorAnalysis;
    }
}

