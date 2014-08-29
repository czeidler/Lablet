/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import nz.ac.auckland.lablet.experiment.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class for activities that analyze an experiment.
 */
abstract public class ExperimentDataActivity extends FragmentActivity {

    protected ExperimentData experimentData = null;

    final protected List<List<ISensorAnalysis>> analysisRuns = new ArrayList<>();
    protected List<ISensorAnalysis> currentAnalysisRun;
    protected ISensorAnalysis currentAnalysisSensor;

    protected void setCurrentAnalysisRun(int index) {
        currentAnalysisRun = analysisRuns.get(index);
        setCurrentAnalysisSensor(0);
    }

    protected void setCurrentAnalysisSensor(int index) {
        currentAnalysisSensor = currentAnalysisRun.get(index);
    }

    public List<ISensorAnalysis> getCurrentAnalysisRun() {
        return currentAnalysisRun;
    }


    private File getAnalysisStorageFor(ExperimentData.RunEntry runEntry, SensorData sensorData, IAnalysisPlugin plugin) {
        File dir = sensorData.getStorageDir().getParentFile();
        dir = new File(dir, "analysis");
        dir = new File(dir, Integer.toString(runEntry.sensorDataList.indexOf(sensorData)));
        dir = new File(dir, sensorData.getDataType());
        dir = new File(dir, plugin.getName());
        return dir;
    }

    private void setExperimentData(ExperimentData experimentData) {
        this.experimentData = experimentData;

        for (ExperimentData.RunEntry runEntry : experimentData.getRuns()) {
            List<ISensorAnalysis> analysisEntryList = new ArrayList<>();
            for (SensorData sensorData : runEntry.sensorDataList) {
                ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                List<IAnalysisPlugin> pluginList = factory.analysisPluginsFor(sensorData);
                if (pluginList.size() == 0)
                    continue;
                IAnalysisPlugin plugin = pluginList.get(0);

                File storage = getAnalysisStorageFor(runEntry, sensorData, plugin);
                ISensorAnalysis sensorAnalysis = ExperimentLoader.setupSensorAnalysis(sensorData, plugin, storage);
                if (sensorAnalysis == null)
                    continue;
                analysisEntryList.add(sensorAnalysis);
            }
            analysisRuns.add(analysisEntryList);
        }

        if (analysisRuns.size() == 0 || analysisRuns.get(0).size() == 0) {
            showErrorAndFinish("No experiment found.");
            return;
        }

        setCurrentAnalysisRun(0);
        setCurrentAnalysisSensor(0);
    }

    public ExperimentData getExperimentData() {
        return experimentData;
    }

    protected boolean loadExperiment(Intent intent) {
        if (intent == null) {
            showErrorAndFinish("can't load experiment (Intent is null)");
            return false;
        }

        String experimentPath = intent.getStringExtra("experiment_path");
        int runId = intent.getIntExtra("run_id", 0);
        int sensorId = intent.getIntExtra("sensor_id", 0);

        ExperimentData experimentData = new ExperimentData();
        if (!experimentData.load(this, new File(experimentPath, "data"))) {
            showErrorAndFinish(experimentData.getLoadError());
            return false;
        }
        setExperimentData(experimentData);

        setCurrentAnalysisRun(runId);
        setCurrentAnalysisSensor(sensorId);
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

    static public File getDefaultExperimentBaseDir(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        return new File(baseDir, "experiments");
    }
}
