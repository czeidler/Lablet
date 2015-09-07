/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;
import android.os.Bundle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * An ExperimentRun represent a single measurement of an experiment.
 *
 * A measurement can be taken with multiple sensors. When recording a run, all sensors are recorded at the same time.
 * However, one can set a current sensor, for example, to indicate what sensor preview should be displayed.
 *
 */
public class ExperimentRun {
    private ExperimentRunInfo data = new ExperimentRunInfo();

    final private List<IExperimentSensor> experimentSensors = new ArrayList<>();
    private IExperimentSensor currentSensor = null;
    private Experiment experiment;
    private Activity experimentActivity;

    final static public String EXPERIMENT_RUN_FILE_NAME = "experiment_run.xml";

    final static private String SENSOR_KEY = "sensor";
    final static private String SENSOR_COUNT_KEY = "sensor_count";
    final static private String CURRENT_SENSOR_KEY = "current_sensor";

    static public ExperimentRun createExperimentRun(List<String> sensorNames) {
        String[] experimentRunsArray = new String[sensorNames.size()];
        for (int i = 0; i < experimentRunsArray.length; i++)
            experimentRunsArray[i] = sensorNames.get(i);

        return createExperimentRun(experimentRunsArray);
    }

    /**
     * Creates an {@link ExperimentRun} from a list of sensor names.
     *
     * Tries to find sensors for each entry in the sensor name list and adds them to a new ExperimentRun.
     *
     * @param sensorNames list of sensors
     * @return the experiment run
     */
    static public ExperimentRun createExperimentRun(String[] sensorNames) {
        ExperimentRun experimentRunGroup = new ExperimentRun();

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (String pluginName : sensorNames) {
            ISensorPlugin plugin = factory.findSensorPlugin(pluginName);
            if (plugin == null)
                continue;
            IExperimentSensor experimentRun = plugin.createExperimentSensor();
            experimentRunGroup.addExperimentSensor(experimentRun);
        }

        return experimentRunGroup;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public void activateSensors(Activity activity) {
        if (experimentActivity != null) {
            for (IExperimentSensor experimentSensor : experimentSensors)
                experimentSensor.destroy();
        }
        experimentActivity = activity;
        if (experimentActivity != null) {
            for (IExperimentSensor experimentSensor : experimentSensors)
                experimentSensor.init(experimentActivity);
        }
    }

    public boolean isActive() {
        return experimentActivity != null;
    }

    public int getSensorCount() {
        return experimentSensors.size();
    }

    public void setCurrentSensor(int i) {
        currentSensor = getExperimentSensorAt(i);
    }

    public boolean setCurrentSensor(IExperimentSensor sensor) {
        if (!experimentSensors.contains(sensor))
            return false;
        currentSensor = sensor;
        return true;
    }

    public IExperimentSensor getCurrentExperimentSensor() {
        return currentSensor;
    }

    public IExperimentSensor getExperimentSensorAt(int i) {
        return experimentSensors.get(i);
    }

    public List<IExperimentSensor> getExperimentSensors() {
        return experimentSensors;
    }

    public boolean addExperimentSensor(IExperimentSensor experimentSensor) {
        if (experimentSensor.getExperimentRun() != null)
            return false;
        experimentSensors.add(experimentSensor);
        experimentSensor.setExperimentRun(this);

        if (experimentActivity != null)
            experimentSensor.init(experimentActivity);
        return true;
    }

    public void removeExperimentSensor(IExperimentSensor experimentSensor) {
        // update the current sensor first if necessary
        if (experimentSensor == getCurrentExperimentSensor()) {
            int index = experimentSensors.indexOf(experimentSensor);
            if (index > 0)
                setCurrentSensor(0);
            else if (index + 1 < experimentSensors.size())
                setCurrentSensor(index + 1);
            else
                setCurrentSensor(null);
        }
        experimentSensors.remove(experimentSensor);

        if (experimentActivity != null)
            experimentSensor.destroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        data.onSaveInstanceState(outState);

        int i = 0;
        for (IExperimentSensor experimentSensor : experimentSensors) {
            Bundle bundle = new Bundle();
            experimentSensor.onSaveInstanceState(bundle);
            outState.putBundle(Integer.toString(i), bundle);
            i++;
        }

        if (currentSensor != null) {
            int index = getExperimentSensors().indexOf(currentSensor);
            outState.putInt(CURRENT_SENSOR_KEY, index);
        }

        // store plugin information
        Bundle experimentSensorClasses = new Bundle();
        i = 0;
        for (IExperimentSensor experimentSensor : experimentSensors) {
            experimentSensorClasses.putString(Integer.toString(i), experimentSensor.getSensorName());
            i++;
        }
        outState.putBundle(SENSOR_KEY, experimentSensorClasses);
        outState.putInt(SENSOR_COUNT_KEY, experimentSensors.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        data.onRestoreInstanceState(savedInstanceState);

        int sensorClassesCount = savedInstanceState.getInt(SENSOR_COUNT_KEY);
        Bundle sensorsBundle = savedInstanceState.getBundle(SENSOR_KEY);
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (int i = 0; i < sensorClassesCount; i++) {
            String pluginName = sensorsBundle.getString(Integer.toString(i));
            ISensorPlugin plugin = factory.findSensorPlugin(pluginName);
            IExperimentSensor experimentSensor = plugin.createExperimentSensor();
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentSensor.onRestoreInstanceState(state);
            addExperimentSensor(experimentSensor);
        }

        int index = savedInstanceState.getInt(CURRENT_SENSOR_KEY, -1);
        if (index >= 0)
            setCurrentSensor(index);
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public boolean dataTaken() {
        for (IExperimentSensor experimentSensor : experimentSensors) {
            if (experimentSensor.dataTaken())
                return true;
        }
        return false;
    }

    /**
     * Finishes the experiment run and saves the sensor data if requested.
     *
     * @param saveData indicates if the data should be saved
     * @param storageDir the storage location
     * @throws IOException
     */
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        if (!dataTaken())
            return;

        if (saveData) {
            storageDir.mkdirs();
            data.saveToFile(new File(storageDir, EXPERIMENT_RUN_FILE_NAME));
        }

        for (IExperimentSensor experimentSensor : experimentSensors)
            experimentSensor.finishExperiment(saveData, storageDir);
    }
}
