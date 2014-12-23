/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ExperimentAnalysis {
    public static class AnalysisRef {
        final public int run;
        final public int sensor;
        final public String analysisId;

        public AnalysisRef(Bundle archive) {
            run = archive.getInt("run");
            sensor = archive.getInt("sensor");
            analysisId = archive.getString("analysisId");
        }

        public AnalysisRef(int run, int sensor, String analysisId) {
            this.run = run;
            this.sensor = sensor;
            this.analysisId = analysisId;
        }

        public Bundle toBundle() {
            Bundle archive = new Bundle();
            archive.putInt("run", run);
            archive.putInt("sensor", sensor);
            archive.putString("analysisId", analysisId);
            return archive;
        }
    }

    public static class AnalysisEntry {
        final public IDataAnalysis analysis;
        final public IAnalysisPlugin plugin;

        public AnalysisEntry(IDataAnalysis analysis, IAnalysisPlugin plugin) {
            this.analysis = analysis;
            this.plugin = plugin;
        }
    }

    public static class AnalysisDataEntry {
        final public List<AnalysisEntry> analysisList = new ArrayList<>();

        public AnalysisEntry getAnalysisEntry(String analysis) {
            for (AnalysisEntry analysisEntry : analysisList) {
                if (analysisEntry.analysis.getIdentifier().equals(analysis))
                    return analysisEntry;
            }
            return null;
        }
    }

    public static class AnalysisRunEntry {
        final public List<AnalysisDataEntry> analysisDataList = new ArrayList<>();

        public AnalysisDataEntry getSensorEntry(int index) {
            return analysisDataList.get(index);
        }
    }

    protected ExperimentData experimentData = null;

    protected List<AnalysisRunEntry> analysisRuns = new ArrayList<>();
    protected AnalysisRunEntry currentAnalysisRun;
    protected IDataAnalysis currentSensorAnalysis;

    public int getNumberOfRuns() {
        return analysisRuns.size();
    }

    static public File getAnalysisStorageFor(ExperimentData experimentData, int run, IDataAnalysis analysis) {
        File dir = experimentData.getStorageDir().getParentFile();
        dir = new File(dir, "analysis");
        dir = new File(dir, "run" + Integer.toString(run));
        ISensorData sensorData = analysis.getData();
        dir = new File(dir, sensorData.getDataType());
        dir = new File(dir, analysis.getIdentifier());
        return dir;
    }

    public IDataAnalysis getCurrentSensorAnalysis() {
        return currentSensorAnalysis;
    }

    /**
     * Set the experiment data and load a sensor analysis for each sensor.
     *
     * For now it is assumed that each sensor only has one analysis.
     *
     * @param experimentData the experiment data
     */
    public void setExperimentData(ExperimentData experimentData) {
        this.experimentData = experimentData;

        List<ExperimentData.Run> runs = experimentData.getRuns();
        for (ExperimentData.Run run : runs) {
            AnalysisRunEntry analysisRunEntry = new AnalysisRunEntry();
            for (ISensorData sensorData : run.sensorDataList) {
                AnalysisDataEntry analysisDataEntry = new AnalysisDataEntry();
                ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                List<IAnalysisPlugin> pluginList = factory.analysisPluginsFor(sensorData);
                if (pluginList.size() == 0)
                    continue;
                IAnalysisPlugin plugin = pluginList.get(0);

                IDataAnalysis sensorAnalysis = plugin.createDataAnalysis(sensorData);
                File storage = getAnalysisStorageFor(experimentData, runs.indexOf(run), sensorAnalysis);
                // if loading fails we add the entry anyway and start a new analysis
                ExperimentHelper.loadSensorAnalysis(sensorAnalysis, storage);
                analysisDataEntry.analysisList.add(new AnalysisEntry(sensorAnalysis, plugin));
                analysisRunEntry.analysisDataList.add(analysisDataEntry);
            }
            analysisRuns.add(analysisRunEntry);
        }

        if (getNumberOfRuns() == 0 || getAnalysisRunAt(0).analysisDataList.size() == 0)
            return;

        setCurrentAnalysisRun(0);
        setCurrentSensorAnalysis(0, 0);
    }


    public ExperimentData getExperimentData() {
        return experimentData;
    }

    public AnalysisRunEntry getCurrentAnalysisRun() {
        return currentAnalysisRun;
    }

    public AnalysisRunEntry getAnalysisRunAt(int index) {
        return analysisRuns.get(index);
    }

    public List<AnalysisRunEntry> getAnalysisRuns() {
        return analysisRuns;
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

    protected boolean setCurrentAnalysisRun(int index) {
        if (index >= analysisRuns.size())
            return false;
        currentAnalysisRun = analysisRuns.get(index);
        setCurrentSensorAnalysis(0, 0);
        return true;
    }

    protected void setCurrentSensorAnalysis(int sensor, int analysis) {
        currentSensorAnalysis = currentAnalysisRun.analysisDataList.get(sensor).analysisList.get(analysis).analysis;
    }
}
