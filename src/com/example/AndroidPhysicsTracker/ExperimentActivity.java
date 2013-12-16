package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.Time;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by lec on 16/12/13.
 */
abstract public class ExperimentActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createExperiment();
        Experiment experiment = getExperiment();

        try {
            experiment.setStorageDir(getStorageDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract protected void createExperiment();
    abstract public Experiment getExperiment();

    protected void saveExperimentToFile() throws IOException {
        Experiment experiment = getExperiment();
        Bundle bundle = new Bundle();
        bundle.putString("experiment_id", experiment.getUid());
        Bundle experimentData = experiment.toBundle();
        bundle.putBundle("data", experimentData);

        // save the bundle
        File projectFile = new File(getStorageDir(), "experiment.xml");
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    protected File getStorageDir() throws IOException {
        String directoryName = getExperiment().getUid();

        File path = getExternalFilesDir(null);
        if (path != null) {
            File file = new File(path, directoryName);
            if (!file.exists())
                file.mkdir();
            return file;
        }
        throw new IOException();
    }

    protected boolean deleteStorageDir() {
        File file = null;
        try {
            file = getStorageDir();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return recursiveDeleteFile(file);
    }

    private boolean recursiveDeleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                if (!recursiveDeleteFile(new File(file, child)));
                    return false;
            }
        }
        return file.delete();
    }
}
