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

public class ExperimentRunGroup {
    private ExperimentRunData data = new ExperimentRunData();

    final private List<IExperimentSensor> experimentRuns = new ArrayList<>();
    private IExperimentSensor currentExperimentRun = null;
    private Experiment experiment;
    private String subStorageDirectory;
    private Activity experimentRunActivity;

    final static public String EXPERIMENT_RUN_GROUP_FILE_NAME = "experiment_run_group.xml";

    static public ExperimentRunGroup createExperimentRunGroup(List<String> experimentRuns, Activity activity) {
        ExperimentRunGroup experimentRunGroup = new ExperimentRunGroup();

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (String experimentRunName : experimentRuns) {
            IExperimentPlugin plugin = factory.findExperimentPlugin(experimentRunName);
            if (plugin == null)
                continue;
            IExperimentSensor experimentRun = plugin.createExperimentSensor(activity);
            experimentRunGroup.addExperimentRun(experimentRun);
        }

        return experimentRunGroup;
    }

    public void setExperiment(Experiment experiment, String subStorageDirectory) {
        this.experiment = experiment;
        this.subStorageDirectory = subStorageDirectory;
    }

    public void activateExperimentRuns(Activity activity) {
        if (experimentRunActivity != null) {
            for (IExperimentSensor experimentRun : experimentRuns)
                experimentRun.destroy();
        }
        experimentRunActivity = activity;
        if (experimentRunActivity != null) {
            for (IExperimentSensor experimentRun : experimentRuns)
                experimentRun.init(experimentRunActivity);
        }
    }

    public boolean isActive() {
        return experimentRunActivity != null;
    }

    public int getExperimentRunCount() {
        return experimentRuns.size();
    }

    public void setCurrentExperimentRun(int i) {
        currentExperimentRun = getExperimentRunAt(i);
    }

    public boolean setCurrentExperimentRun(IExperimentSensor experimentRun) {
        if (!experimentRuns.contains(experimentRun))
            return false;
        currentExperimentRun = experimentRun;
        return true;
    }

    public IExperimentSensor getCurrentExperimentRun() {
        return currentExperimentRun;
    }

    public IExperimentSensor getExperimentRunAt(int i) {
        return experimentRuns.get(i);
    }

    public List<IExperimentSensor> getExperimentRuns() {
        return experimentRuns;
    }

    public boolean addExperimentRun(IExperimentSensor experimentRun) {
        if (experimentRun.getExperimentRunGroup() != null)
            return false;
        experimentRuns.add(experimentRun);
        experimentRun.setExperimentRunGroup(this);

        if (experimentRunActivity != null)
            experimentRun.init(experimentRunActivity);
        return true;
    }

    public void removeExperimentRun(IExperimentSensor experimentRun) {
        // update the current experiment run first if necessary
        if (experimentRun == getCurrentExperimentRun()) {
            int index = experimentRuns.indexOf(experimentRun);
            if (index > 0)
                setCurrentExperimentRun(0);
            else if (index + 1 < experimentRuns.size())
                setCurrentExperimentRun(index + 1);
            else
                setCurrentExperimentRun(null);
        }
        experimentRuns.remove(experimentRun);

        if (experimentRunActivity != null)
            experimentRun.destroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        data.onSaveInstanceState(outState);

        int i = 0;
        for (IExperimentSensor experimentRun : experimentRuns) {
            Bundle runBundle = new Bundle();
            experimentRun.onSaveInstanceState(runBundle);
            outState.putBundle(Integer.toString(i), runBundle);
            i++;
        }

        // store plugin information
        Bundle experimentRunClasses = new Bundle();
        i = 0;
        for (IExperimentSensor experimentRun : experimentRuns) {
            experimentRunClasses.putString(Integer.toString(i), experimentRun.getClass().getSimpleName());
            i++;
        }
        outState.putBundle("runClasses", experimentRunClasses);
        outState.putInt("runClassesCount", experimentRuns.size());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        data.onRestoreInstanceState(savedInstanceState);

        int runClassesCount = savedInstanceState.getInt("runClassesCount");
        Bundle experimentRunClasses = savedInstanceState.getBundle("runClasses");
        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        for (int i = 0; i < runClassesCount; i++) {
            String runName = experimentRunClasses.getString(Integer.toString(i));
            IExperimentPlugin plugin = factory.findExperimentPlugin(runName);
            IExperimentSensor experimentRun = plugin.createExperimentSensor(experiment.getActivity());
            Bundle state = savedInstanceState.getBundle(Integer.toString(i));
            experimentRun.onRestoreInstanceState(state);
            experimentRuns.add(experimentRun);
        }
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public boolean dataTaken() {
        for (IExperimentSensor experimentRun : experimentRuns) {
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

        for (IExperimentSensor experimentRun : experimentRuns) {
            experimentRun.finishExperiment(saveData, new File(storageDir, experimentRun.getClass().getSimpleName()));
        }
    }
}
