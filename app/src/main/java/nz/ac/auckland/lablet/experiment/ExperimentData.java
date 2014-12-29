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


/**
 * Main class for experiment data.
 *
 * Holds the run data.
 */
public class ExperimentData {
    /**
     * Contains the run information and the run sensor data.
     */
    public static class RunData {
        public ExperimentRunInfo experimentRunInfo;
        public List<ISensorData> sensorDataList = new ArrayList<>();
    }

    private File storageDir;
    private String loadError = "";
    private List<RunData> runDataList = new ArrayList<>();

    /**
     * Get the data storage directory.
     *
     * @return null if no load hasn't be called or loading the data failed
     */
    public File getStorageDir() {
        return storageDir;
    }

    /**
     * @return the list of run data
     */
    public List<RunData> getRunDataList() {
        return runDataList;
    }

    /**
     * @return the last load error
     */
    public String getLoadError() {
        return loadError;
    }

    /**
     * Load the experiment data from a storage directory.
     *
     * @param context
     * @param storageDir
     * @return false if there was an error (see {@link #getLoadError})
     */
    public boolean load(Context context, File storageDir) {
        if (storageDir == null || !storageDir.exists())
            return false;

        for (File groupDir : storageDir.listFiles()) {
            if (!groupDir.isDirectory())
                continue;

            RunData runData = loadRunData(context, groupDir);
            if (runData == null)
                return false;
            runDataList.add(runData);
        }

        this.storageDir = storageDir;
        return true;
    }

    private ISensorData loadExperimentData(Context context, File sensorDirectory) {
        Bundle bundle;

        File file = new File(sensorDirectory, ISensorData.EXPERIMENT_DATA_FILE_NAME);
        bundle = ExperimentHelper.loadBundleFromFile(file);

        if (bundle == null) {
            loadError = "can't read experiment file";
            return null;
        }

        Bundle dataBundle = bundle.getBundle(AbstractSensorData.DATA_KEY);
        if (dataBundle == null) {
            loadError = "failed to load sensor data";
            return null;
        }

        String sensorName = bundle.getString(AbstractSensorData.SENSOR_NAME_KEY, "");

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        ISensorPlugin plugin = factory.findSensorPlugin(sensorName);
        if (plugin == null) {
            // fallback: try to find analysis for the data type
            if (!dataBundle.containsKey(AbstractSensorData.DATA_TYPE_KEY)) {
                loadError = "data type information is missing";
                return null;
            }
            String dataType = dataBundle.getString(AbstractSensorData.DATA_TYPE_KEY);
            ISensorData sensorData = getSensorDataForType(dataType, dataBundle, sensorDirectory);
            if (sensorData == null)
                loadError = "unknown data type";
            return sensorData;
        }


        ISensorData sensorData = plugin.loadSensorData(context, dataBundle, sensorDirectory);
        if (sensorData == null) {
            loadError = "can't load sensor data";
            return null;
        }

        return sensorData;
    }

    private ISensorData getSensorDataForType(String dataType, Bundle data, File dir) {
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        ISensorData sensorData = factory.instantiateSensorData(dataType);
        if (sensorData == null)
            return null;

        try {
            sensorData.loadExperimentData(data, dir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sensorData;
    }

    private RunData loadRunData(Context context, File runDataDir) {
        ExperimentRunInfo runInfo = new ExperimentRunInfo();
        File runDataFile = new File(runDataDir, ExperimentRun.EXPERIMENT_RUN_FILE_NAME);
        try {
            runInfo.loadFromFile(runDataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        RunData runData = new RunData();
        runData.experimentRunInfo = runInfo;

        for (File runDirectory : runDataDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ISensorData sensorData = loadExperimentData(context, runDirectory);
            if (sensorData == null)
                return null;
            runData.sensorDataList.add(sensorData);
        }
        return runData;
    }
}
