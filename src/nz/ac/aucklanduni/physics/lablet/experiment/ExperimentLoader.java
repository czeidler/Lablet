/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.experiment;

import android.content.Context;
import android.os.Bundle;
import nz.ac.aucklanduni.physics.lablet.misc.PersistentBundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


public class ExperimentLoader {
    public static class Result {
        public ExperimentPlugin plugin;
        public Experiment experiment;
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

    public static boolean loadExperiment(Context context, String experimentPath, Result result) {
        File storageDir = null;
        Bundle bundle = null;

        if (experimentPath != null) {
            storageDir = new File(experimentPath);
            File file = new File(storageDir, Experiment.EXPERIMENT_DATA_FILE_NAME);
            bundle = ExperimentLoader.loadBundleFromFile(file);
        }

        if (bundle == null) {
            result.loadError = "can't read experiment file";
            return false;
        }

        String experimentIdentifier = bundle.getString("experiment_identifier");
        if (experimentIdentifier == null) {
            result.loadError = "invalid experiment data";
            return false;
        }

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        result.plugin = factory.findExperimentPlugin(experimentIdentifier);
        if (result.plugin == null) {
            result.loadError = "unknown experiment type";
            return false;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            result.loadError = "failed to load experiment data";
            return false;
        }
        result.experiment = result.plugin.loadExperiment(context, experimentData, storageDir);
        if (result.experiment == null) {
            result.loadError = "can't load experiment";
            return false;
        }

        return true;
    }

    // Creates a new ExperimentAnalysis and tries to load an existing analysis.
    public static ExperimentAnalysis getExperimentAnalysis(Experiment experiment, ExperimentPlugin plugin) {
        ExperimentAnalysis experimentAnalysis = plugin.loadExperimentAnalysis(experiment);

        // try to load old analysis
        File projectFile = new File(experiment.getStorageDir(), ExperimentAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = ExperimentLoader.loadBundleFromFile(projectFile);
        if (bundle == null)
            return experimentAnalysis;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return experimentAnalysis;

        experimentAnalysis.loadAnalysisData(analysisDataBundle, experiment.getStorageDir());

        return experimentAnalysis;
    }

    public static ExperimentAnalysis loadExperimentAnalysis(Context context, String experimentPath) {
        Result result = new ExperimentLoader.Result();
        if (!loadExperiment(context, experimentPath, result))
            return null;

        return getExperimentAnalysis(result.experiment, result.plugin);
    }
}

