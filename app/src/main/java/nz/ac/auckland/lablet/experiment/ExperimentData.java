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
import nz.ac.auckland.lablet.accelerometer.AccelerometerSensorData;
import nz.ac.auckland.lablet.camera.CameraSensorData;
import nz.ac.auckland.lablet.microphone.MicrophoneSensorData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExperimentData {
    public static class Run {
        public ExperimentRunInfo runData;
        public List<ISensorData> sensorDataList = new ArrayList<>();
    }

    private File storageDir;
    private String loadError = "";
    private List<Run> runs = new ArrayList<>();

    public File getStorageDir() {
        return storageDir;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public String getLoadError() {
        return loadError;
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
            ISensorData sensorData = getSensorDataForType(dataType, context, dataBundle, sensorDirectory);
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

    private ISensorData getSensorDataForType(String dataType, Context context, Bundle data, File dir) {
        ISensorData sensorData = null;
        switch (dataType) {
            case MicrophoneSensorData.DATA_TYPE:
                sensorData = new MicrophoneSensorData(context);
                break;
            case CameraSensorData.DATA_TYPE:
                sensorData = new CameraSensorData(context);
                break;
            case AccelerometerSensorData.DATA_TYPE:
                sensorData = new AccelerometerSensorData(context);
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

    private Run loadRun(Context context, File runDir) {
        ExperimentRunInfo runInfo = new ExperimentRunInfo();
        File runDataFile = new File(runDir, ExperimentRun.EXPERIMENT_RUN_FILE_NAME);
        try {
            runInfo.loadFromFile(runDataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Run run = new Run();
        run.runData = runInfo;

        for (File runDirectory : runDir.listFiles()) {
            if (!runDirectory.isDirectory())
                continue;
            ISensorData sensorData = loadExperimentData(context, runDirectory);
            if (sensorData == null)
                return null;
            run.sensorDataList.add(sensorData);
        }
        return run;
    }

    public boolean load(Context context, File storageDir) {
        if (storageDir == null || !storageDir.exists())
            return false;

        this.storageDir = storageDir;

        for (File groupDir : storageDir.listFiles()) {
            if (!groupDir.isDirectory())
                continue;

            Run run = loadRun(context, groupDir);
            if (run == null)
                return false;
            runs.add(run);
        }

        return true;
    }
}
