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
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


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
public class ExperimentAnalyserActivity extends ExperimentDataActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!loadExperiment(getIntent())) {
            showErrorAndFinish("Unable to load the experiment.");
            return;
        }

        // gui stuff:

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        final ViewPager pager = (ViewPager)findViewById(R.id.pager);
        final ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(0);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentSensorAnalysis(position, 0);

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

        if (analysisRuns.size() == 0)
            return;

        for (AnalysisRunEntry analysisRun : analysisRuns) {
            for (AnalysisSensorEntry sensorEntry : analysisRun.sensorList) {
                for (AnalysisEntry analysisEntry : sensorEntry.analysisList) {
                    try {
                        ISensorAnalysis analysis = analysisEntry.analysis;
                        File storageDir = getAnalysisStorageFor(analysisRuns.indexOf(analysisRun), analysis);
                        analysis.saveAnalysisData(storageDir);
                        exportTagMarkerCSVData(analysis, storageDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private File getTagMarkerCSVFile(ISensorAnalysis analysis, File storageDir) {
        SensorData sensorData = analysis.getData();
        return new File(storageDir, sensorData.getUid() + "_tag_markers.csv");
    }

    private void exportTagMarkerCSVData(ISensorAnalysis sensorAnalysis, File storageDir) throws IOException {
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

        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        sensorAnalysis.exportTagMarkerCSVData(outputStream);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            AnalysisEntry analysisEntry = currentAnalysisRun.sensorList.get(position).analysisList.get(0);
            return analysisEntry.plugin.createSensorAnalysisFragment(analysisEntry.analysis);
        }

        @Override
        public int getCount() {
            return currentAnalysisRun.sensorList.size();
        }
    }
}


