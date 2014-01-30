/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


abstract public class ExperimentActivity extends FragmentActivity {
    protected Experiment experiment = null;
    protected ExperimentPlugin plugin = null;

    final String EXPERIMENT_DATA_FILE_NAME = "experiment_data.xml";

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

    protected Bundle loadBundleFromFile(File file) {
        Bundle bundle;
        InputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        PersistentBundle persistentBundle = new PersistentBundle();
        try {
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }

        return bundle;
    }

    protected boolean loadExperiment(Intent intent) {
        if (intent == null) {
            showErrorAndFinish("can't load experiment (Intent is null)");
            return false;
        }

        File storageDir = null;
        Bundle bundle = null;

        String experimentPath = intent.getStringExtra("experiment_path");
        if (experimentPath != null) {
            storageDir = new File(experimentPath);
            File file = new File(storageDir, EXPERIMENT_DATA_FILE_NAME);
            bundle = loadBundleFromFile(file);
        }

        if (bundle == null) {
            showErrorAndFinish("can't read experiment file");
            return false;
        }

        String experimentIdentifier = bundle.getString("experiment_identifier");
        if (experimentIdentifier == null) {
            showErrorAndFinish("invalid experiment data");
            return false;
        }

        ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
        plugin = factory.findExperimentPlugin(experimentIdentifier);
        if (plugin == null) {
            showErrorAndFinish("unknown experiment type");
            return false;
        }

        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null) {
            showErrorAndFinish("failed to load experiment data");
            return false;
        }
        experiment = plugin.loadExperiment(this, experimentData, storageDir);

        if (experiment == null) {
            showErrorAndFinish("can't load experiment");
            return false;
        }

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
