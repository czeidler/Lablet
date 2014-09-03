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
    public static class RunEntry {
        public ExperimentRunData runData;
        public List<ISensorData> sensorDataList = new ArrayList<>();
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

    private ISensorData loadSensorData(Context context, File sensorDirectory) {
        Bundle bundle;

        File file = new File(sensorDirectory, ISensorData.EXPERIMENT_DATA_FILE_NAME);
        bundle = ExperimentHelper.loadBundleFromFile(file);

        if (bundle == null) {
            loadError = "can't read experiment file";
            return null;
        }

        String experimentIdentifier = bundle.getString("sensor_name");
        if (experimentIdentifier == null) {
            loadError = "invalid experiment data";
            return null;
        }

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        ISensorPlugin plugin = factory.findSensorPlugin(experimentIdentifier);
        if (plugin == null) {
            loadError = "unknown experiment type";
            return null;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            loadError = "failed to load experiment data";
            return null;
        }
        ISensorData sensorData = plugin.loadSensorData(context, experimentData, sensorDirectory);
        if (sensorData == null) {
            loadError = "can't load experiment";
            return null;
        }

        return sensorData;
    }

    private ExperimentData.RunEntry loadRunData(Context context, File runDir) {
        ExperimentRunData runData = new ExperimentRunData();
        File runDataFile = new File(runDir, ExperimentRun.EXPERIMENT_RUN_FILE_NAME);
        try {
            runData.loadFromFile(runDataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        ExperimentData.RunEntry runEntry = new ExperimentData.RunEntry();
        runEntry.runData = runData;

        for (File runDirectory : runDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ISensorData sensorData = loadSensorData(context, runDirectory);
            if (sensorData == null)
                return null;
            runEntry.sensorDataList.add(sensorData);
        }
        return runEntry;
    }

    public boolean load(Context context, File storageDir) {
        if (storageDir == null || !storageDir.exists())
            return false;

        this.storageDir = storageDir;

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
