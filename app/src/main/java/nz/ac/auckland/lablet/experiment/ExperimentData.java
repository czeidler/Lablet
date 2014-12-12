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
import nz.ac.auckland.lablet.accelerometer.AccelerometerExperimentData;
import nz.ac.auckland.lablet.camera.CameraExperimentData;
import nz.ac.auckland.lablet.microphone.MicrophoneExperimentData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExperimentData {
    public static class RunEntry {
        public ExperimentRunData runData;
        public List<IExperimentData> sensorDataList = new ArrayList<>();
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

    private IExperimentData loadSensorData(Context context, File sensorDirectory) {
        Bundle bundle;

        File file = new File(sensorDirectory, IExperimentData.EXPERIMENT_DATA_FILE_NAME);
        bundle = ExperimentHelper.loadBundleFromFile(file);

        if (bundle == null) {
            loadError = "can't read experiment file";
            return null;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            loadError = "failed to load experiment data";
            return null;
        }

        String experimentIdentifier = bundle.getString("sensor_name", "");

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        ISensorPlugin plugin = factory.findSensorPlugin(experimentIdentifier);
        if (plugin == null) {
            // fallback: try to find analysis for the data type
            if (!experimentData.containsKey(AbstractExperimentData.DATA_TYPE_KEY)) {
                loadError = "experiment data type information is missing";
                return null;
            }
            String dataType = experimentData.getString(AbstractExperimentData.DATA_TYPE_KEY);
            IExperimentData sensorData = getSensorDataForType(dataType, context, experimentData, sensorDirectory);
            if (sensorData == null)
                loadError = "unknown experiment type";
            return sensorData;
        }


        IExperimentData sensorData = plugin.loadSensorData(context, experimentData, sensorDirectory);
        if (sensorData == null) {
            loadError = "can't load experiment";
            return null;
        }

        return sensorData;
    }

    private IExperimentData getSensorDataForType(String dataType, Context context, Bundle data, File dir) {
        IExperimentData sensorData = null;
        switch (dataType) {
            case MicrophoneExperimentData.DATA_TYPE:
                sensorData = new MicrophoneExperimentData(context);
                break;
            case CameraExperimentData.DATA_TYPE:
                sensorData = new CameraExperimentData(context);
                break;
            case AccelerometerExperimentData.DATA_TYPE:
                sensorData = new AccelerometerExperimentData(context);
                break;
        }
        if (sensorData != null) {
            try {
                sensorData.loadExperimentData(data, dir);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
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
            IExperimentData sensorData = loadSensorData(context, runDirectory);
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
