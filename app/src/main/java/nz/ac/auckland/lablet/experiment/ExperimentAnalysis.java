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
        final static public String RUN_ID_KEY = "runID";
        final static public String ANALYSIS_UID_KEY = "analysisUID";

        final public int runId;
        final public String analysisUid;

        public AnalysisRef(Bundle archive) {
            runId = archive.getInt(RUN_ID_KEY);
            analysisUid = archive.getString(ANALYSIS_UID_KEY);
        }

        public AnalysisRef(int runId, String analysisUid) {
            this.runId = runId;
            this.analysisUid = analysisUid;
        }

        public Bundle toBundle() {
            Bundle archive = new Bundle();
            archive.putInt(RUN_ID_KEY, runId);
            archive.putString(ANALYSIS_UID_KEY, analysisUid);
            return archive;
        }
    }

    public static class AnalysisEntry {
        final public IDataAnalysis analysis;
        final public String analysisUid;
        final public IAnalysisPlugin plugin;
        final public File storageDir;

        public AnalysisEntry(IDataAnalysis analysis, String analysisUid, IAnalysisPlugin plugin, File storageDir) {
            this.analysis = analysis;
            this.analysisUid = analysisUid;
            this.plugin = plugin;
            this.storageDir = storageDir;
        }
    }

    public static class AnalysisRunEntry {
        final public List<AnalysisEntry> analysisList = new ArrayList<>();

        public AnalysisEntry getAnalysisEntry(String analysisUid) {
            for (AnalysisEntry analysisEntry : analysisList) {
                if (analysisEntry.analysisUid.equals(analysisUid))
                    return analysisEntry;
            }
            return null;
        }
    }

    protected ExperimentData experimentData = null;

    protected List<AnalysisRunEntry> analysisRuns = new ArrayList<>();
    protected AnalysisRunEntry currentAnalysisRun;
    protected IDataAnalysis currentAnalysis;

    public int getNumberOfRuns() {
        return analysisRuns.size();
    }

    public IDataAnalysis getCurrentAnalysis() {
        return currentAnalysis;
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

    protected String generateNewUid(IDataAnalysis dataAnalysis) {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_HH-mm-ss", new java.util.Date());

        String uid = dataAnalysis.getIdentifier();
        uid += dateString + "_";
        uid += (int)(100000f * Math.random());
        return uid;
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

                    analysisRunEntry.analysisList.add(analysisEntry);
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

                    analysisRunEntry.analysisList.add(new AnalysisEntry(dataAnalysis, generateNewUid(dataAnalysis),
                            plugin, storageDir));
                }
            }
        }

        if (getNumberOfRuns() == 0 || getAnalysisRunAt(0).analysisList.size() == 0)
            return;

        setCurrentAnalysisRun(0);
        setCurrentAnalysis(0);
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
        setCurrentAnalysis(0);
        return true;
    }

    public void setCurrentAnalysis(int analysis) {
        currentAnalysis = currentAnalysisRun.analysisList.get(analysis).analysis;
    }

    public boolean setCurrentAnalysis(String analysisId) {
        for (int i = 0; i < currentAnalysisRun.analysisList.size(); i++) {
            AnalysisEntry analysisEntry = currentAnalysisRun.analysisList.get(i);
            if (analysisEntry.analysisUid.equals(analysisId)) {
                setCurrentAnalysis(i);
                return true;
            }
        }
        return false;
    }
}
