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
abstract public class ExperimentAnalysisBaseActivity extends FragmentActivity {
    public static class AnalysisRef {
        final public int run;
        final public int sensor;
        final public String analysisId;

        public AnalysisRef(int run, int sensor, String analysisId) {
            this.run = run;
            this.sensor = sensor;
            this.analysisId = analysisId;
        }
    }

    class AnalysisEntry {
        public ISensorAnalysis analysis;
        public IAnalysisPlugin plugin;

        public AnalysisEntry(ISensorAnalysis analysis, IAnalysisPlugin plugin) {
            this.analysis = analysis;
            this.plugin = plugin;
        }
    }

    class AnalysisSensorEntry {
        public List<AnalysisEntry> analysisList = new ArrayList<>();

        public AnalysisEntry getAnalysisEntry(String analysis) {
            for (AnalysisEntry analysisEntry : analysisList) {
                if (analysisEntry.analysis.getIdentifier() == analysis)
                    return analysisEntry;
            }
            return null;
        }
    }

    class AnalysisRunEntry {
        public List<AnalysisSensorEntry> sensorList = new ArrayList();

        public AnalysisSensorEntry getSensorEntry(int index) {
            return sensorList.get(index);
        }
    }

    protected ExperimentData experimentData = null;

    protected List<AnalysisRunEntry> analysisRuns = new ArrayList<>();
    protected AnalysisRunEntry currentAnalysisRun;
    protected ISensorAnalysis currentSensorAnalysis;

    protected void setCurrentAnalysisRun(int index) {
        currentAnalysisRun = analysisRuns.get(index);
        setCurrentSensorAnalysis(0, 0);
    }

    protected void setCurrentSensorAnalysis(int sensor, int analysis) {
        currentSensorAnalysis = currentAnalysisRun.sensorList.get(sensor).analysisList.get(analysis).analysis;
    }

    public AnalysisRunEntry getCurrentAnalysisRun() {
        return currentAnalysisRun;
    }

    public AnalysisEntry getAnalysisEntry(AnalysisRef ref) {
        return analysisRuns.get(ref.run).getSensorEntry(ref.sensor).getAnalysisEntry(ref.analysisId);
    }

    public IAnalysisPlugin getAnalysisPlugin(AnalysisRef analysisRef) {
        return getAnalysisEntry(analysisRef).plugin;
    }

    public int getCurrentAnalysisRunIndex() {
        return analysisRuns.indexOf(currentAnalysisRun);
    }

    protected File getAnalysisStorageFor(int run, ISensorAnalysis analysis) {
        SensorData sensorData = analysis.getData();
        File dir = sensorData.getStorageDir().getParentFile();
        dir = new File(dir, "analysis");
        dir = new File(dir, Integer.toString(run));
        dir = new File(dir, sensorData.getDataType());
        dir = new File(dir, analysis.getIdentifier());
        return dir;
    }

    /**
     * Set the experiment data and load a sensor analysis for each sensor.
     *
     * For now it is assumed that each sensor only has one analysis.
     *
     * @param experimentData the experiment data
     */
    private void setExperimentData(ExperimentData experimentData) {
        this.experimentData = experimentData;

        List<ExperimentData.RunEntry> runs = experimentData.getRuns();
        for (ExperimentData.RunEntry runEntry : runs) {
            AnalysisRunEntry analysisRunEntry = new AnalysisRunEntry();
            for (SensorData sensorData : runEntry.sensorDataList) {
                AnalysisSensorEntry analysisSensorEntry = new AnalysisSensorEntry();
                ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                List<IAnalysisPlugin> pluginList = factory.analysisPluginsFor(sensorData);
                if (pluginList.size() == 0)
                    continue;
                IAnalysisPlugin plugin = pluginList.get(0);

                ISensorAnalysis sensorAnalysis = plugin.createSensorAnalysis(sensorData);
                File storage = getAnalysisStorageFor(runs.indexOf(runEntry), sensorAnalysis);
                ExperimentLoader.setupSensorAnalysis(sensorData, sensorAnalysis, storage);
                if (sensorAnalysis == null)
                    continue;
                analysisSensorEntry.analysisList.add(new AnalysisEntry(sensorAnalysis, plugin));
                analysisRunEntry.sensorList.add(analysisSensorEntry);
            }
            analysisRuns.add(analysisRunEntry);
        }

        if (analysisRuns.size() == 0 || analysisRuns.get(0).sensorList.size() == 0) {
            showErrorAndFinish("No experiment found.");
            return;
        }

        setCurrentAnalysisRun(0);
        setCurrentSensorAnalysis(0, 0);
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
        setCurrentSensorAnalysis(sensorId, 0);
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
