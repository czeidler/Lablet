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
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to load an {@link ExperimentRunData} or an
 * {@link nz.ac.auckland.lablet.experiment.ExperimentAnalysis}.
 */
public class ExperimentLoader {
    /**
     * The results of an experiment load attempt.
     */
    static public class Result {
        public ExperimentData experimentData;
        public String loadError;
    }

    static public class ExperimentData {
        public static class RunEntry {
            public IExperimentPlugin plugin;
            public ExperimentRunData experimentRunData;
        }

        public static class GroupEntry {
            public ExperimentRunGroupData groupData;
            public List<RunEntry> runs = new ArrayList<>();
        }

        public List<GroupEntry> groups = new ArrayList<>();
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

    private static ExperimentData.RunEntry loadExperimentRun(Context context, File runDirectory, Result result) {
        ExperimentData.RunEntry runEntry = new ExperimentData.RunEntry();

        Bundle bundle = null;

        File file = new File(runDirectory, ExperimentRunData.EXPERIMENT_DATA_FILE_NAME);
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
        runEntry.plugin = factory.findExperimentPlugin(experimentIdentifier);
        if (runEntry.plugin == null) {
            result.loadError = "unknown experiment type";
            return null;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            result.loadError = "failed to load experiment data";
            return null;
        }
        runEntry.experimentRunData = runEntry.plugin.loadExperimentData(context, experimentData, runDirectory);
        if (runEntry.experimentRunData == null) {
            result.loadError = "can't load experiment";
            return null;
        }

        return runEntry;
    }

    private static ExperimentData.GroupEntry loadRunGroup(Context context, File groupDir, Result result) {
        ExperimentRunGroupData groupData = new ExperimentRunGroupData();
        File groupFile = new File(groupDir, ExperimentRunGroup.EXPERIMENT_RUN_GROUP_FILE_NAME);
        try {
            groupData.loadFromFile(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ExperimentData.GroupEntry groupEntry = new ExperimentData.GroupEntry();
        groupEntry.groupData = groupData;

        for (File runDirectory : groupDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ExperimentData.RunEntry runEntry = loadExperimentRun(context, runDirectory, result);
            if (runEntry == null)
                return null;
            groupEntry.runs.add(runEntry);
        }
        return groupEntry;
    }

    public static boolean loadExperiment(Context context, String experimentPath, Result result) {
        File storageDir = new File(experimentPath);

        result.experimentData = new ExperimentData();

        for (File groupDir : storageDir.listFiles()) {
            if (!groupDir.isDirectory())
                continue;

            ExperimentData.GroupEntry groupEntry = loadRunGroup(context, groupDir, result);
            if (groupEntry == null)
                return false;
            result.experimentData.groups.add(groupEntry);
        }

        return true;
    }

    // Creates a new ExperimentAnalysis and tries to load an existing analysis.
    public static ExperimentAnalysis getExperimentAnalysis(ExperimentData.RunEntry runEntry) {
        ExperimentRunData runData = runEntry.experimentRunData;
        ExperimentAnalysis experimentAnalysis = runEntry.plugin.createExperimentAnalysis(runData);

        // try to load old analysis
        File projectFile = new File(runData.getStorageDir(), ExperimentAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = ExperimentLoader.loadBundleFromFile(projectFile);
        if (bundle == null)
            return experimentAnalysis;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return experimentAnalysis;

        experimentAnalysis.loadAnalysisData(analysisDataBundle, runData.getStorageDir());

        return experimentAnalysis;
    }
}

