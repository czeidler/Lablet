/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.PopupMenu;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.ExperimentAnalysisFragment;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.ExperimentPluginHelper;
import nz.ac.auckland.lablet.experiment.IAnalysisPlugin;
import nz.ac.auckland.lablet.views.ScaleSettingsDialog;


public class MotionAnalysisFragment extends ExperimentAnalysisFragment {
    static final int PERFORM_RUN_SETTINGS = 0;

    private boolean resumeWithRunSettings = false;
    private boolean resumeWithRunSettingsHelp = false;

    public MotionAnalysisFragment(ExperimentAnalysisActivity.AnalysisRef ref) {
        super(ref);
    }

    private MotionAnalysis getSensorAnalysis() {
        return (MotionAnalysis)sensorAnalysis;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (sensorAnalysis == null)
            return;

        menu.clear();
        inflater.inflate(R.menu.experiment_analyser_activity_actions, menu);

        final MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
                return true;
            }
        });
        final MenuItem settingsItem = menu.findItem(R.id.action_run_settings);
        assert settingsItem != null;
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startRunSettingsActivity(null);
                return true;
            }
        });

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
    }

    private void showCalibrationMenu() {
        ScaleSettingsDialog scaleSettingsDialog = new ScaleSettingsDialog(getActivity(),
                getSensorAnalysis().getLengthCalibrationSetter());
        scaleSettingsDialog.show();
    }

    private void showOriginPopup() {
        final View menuView = getActivity().findViewById(R.id.action_origin_settings);
        final PopupMenu popup = new PopupMenu(getActivity(), menuView);
        popup.inflate(R.menu.origin_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int item = menuItem.getItemId();
                if (item == R.id.showCoordinateSystem) {
                    getSensorAnalysis().setShowCoordinateSystem(!menuItem.isChecked());
                } else if (item == R.id.swapAxis) {
                    getSensorAnalysis().getCalibrationXY().setSwapAxis(!menuItem.isChecked());
                }
                return false;
            }
        });
        popup.getMenu().getItem(0).setChecked(getSensorAnalysis().getShowCoordinateSystem());
        popup.getMenu().getItem(1).setChecked(getSensorAnalysis().getCalibrationXY().getSwapAxis());
        popup.show();
    }

    /**
     * Starts an activity to config the experiment analysis.
     * <p>
     * For example, the camera experiment uses it to set the framerate and the video start and end point.
     * </p>
     * <p>
     * Important: the analysisSpecificData and the options bundles have to be put as extras into the intent:
     * <ul>
     * <li>bundle field "analysisSpecificData" -> analysisSpecificData</li>
     * <li>bundle field "options" -> options</li>
     * </ul>
     * </p>
     * <p>
     * The following options can be put into the option bundle:
     * <ul>
     * <li>boolean field "start_with_help", to start with help screen</li>
     * </ul>
     * </p>
     * <p>
     * The Activity should return an Intent containing the following fields:
     * <ul>
     * <li>bundle field "run_settings", the updated run settings</li>
     * <li>boolean field "run_settings_changed", if the run settings have been changed</li>
     * </ul>
     * </p>
     *
     * @param options bundle with options for the run settings activity
     */
    private void startRunSettingsActivity(Bundle options) {
        String experimentPath = getExperimentData().getStorageDir().getParentFile().getPath();

        Intent intent = new Intent(getActivity(), CameraRunSettingsActivity.class);
        ExperimentPluginHelper.packStartAnalysisSettingsIntent(intent, analysisRef, experimentPath, options);
        startActivityForResult(intent, PERFORM_RUN_SETTINGS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, requestCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_RUN_SETTINGS) {
            MotionAnalysis sensorAnalysis = getSensorAnalysis();

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

        setHasOptionsMenu(true);

        final Intent intent = getActivity().getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.getBoolean("first_start_with_run_settings", false)
                        && getSensorAnalysis().getTagMarkers().getMarkerCount() == 0) {
                    resumeWithRunSettings = true;
                }
                if (extras.getBoolean("first_start_with_run_settings_help", false)) {
                    resumeWithRunSettings = true;
                    resumeWithRunSettingsHelp = true;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new MotionAnalysisFragmentView(getActivity(), getSensorAnalysis());
    }

    @Override
    public void onResume() {
        super.onResume();

        MotionAnalysis sensorAnalysis = getSensorAnalysis();
        if (getSensorAnalysis() == null)
            return;
        sensorAnalysis.getFrameDataModel().setCurrentFrame(sensorAnalysis.getFrameDataModel().getCurrentFrame());

        if (resumeWithRunSettings) {
            Bundle options = null;
            if (resumeWithRunSettingsHelp) {
                options = new Bundle();
                options.putBoolean("start_with_help", true);
            }
            startRunSettingsActivity(options);
            resumeWithRunSettings = false;
        }
    }
}
