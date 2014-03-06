/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import nz.ac.aucklanduni.physics.tracker.Experiment;
import nz.ac.aucklanduni.physics.tracker.ExperimentLoader;
import nz.ac.aucklanduni.physics.tracker.ExperimentPlugin;
import nz.ac.aucklanduni.physics.tracker.PersistentBundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;

abstract public class ExperimentActivity extends FragmentActivity {
    protected Experiment experiment = null;
    protected ExperimentPlugin plugin = null;

    final static public String EXPERIMENT_DATA_FILE_NAME = "experiment_data.xml";

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
    public ExperimentPlugin getExperimentPlugin() {
        return plugin;
    }

    protected boolean loadExperiment(Intent intent) {
        if (intent == null) {
            showErrorAndFinish("can't load experiment (Intent is null)");
            return false;
        }

        String experimentPath = intent.getStringExtra("experiment_path");

        ExperimentLoader.Result result = new ExperimentLoader.Result();
        if (!ExperimentLoader.loadExperiment(this, experimentPath, result)) {
            showErrorAndFinish(result.loadError);
            return false;
        }

        plugin = result.plugin;
        experiment = result.experiment;

        return true;
    }

    protected void showErrorAndFinish(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        builder.setNeutralButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
    }

    protected void saveExperimentDataToFile() throws IOException {
        Bundle bundle = new Bundle();
        bundle.putString("experiment_identifier", experiment.getIdentifier());
        Bundle experimentData = experiment.experimentDataToBundle();
        bundle.putBundle("data", experimentData);
        experiment.onSaveAdditionalData(getStorageDir());

        // save the bundle
        File projectFile = new File(getStorageDir(), EXPERIMENT_DATA_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }


    protected File getStorageDir() throws IOException {
        String directoryName = getExperiment().getUid();

        File path = Experiment.getMainExperimentDir(this);
        if (path != null) {
            File file = new File(path, directoryName);
            if (!file.exists()) {
                if (!file.mkdir())
                    throw new IOException();
            }
            return file;
        }
        throw new IOException();
    }

    protected boolean deleteStorageDir() {
        File file;
        try {
            file = getStorageDir();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return recursiveDeleteFile(file);
    }

    static public boolean recursiveDeleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                if (!recursiveDeleteFile(new File(file, child)))
                    return false;
            }
        }
        return file.delete();
    }
}
