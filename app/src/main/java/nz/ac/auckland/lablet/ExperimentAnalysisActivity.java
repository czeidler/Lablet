/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import nz.ac.auckland.lablet.experiment.*;

import java.io.*;
import java.util.List;


/**
 * Common activity to do the experiment analysis.
 * <p>
 * You can put the following extra options into the intent:
 * <ul>
 * <li>boolean field "first_start_with_run_settings", to open the run settings on start</li>
 * <li>boolean field "first_start_with_run_settings_help", to open the run settings with help screen on start</li>
 * </ul>
 * </p>
 */
public class ExperimentAnalysisActivity extends ExperimentAnalysisBaseActivity {
    private ViewPager pager;

    public ViewPager getViewPager() {
        return pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // gui stuff:

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        pager = (ViewPager)findViewById(R.id.pager);
        final ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(0);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                experimentAnalysis.setCurrentSensorAnalysis(position, 0);

                // repopulate the menu
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (experimentAnalysis == null || experimentAnalysis.getNumberOfRuns() == 0)
            return;

        final ExperimentData experimentData = experimentAnalysis.getExperimentData();
        List<ExperimentAnalysis.AnalysisRunEntry> analysisRuns = experimentAnalysis.getAnalysisRuns();
        for (ExperimentAnalysis.AnalysisRunEntry analysisRun : analysisRuns) {
            for (ExperimentAnalysis.AnalysisDataEntry dataEntry : analysisRun.analysisDataList) {
                for (ExperimentAnalysis.AnalysisEntry analysisEntry : dataEntry.analysisList) {
                    try {
                        IDataAnalysis analysis = analysisEntry.analysis;
                        File storageDir = ExperimentAnalysis.getAnalysisStorageFor(experimentData,
                                analysisRuns.indexOf(analysisRun), analysis);
                        storageDir.mkdirs();
                        ExperimentHelper.saveAnalysisData(analysis, storageDir);
                        exportTagMarkerCSVData(analysis, storageDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private File getTagMarkerCSVFile(IDataAnalysis analysis, File storageDir) {
        ISensorData sensorData = analysis.getData();
        return new File(storageDir, sensorData.getUid() + "_tag_markers.csv");
    }

    private void exportTagMarkerCSVData(IDataAnalysis sensorAnalysis, File storageDir) throws IOException {
        File csvFile = getTagMarkerCSVFile(sensorAnalysis, storageDir);
        if (!csvFile.exists()) {
            try {
                if (!csvFile.createNewFile())
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile)));
            sensorAnalysis.exportTagMarkerCSVData(writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int sensor) {
            int run = experimentAnalysis.getCurrentAnalysisRunIndex();
            int analysisIndex = 0;
            ExperimentAnalysis.AnalysisEntry analysisEntry
                    = experimentAnalysis.getCurrentAnalysisRun().analysisDataList.get(sensor).analysisList.get(analysisIndex);
            ExperimentAnalysis.AnalysisRef analysisRef
                    = new ExperimentAnalysis.AnalysisRef(run, sensor, analysisEntry.analysis.getIdentifier());
            return analysisEntry.plugin.createSensorAnalysisFragment(analysisRef);
        }

        @Override
        public int getCount() {
            if (experimentAnalysis == null)
                return 0;
            ExperimentAnalysis.AnalysisRunEntry currentAnalysisRun = experimentAnalysis.getCurrentAnalysisRun();
            if (currentAnalysisRun == null)
                return 0;
            return currentAnalysisRun.analysisDataList.size();
        }
    }
}


