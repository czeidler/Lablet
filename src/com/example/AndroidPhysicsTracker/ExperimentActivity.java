package com.example.AndroidPhysicsTracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


abstract public class ExperimentActivity extends FragmentActivity {
    protected Experiment experiment = null;
    protected ExperimentPlugin plugin = null;

    protected void setExperiment(Experiment experiment) {
        this.experiment = experiment;
        try {
            experiment.setStorageDir(getStorageDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Experiment getExperiment() {
        return experiment;
    }

    protected void loadExperiment(Intent intent) {
        if (intent == null)
            showErrorAndFinish("can't load experiment (Intent is null)");

        File storageDir = null;
        Bundle bundle = null;

        String experimentPath = intent.getStringExtra("experiment_path");
        if (experimentPath != null) {
            storageDir = new File(experimentPath);
            File file = new File(storageDir, "experiment.xml");

            InputStream inStream = null;
            try {
                inStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showErrorAndFinish("experiment file not found");
            }

            PersistentBundle persistentBundle = new PersistentBundle();
            try {
                bundle = persistentBundle.unflattenBundle(inStream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                showErrorAndFinish("can't read experiment file");
            }
            String experimentIdentifier = bundle.getString("experiment_identifier");

            ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
            plugin = factory.findExperimentPlugin(experimentIdentifier);
        }

        if (plugin == null)
            showErrorAndFinish("unknown experiment type");

        assert bundle != null;
        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null)
            showErrorAndFinish("failed to load experiment data");
        experiment = plugin.loadExperiment(this, experimentData, storageDir);

        if (experiment == null)
            showErrorAndFinish("can't load experiment");
    }

    protected void showErrorAndFinish(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        builder.setNeutralButton("Ok", null);
        builder.create().show();
        finish();
    }

    protected void saveExperimentToFile() throws IOException {
        Experiment experiment = getExperiment();
        Bundle bundle = new Bundle();
        bundle.putString("experiment_identifier", experiment.getIdentifier());
        Bundle experimentData = experiment.toBundle();
        bundle.putBundle("data", experimentData);
        experiment.onSaveAdditionalData(getStorageDir());

        // save the bundle
        File projectFile = new File(getStorageDir(), "experiment.xml");
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    protected File getStorageDir() throws IOException {
        String directoryName = getExperiment().getUid();

        File path = Experiment.getMainExperimentDir(this);
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
