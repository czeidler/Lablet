/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.PopupMenu;

import java.io.*;


public class ExperimentAnalyserActivity extends ExperimentActivity {
    static final int PERFORM_RUN_SETTINGS = 0;
    final String EXPERIMENT_ANALYSIS_FILE_NAME = "experiment_analysis.xml";

    final public static int MARKER_COLOR = Color.argb(255, 100, 200, 20);

    ExperimentAnalysis experimentAnalysis;

    public ExperimentAnalysis getExperimentAnalysis() {
        return experimentAnalysis;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getExperiment() == null)
            return false;

        menu.clear();
        getMenuInflater().inflate(R.menu.experiment_analyser_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        });
        MenuItem settingsItem = menu.findItem(R.id.action_run_settings);
        assert settingsItem != null;
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
        assert calibrationMenu != null;
        calibrationMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCalibrationMenu();
                return true;
            }
        });

        MenuItem originMenu = menu.findItem(R.id.action_origin_settings);
        assert originMenu != null;
        originMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showOriginPopup();
                return true;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private void showCalibrationMenu() {
        CalibrationView calibrationView = new CalibrationView(this, experimentAnalysis);
        calibrationView.show();
    }

    private void showOriginPopup() {
        View menuView = findViewById(R.id.action_origin_settings);
        PopupMenu popup = new PopupMenu(this, menuView);
        popup.inflate(R.menu.origin_popup);
        popup.setOnMenuItemClickListener(new OriginMenuClickListener());
        popup.getMenu().getItem(0).setChecked(experimentAnalysis.getShowCoordinateSystem());
        popup.getMenu().getItem(1).setChecked(experimentAnalysis.getCalibration().getSwapAxis());
        popup.show();
    }

    private class OriginMenuClickListener implements PopupMenu.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            int item = menuItem.getItemId();
            if (item == R.id.showCoordinateSystem) {
                experimentAnalysis.setShowCoordinateSystem(!menuItem.isChecked());
            } else if (item == R.id.swapAxis) {
                experimentAnalysis.getCalibration().setSwapAxis(!menuItem.isChecked());
            }
            return false;
        }
    }

    private void startRunSettingsActivity(Bundle analysisSpecificData) {
        plugin.startRunSettingsActivity(experiment, analysisSpecificData, this, PERFORM_RUN_SETTINGS);
    }

    private void mailData() {
        // first save data again
        exportTagMarkerCSVData();
        File tagMarkerCSVFile = getTagMarkerCSVFile();

        // start mail intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Experiment Data");
        intent.putExtra(Intent.EXTRA_TEXT, "Attached is your experiment data.");
        intent .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tagMarkerCSVFile));

        startActivity(Intent.createChooser(intent, "Send Email"));
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
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!loadExperiment(getIntent())) {
            showErrorAndFinish("Unable to load the experiment.");
            return;
        }

        experimentAnalysis = plugin.loadExperimentAnalysis(experiment);
        loadAnalysisDataToFile();

        // gui stuff:

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(0);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (experimentAnalysis != null)
            experimentAnalysis.getRunDataModel().setCurrentRun(experimentAnalysis.getRunDataModel().getCurrentRun());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (experimentAnalysis == null)
            return;

        try {
            saveAnalysisDataToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        exportTagMarkerCSVData();
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

    private File getTagMarkerCSVFile() {
        return new File(experiment.getStorageDir(), experiment.getUid() + "_tag_markers.csv");
    }

    private void exportTagMarkerCSVData() {
        File csvFile = getTagMarkerCSVFile();

        if (!csvFile.setWritable(true))
            return;

        FileOutputStream outputStream;
        if (!csvFile.exists()) {
            try {
                if (!csvFile.createNewFile())
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            outputStream = new FileOutputStream(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        experimentAnalysis.exportTagMarkerCSVData(outputStream);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0)
                return new AnalysisMixedDataFragment();
            else if (position == 1)
                return new AnalysisTableGraphDataFragment();
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}


