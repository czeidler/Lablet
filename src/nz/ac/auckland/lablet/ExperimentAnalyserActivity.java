/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.PopupMenu;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.views.ScaleSettingsDialog;

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
public class ExperimentAnalyserActivity extends ExperimentDataActivity {
    static final int PERFORM_RUN_SETTINGS = 0;

    final public static int MARKER_COLOR = Color.argb(255, 100, 200, 20);

    private boolean resumeWithRunSettings = false;
    private boolean resumeWithRunSettingsHelp = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getExperimentData() == null)
            return false;

        menu.clear();
        getMenuInflater().inflate(R.menu.experiment_analyser_activity_actions, menu);

        final MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_OK);
                finish();
                return true;
            }
        });
        final MenuItem settingsItem = menu.findItem(R.id.action_run_settings);
        assert settingsItem != null;
        final StringBuilder settingsName = new StringBuilder();
        if (currentAnalysisSensor.plugin.hasSensorSettingsActivity(settingsName)) {
            settingsItem.setTitle(settingsName);
            settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    startRunSettingsActivity(currentAnalysisSensor.analysis.getExperimentSpecificData(), null);
                    return true;
                }
            });
        } else {
            settingsItem.setVisible(false);
        }

        final MenuItem calibrationMenu = menu.findItem(R.id.action_calibration_settings);
        assert calibrationMenu != null;
        calibrationMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCalibrationMenu();
                return true;
            }
        });

        final MenuItem originMenu = menu.findItem(R.id.action_origin_settings);
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void showCalibrationMenu() {
        ScaleSettingsDialog scaleSettingsDialog = new ScaleSettingsDialog(this, currentAnalysisSensor.analysis);
        scaleSettingsDialog.show();
    }

    private void showOriginPopup() {
        final View menuView = findViewById(R.id.action_origin_settings);
        final PopupMenu popup = new PopupMenu(this, menuView);
        popup.inflate(R.menu.origin_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int item = menuItem.getItemId();
                if (item == R.id.showCoordinateSystem) {
                    currentAnalysisSensor.analysis.setShowCoordinateSystem(!menuItem.isChecked());
                } else if (item == R.id.swapAxis) {
                    currentAnalysisSensor.analysis.getCalibration().setSwapAxis(!menuItem.isChecked());
                }
                return false;
            }
        });
        popup.getMenu().getItem(0).setChecked(currentAnalysisSensor.analysis.getShowCoordinateSystem());
        popup.getMenu().getItem(1).setChecked(currentAnalysisSensor.analysis.getCalibration().getSwapAxis());
        popup.show();
    }

    private ExperimentData.SensorDataRef getSensorDataRef(AnalysisEntry analysisEntry) {
        for (int run = 0; run < analysisRuns.size(); run++) {
            List<AnalysisEntry> analysisList = analysisRuns.get(run);
            for (int sensor = 0; sensor < analysisList.size(); sensor++) {
                AnalysisEntry entry = analysisList.get(sensor);
                if (entry == analysisEntry)
                    return new ExperimentData.SensorDataRef(experimentData, run, sensor);
            }
        }
        return null;
    }


    private void startRunSettingsActivity(Bundle analysisSpecificData, Bundle options) {
        IExperimentPlugin plugin = currentAnalysisSensor.plugin;
        ExperimentData.SensorDataRef sensorDataRef = getSensorDataRef(currentAnalysisSensor);
        plugin.startSensorSettingsActivity(this, PERFORM_RUN_SETTINGS, sensorDataRef, analysisSpecificData, options);
    }

    /*
    private void mailData() {
        // first save data again
        exportTagMarkerCSVData();
        final File tagMarkerCSVFile = getTagMarkerCSVFile();

        // start mail intent
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Experiment Data");
        intent.putExtra(Intent.EXTRA_TEXT, "Attached is your experiment data.");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tagMarkerCSVFile));

        startActivity(Intent.createChooser(intent, "Send Email"));
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == PERFORM_RUN_SETTINGS) {
            SensorAnalysis sensorAnalysis = currentAnalysisSensor.analysis;

            Bundle extras = data.getExtras();
            if (extras != null) {
                Bundle settings = extras.getBundle("run_settings");
                if (settings != null) {
                    Bundle specificData = sensorAnalysis.getExperimentSpecificData();
                    if (specificData == null)
                        specificData = new Bundle();
                    specificData.putBundle("run_settings", settings);
                    sensorAnalysis.setExperimentSpecificData(specificData);
                }
                boolean settingsChanged = extras.getBoolean("run_settings_changed", false);
                if (settingsChanged) {
                    sensorAnalysis.getTagMarkers().clear();
                    sensorAnalysis.getFrameDataModel().setCurrentFrame(0);
                }
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

        final Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.getBoolean("first_start_with_run_settings", false)
                        && currentAnalysisSensor.analysis.getTagMarkers().getMarkerCount() == 0) {
                    resumeWithRunSettings = true;
                }
                if (extras.getBoolean("first_start_with_run_settings_help", false)) {
                    resumeWithRunSettings = true;
                    resumeWithRunSettingsHelp = true;
                }
            }
        }

        // gui stuff:

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        final ViewPager pager = (ViewPager)findViewById(R.id.pager);
        final ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(0);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentAnalysisSensor == null)
            return;
        SensorAnalysis sensorAnalysis = currentAnalysisSensor.analysis;
        if (sensorAnalysis != null)
            sensorAnalysis.getFrameDataModel().setCurrentFrame(sensorAnalysis.getFrameDataModel().getCurrentFrame());

        if (resumeWithRunSettings) {
            Bundle options = null;
            if (resumeWithRunSettingsHelp) {
                options = new Bundle();
                options.putBoolean("start_with_help", true);
            }
            startRunSettingsActivity(sensorAnalysis.getExperimentSpecificData(), options);
            resumeWithRunSettings = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (analysisRuns.size() == 0)
            return;

        for (List<AnalysisEntry> entries : analysisRuns) {
            for (AnalysisEntry entry : entries) {
                try {
                    entry.analysis.saveAnalysisDataToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        exportTagMarkerCSVData();
    }

    private File getTagMarkerCSVFile(SensorAnalysis analysis) {
        SensorData sensorData = analysis.getSensorData();
        return new File(sensorData.getStorageDir(), sensorData.getUid() + "_tag_markers.csv");
    }

    private void exportTagMarkerCSVData() {
        for (List<AnalysisEntry> entries : analysisRuns) {
            for (AnalysisEntry entry : entries) {
                exportTagMarkerCSVData(entry.analysis);
            }
        }
    }

    private void exportTagMarkerCSVData(SensorAnalysis sensorAnalysis) {
        File csvFile = getTagMarkerCSVFile(sensorAnalysis);
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
            return new AnalysisMixedDataFragment(position);
        }

        @Override
        public int getCount() {
            return currentAnalysisRun.size();
        }
    }
}


