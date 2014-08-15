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

class ExperimentRunData {
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
            inStream.close();
        }
        onRestoreInstanceState(bundle);
    }
}

public class ExperimentRun {
    private ExperimentRunData data = new ExperimentRunData();

    final private List<IExperimentSensor> experimentSensors = new ArrayList<>();
    private IExperimentSensor currentExperimentRun = null;
    private Experiment experiment;
    private String subStorageDirectory;
    private Activity experimentActivity;

    final static public String EXPERIMENT_RUN_GROUP_FILE_NAME = "experiment_run_group.xml";

    static public ExperimentRun createExperimentRunGroup(List<String> experimentRuns, Activity activity) {
        String[] experimentRunsArray = new String[experimentRuns.size()];
        for (int i = 0; i < experimentRunsArray.length; i++)
            experimentRunsArray[i] = experimentRuns.get(i);

        return createExperimentRunGroup(experimentRunsArray, activity);
    }

    static public ExperimentRun createExperimentRunGroup(String[] plugins, Activity activity) {
        ExperimentRun experimentRunGroup = new ExperimentRun();

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (String pluginName : plugins) {
            IExperimentPlugin plugin = factory.findExperimentPlugin(pluginName);
            if (plugin == null)
                continue;
            IExperimentSensor experimentRun = plugin.createExperimentSensor(activity);
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
            for (IExperimentSensor experimentRun : experimentSensors)
                experimentRun.destroy();
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

    public int getExperimentRunCount() {
        return experimentSensors.size();
    }

    public void setCurrentExperimentRun(int i) {
        currentExperimentRun = getExperimentRunAt(i);
    }

    public boolean setCurrentExperimentRun(IExperimentSensor experimentRun) {
        if (!experimentSensors.contains(experimentRun))
            return false;
        currentExperimentRun = experimentRun;
        return true;
    }

    public IExperimentSensor getCurrentExperimentSensor() {
        return currentExperimentRun;
    }

    public IExperimentSensor getExperimentRunAt(int i) {
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

    public void removeExperimentSensor(IExperimentSensor experimentRun) {
        // update the current experiment run first if necessary
        if (experimentRun == getCurrentExperimentSensor()) {
            int index = experimentSensors.indexOf(experimentRun);
            if (index > 0)
                setCurrentExperimentRun(0);
            else if (index + 1 < experimentSensors.size())
                setCurrentExperimentRun(index + 1);
            else
                setCurrentExperimentRun(null);
        }
        experimentSensors.remove(experimentRun);

        if (experimentActivity != null)
            experimentRun.destroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        data.onSaveInstanceState(outState);

        int i = 0;
        for (IExperimentSensor experimentSensor : experimentSensors) {
            Bundle runBundle = new Bundle();
            experimentSensor.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }

        // store plugin information
        Bundle experimentRunClasses = new Bundle();
        i = 0;
        for (IExperimentSensor experimentSensor : experimentSensors) {
            experimentRunClasses.putString(Integer.toString(i), experimentSensor.getPlugin().getName());
            i++;
        }
        outState.putBundle("run_plugins", experimentRunClasses);
        outState.putInt("run_plugin_count", experimentSensors.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        data.onRestoreInstanceState(savedInstanceState);

        int runClassesCount = savedInstanceState.getInt("run_plugin_count");
        Bundle experimentRunClasses = savedInstanceState.getBundle("run_plugins");
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (int i = 0; i < runClassesCount; i++) {
            String runName = experimentRunClasses.getString(Integer.toString(i));
            IExperimentPlugin plugin = factory.findExperimentPlugin(runName);
            IExperimentSensor experimentSensor = plugin.createExperimentSensor(experiment.getActivity());
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentSensor.onRestoreInstanceState(state);
            addExperimentSensor(experimentSensor);
        }
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public boolean dataTaken() {
        for (IExperimentSensor experimentRun : experimentSensors) {
            if (experimentRun.dataTaken())
                return true;
        }
        return false;
    }

    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        if (saveData) {
            storageDir.mkdirs();
            data.saveToFile(new File(storageDir, EXPERIMENT_RUN_GROUP_FILE_NAME));
        }

        for (IExperimentSensor experimentSensor : experimentSensors) {
            experimentSensor.finishExperiment(saveData,
                    new File(storageDir, experimentSensor.getClass().getSimpleName()));
        }
    }
}
