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
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


class ExperimentRunInfo {
    private String description = "";
    private Bundle runInformation = new Bundle();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Bundle getRunInformation() {
        return runInformation;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putString("description", getDescription());
        outState.putBundle("runInformation", getRunInformation());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        description = savedInstanceState.getString("description", "");
        Bundle bundle = savedInstanceState.getBundle("runInformation");
        if (bundle != null)
            runInformation = bundle;
    }

    public void saveToFile(File file) throws IOException {
        Bundle bundle = new Bundle();
        onSaveInstanceState(bundle);

        FileWriter fileWriter = new FileWriter(file);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
        fileWriter.close();
    }

    public void loadFromFile(File file) throws IOException {
        Bundle bundle = null;
        PersistentBundle persistentBundle = new PersistentBundle();
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (inStream!= null)
                inStream.close();
        }
        onRestoreInstanceState(bundle);
    }
}

public class ExperimentRun {
    private ExperimentRunInfo data = new ExperimentRunInfo();

    final private List<IExperimentSensor> experimentSensors = new ArrayList<>();
    private IExperimentSensor currentSensor = null;
    private Experiment experiment;
    private String subStorageDirectory;
    private Activity experimentActivity;

    final static public String EXPERIMENT_RUN_FILE_NAME = "experiment_run.xml";

    static public ExperimentRun createExperimentRunGroup(List<String> experimentRuns) {
        String[] experimentRunsArray = new String[experimentRuns.size()];
        for (int i = 0; i < experimentRunsArray.length; i++)
            experimentRunsArray[i] = experimentRuns.get(i);

        return createExperimentRunGroup(experimentRunsArray);
    }

    static public ExperimentRun createExperimentRunGroup(String[] plugins) {
        ExperimentRun experimentRunGroup = new ExperimentRun();

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (String pluginName : plugins) {
            ISensorPlugin plugin = factory.findSensorPlugin(pluginName);
            if (plugin == null)
                continue;
            IExperimentSensor experimentRun = plugin.createExperimentSensor();
            experimentRunGroup.addExperimentSensor(experimentRun);
        }

        return experimentRunGroup;
    }

    public void setExperiment(Experiment experiment, String subStorageDirectory) {
        this.experiment = experiment;
        this.subStorageDirectory = subStorageDirectory;
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
            outState.putInt("current_sensor", index);
        }

        // store plugin information
        Bundle experimentSensorClasses = new Bundle();
        i = 0;
        for (IExperimentSensor experimentSensor : experimentSensors) {
            experimentSensorClasses.putString(Integer.toString(i), experimentSensor.getSensorName());
            i++;
        }
        outState.putBundle("sensors", experimentSensorClasses);
        outState.putInt("sensor_count", experimentSensors.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        data.onRestoreInstanceState(savedInstanceState);

        int sensorClassesCount = savedInstanceState.getInt("sensor_count");
        Bundle sensorsBundle = savedInstanceState.getBundle("sensors");
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (int i = 0; i < sensorClassesCount; i++) {
            String pluginName = sensorsBundle.getString(Integer.toString(i));
            ISensorPlugin plugin = factory.findSensorPlugin(pluginName);
            IExperimentSensor experimentSensor = plugin.createExperimentSensor();
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentSensor.onRestoreInstanceState(state);
            addExperimentSensor(experimentSensor);
        }

        int index = savedInstanceState.getInt("current_sensor", -1);
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

    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        if (saveData) {
            storageDir.mkdirs();
            data.saveToFile(new File(storageDir, EXPERIMENT_RUN_FILE_NAME));
        }

        for (IExperimentSensor experimentSensor : experimentSensors) {
            experimentSensor.finishExperiment(saveData,
                    new File(storageDir, experimentSensor.getClass().getSimpleName()));
        }
    }
}
