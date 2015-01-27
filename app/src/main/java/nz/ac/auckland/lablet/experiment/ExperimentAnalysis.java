/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ExperimentAnalysis {
    public static class AnalysisRef {
        final static private String KEY_RUN_ID = "runID";
        final static private String KEY_DATA_ID = "dataID";
        final static private String KEY_ANALYSIS_ID = "analysisID";

        final public int runId;
        final public int dataId;
        final public String analysisId;

        public AnalysisRef(Bundle archive) {
            runId = archive.getInt(KEY_RUN_ID);
            dataId = archive.getInt(KEY_DATA_ID);
            analysisId = archive.getString(KEY_ANALYSIS_ID);
        }

        public AnalysisRef(int runId, int dataId, String analysisId) {
            this.runId = runId;
            this.dataId = dataId;
            this.analysisId = analysisId;
        }

        public Bundle toBundle() {
            Bundle archive = new Bundle();
            archive.putInt(KEY_RUN_ID, runId);
            archive.putInt(KEY_DATA_ID, dataId);
            archive.putString(KEY_ANALYSIS_ID, analysisId);
            return archive;
        }
    }

    public static class AnalysisEntry {
        final public IDataAnalysis analysis;
        final public IAnalysisPlugin plugin;
        final public File storageDir;

        public AnalysisEntry(IDataAnalysis analysis, IAnalysisPlugin plugin, File storageDir) {
            this.analysis = analysis;
            this.plugin = plugin;
            this.storageDir = storageDir;
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
    }

    protected ExperimentData experimentData = null;

    protected List<AnalysisRunEntry> analysisRuns = new ArrayList<>();
    protected AnalysisRunEntry currentAnalysisRun;
    protected IDataAnalysis currentSensorAnalysis;

    public int getNumberOfRuns() {
        return analysisRuns.size();
    }



    public IDataAnalysis getCurrentSensorAnalysis() {
        return currentSensorAnalysis;
    }

    static public File getAnalysisRunStorage(ExperimentData experimentData, int run) {
        File dir = experimentData.getStorageDir().getParentFile();
        dir = new File(dir, "analysis");
        dir = new File(dir, "run" + Integer.toString(run));
        return dir;
    }

    static private File getAnalysisStorageFor(ExperimentData experimentData, int run, IDataAnalysis analysis) {
        File dir = ExperimentAnalysis.getAnalysisRunStorage(experimentData, run);
        String analysisId = analysis.getIdentifier();
        for (int i = 0; true; i++) {
            String name = analysisId;
            if (i > 0)
                name += i;

            dir = new File(dir, name);
            if (dir.exists())
                continue;
            return dir;
        }
    }

    /**
     * Set the experiment data and load a sensor analysis for all data.
     *
     * For now it is assumed that each data entry only has one analysis.
     *
     * @param experimentData the experiment data
     */
    public void setExperimentData(ExperimentData experimentData) {
        this.experimentData = experimentData;

        List<ExperimentData.RunData> runDataList = experimentData.getRunDataList();
        for (ExperimentData.RunData runData : runDataList) {
            AnalysisRunEntry analysisRunEntry = new AnalysisRunEntry();
            analysisRuns.add(analysisRunEntry);

            File analysisRunDir = getAnalysisRunStorage(experimentData, runDataList.indexOf(runData));
            String[] analysisDirs = analysisRunDir.list();
            if (analysisDirs != null && analysisDirs.length > 0) {
                for (String analysisDir : analysisDirs) {
                    File storage = new File(analysisRunDir, analysisDir);
                    // try to load exiting analyses
                    AnalysisEntry analysisEntry = ExperimentHelper.loadSensorAnalysis(storage, runData.sensorDataList);
                    if (analysisEntry == null)
                        continue;

                    AnalysisDataEntry analysisDataEntry = new AnalysisDataEntry();
                    analysisDataEntry.analysisList.add(analysisEntry);
                    analysisRunEntry.analysisDataList.add(analysisDataEntry);
                }
            } else {
                // assign analyses to the data
                for (ISensorData sensorData : runData.sensorDataList) {

                    ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                    List<IAnalysisPlugin> pluginList = factory.analysisPluginsFor(sensorData);
                    if (pluginList.size() == 0)
                        continue;
                    IAnalysisPlugin plugin = pluginList.get(0);

                    IDataAnalysis dataAnalysis = plugin.createDataAnalysis(sensorData);
                    if (dataAnalysis == null)
                        continue;

                    File storageDir = getAnalysisStorageFor(experimentData, runDataList.indexOf(runData), dataAnalysis);

                    AnalysisDataEntry analysisDataEntry = new AnalysisDataEntry();
                    analysisDataEntry.analysisList.add(new AnalysisEntry(dataAnalysis, plugin, storageDir));
                    analysisRunEntry.analysisDataList.add(analysisDataEntry);
                }
            }
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

    public int getCurrentAnalysisRunIndex() {
        return analysisRuns.indexOf(currentAnalysisRun);
    }

    public boolean setCurrentAnalysisRun(int index) {
        if (index >= analysisRuns.size())
            return false;
        currentAnalysisRun = analysisRuns.get(index);
        setCurrentSensorAnalysis(0, 0);
        return true;
    }

    public void setCurrentSensorAnalysis(int sensor, int analysis) {
        currentSensorAnalysis = currentAnalysisRun.analysisDataList.get(sensor).analysisList.get(analysis).analysis;
    }
}
