package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;

import java.io.*;


public class ExperimentAnalyserActivity extends ExperimentActivity {
    static final int PERFORM_RUN_SETTINGS = 0;
    final String EXPERIMENT_ANALYSIS_FILE_NAME = "experiment_analysis.xml";

    ExperimentAnalysis experimentAnalysis;

    public ExperimentAnalysis getExperimentAnalysis() {
        return experimentAnalysis;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getExperiment() == null)
            return false;

        getMenuInflater().inflate(R.menu.experiment_analyser_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        });
        MenuItem settingsItem = menu.findItem(R.id.action_run_settings);
        StringBuilder settingsName = new StringBuilder();
        if (plugin.hasRunEditActivity(settingsName)) {
            settingsItem.setTitle(settingsName);
            settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    startRunSettingsActivity(experimentAnalysis.getExperimentSpecificData());
                    return true;
                }
            });
        } else {
            settingsItem.setVisible(false);
        }

        MenuItem calibrationMenu = menu.findItem(R.id.action_calibration_settings);
        calibrationMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startCalibrationMenu();
                return true;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void startCalibrationMenu() {
        CalibrationView calibrationView = new CalibrationView(this, experimentAnalysis.getLengthCalibrationSetter());
        calibrationView.show();
    }

    private void startRunSettingsActivity(Bundle analysisSpecificData) {
        plugin.startRunSettingsActivity(experiment, analysisSpecificData, this, PERFORM_RUN_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_RUN_SETTINGS) {
            Bundle runSettings = data.getExtras();
            if (runSettings != null) {
                Bundle specificData = experimentAnalysis.getExperimentSpecificData();
                if (specificData == null)
                    specificData = new Bundle();
                specificData.putBundle("run_settings", runSettings);
                experimentAnalysis.setExperimentSpecificData(specificData);
            }
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!loadExperiment(getIntent()))
            return;

        experimentAnalysis = plugin.loadExperimentAnalysis(experiment);
        loadAnalysisDataToFile();

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        experimentAnalysis.getRunDataModel().setCurrentRun(experimentAnalysis.getRunDataModel().getCurrentRun());
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            saveAnalysisDataToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File csvFile = new File(experiment.getStorageDir(), "tag_markers.csv");
        exportTagMarkerCSVData(csvFile);
    }

    protected boolean loadAnalysisDataToFile() {
        File projectFile = new File(experiment.getStorageDir(), EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = loadBundleFromFile(projectFile);
        if (bundle == null)
            return false;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return false;

        return experimentAnalysis.loadAnalysisData(analysisDataBundle, experiment.getStorageDir());
    }

    protected void saveAnalysisDataToFile() throws IOException {
        Bundle bundle = new Bundle();
        Bundle experimentData = experimentAnalysis.analysisDataToBundle();
        bundle.putBundle("analysis_data", experimentData);

        // save the bundle
        File projectFile = new File(getStorageDir(), EXPERIMENT_ANALYSIS_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    protected void exportTagMarkerCSVData(File output) {
        output.setWritable(true);
        FileOutputStream outputStream = null;
        if (!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            outputStream = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        MarkersDataModel tagMarkers = experimentAnalysis.getTagMarkers();
        try {
            outputStream.write("id, x, y, runValue\n".getBytes());
            for (int i = 0; i < tagMarkers.getMarkerCount(); i++) {
                MarkerData markerData = tagMarkers.getMarkerDataAt(i);
                String string = "";
                string += markerData.getRunId();
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                PointF position = tagMarkers.getCalibratedMarkerPositionAt(i);
                string = "";
                string += position.x;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += position.y;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += experiment.getRunValueAt(i);
                outputStream.write(string.getBytes());

                outputStream.write("\n".getBytes());
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0)
                return new AnalysisRunViewFragment();
            else if (position == 1)
                return new AnalysisMixedDataFragment();
            else if (position == 2)
                return new AnalysisTableGraphDataFragment();
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}


