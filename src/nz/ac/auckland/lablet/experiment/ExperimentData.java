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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExperimentData {
    public static class SensorDataRef {
        final ExperimentData experimentData;
        final public int run;
        final public int sensor;

        public SensorDataRef(ExperimentData experimentData, int run, int sensor) {
            this.experimentData = experimentData;
            this.run = run;
            this.sensor = sensor;
        }
    }
    public static class SensorEntry {
        public IExperimentPlugin plugin;
        public SensorData sensorData;
    }

    public static class RunEntry {
        public ExperimentRunData runData;
        public List<SensorEntry> sensors = new ArrayList<>();
    }

    private File storageDir;
    private String loadError = "";
    private List<RunEntry> runs = new ArrayList<>();

    public File getStorageDir() {
        return storageDir;
    }

    public List<RunEntry> getRuns() {
        return runs;
    }

    public String getLoadError() {
        return loadError;
    }

    private ExperimentData.SensorEntry loadSensorData(Context context, File sensorDirectory) {
        ExperimentData.SensorEntry sensorEntry = new ExperimentData.SensorEntry();

        Bundle bundle = null;

        File file = new File(sensorDirectory, SensorData.EXPERIMENT_DATA_FILE_NAME);
        bundle = ExperimentLoader.loadBundleFromFile(file);

        if (bundle == null) {
            loadError = "can't read experiment file";
            return null;
        }

        String experimentIdentifier = bundle.getString("experiment_identifier");
        if (experimentIdentifier == null) {
            loadError = "invalid experiment data";
            return null;
        }

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        sensorEntry.plugin = factory.findExperimentPlugin(experimentIdentifier);
        if (sensorEntry.plugin == null) {
            loadError = "unknown experiment type";
            return null;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            loadError = "failed to load experiment data";
            return null;
        }
        sensorEntry.sensorData = sensorEntry.plugin.loadSensorData(context, experimentData, sensorDirectory);
        if (sensorEntry.sensorData == null) {
            loadError = "can't load experiment";
            return null;
        }

        return sensorEntry;
    }

    private ExperimentData.RunEntry loadRunData(Context context, File runDir) {
        ExperimentRunData groupData = new ExperimentRunData();
        File groupFile = new File(runDir, ExperimentRun.EXPERIMENT_RUN_GROUP_FILE_NAME);
        try {
            groupData.loadFromFile(groupFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ExperimentData.RunEntry runEntry = new ExperimentData.RunEntry();
        runEntry.runData = groupData;

        for (File runDirectory : runDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ExperimentData.SensorEntry sensorEntry = loadSensorData(context, runDirectory);
            if (sensorEntry == null)
                return null;
            runEntry.sensors.add(sensorEntry);
        }
        return runEntry;
    }

    public boolean load(Context context, String experimentPath) {
        storageDir = new File(experimentPath);

        for (File groupDir : storageDir.listFiles()) {
            if (!groupDir.isDirectory())
                continue;

            ExperimentData.RunEntry runEntry = loadRunData(context, groupDir);
            if (runEntry == null)
                return false;
            runs.add(runEntry);
        }

        return true;
    }
}
