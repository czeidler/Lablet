/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ExperimentAnalysis {
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

    public static class AnalysisEntry {
        final public ISensorAnalysis analysis;
        final public IAnalysisPlugin plugin;

        public AnalysisEntry(ISensorAnalysis analysis, IAnalysisPlugin plugin) {
            this.analysis = analysis;
            this.plugin = plugin;
        }
    }

    public static class AnalysisSensorEntry {
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
        final public List<AnalysisSensorEntry> sensorList = new ArrayList<>();

        public AnalysisSensorEntry getSensorEntry(int index) {
            return sensorList.get(index);
        }
    }

    protected ExperimentData experimentData = null;

    protected List<AnalysisRunEntry> analysisRuns = new ArrayList<>();
    protected AnalysisRunEntry currentAnalysisRun;
    protected ISensorAnalysis currentSensorAnalysis;

    public int getNumberOfRuns() {
        return analysisRuns.size();
    }

    static public File getAnalysisStorageFor(ExperimentData experimentData, int run, ISensorAnalysis analysis) {
        File dir = experimentData.getStorageDir().getParentFile();
        dir = new File(dir, "analysis");
        dir = new File(dir, "run" + Integer.toString(run));
        ISensorData sensorData = analysis.getData();
        dir = new File(dir, sensorData.getDataType());
        dir = new File(dir, analysis.getIdentifier());
        return dir;
    }

    public ISensorAnalysis getCurrentSensorAnalysis() {
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

        List<ExperimentData.RunEntry> runs = experimentData.getRuns();
        for (ExperimentData.RunEntry runEntry : runs) {
            AnalysisRunEntry analysisRunEntry = new AnalysisRunEntry();
            for (ISensorData sensorData : runEntry.sensorDataList) {
                AnalysisSensorEntry analysisSensorEntry = new AnalysisSensorEntry();
                ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                List<IAnalysisPlugin> pluginList = factory.analysisPluginsFor(sensorData);
                if (pluginList.size() == 0)
                    continue;
                IAnalysisPlugin plugin = pluginList.get(0);

                ISensorAnalysis sensorAnalysis = plugin.createSensorAnalysis(sensorData);
                File storage = getAnalysisStorageFor(experimentData, runs.indexOf(runEntry), sensorAnalysis);
                ExperimentHelper.loadSensorAnalysis(sensorAnalysis, storage);
                analysisSensorEntry.analysisList.add(new AnalysisEntry(sensorAnalysis, plugin));
                analysisRunEntry.sensorList.add(analysisSensorEntry);
            }
            analysisRuns.add(analysisRunEntry);
        }

        if (getNumberOfRuns() == 0 || getAnalysisRunAt(0).sensorList.size() == 0)
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
        currentSensorAnalysis = currentAnalysisRun.sensorList.get(sensor).analysisList.get(analysis).analysis;
    }
}
