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
import nz.ac.auckland.lablet.ExperimentAnalysisFragment;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.views.ScaleSettingsDialog;


public class MotionAnalysisFragment extends ExperimentAnalysisFragment {
    static final int PERFORM_RUN_SETTINGS = 0;

    private boolean resumeWithRunSettings = false;
    private boolean resumeWithRunSettingsHelp = false;

    public MotionAnalysisFragment() {
        super();
    }

    private MotionAnalysis getSensorAnalysis() {
        return (MotionAnalysis)sensorAnalysis;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (sensorAnalysis == null)
            return;

        menu.clear();
        inflater.inflate(R.menu.motion_analysis_actions, menu);

        final MenuItem deleteItem = menu.findItem(R.id.action_delete);
        assert deleteItem != null;
        final MarkerDataModel markerDataModel = getSensorAnalysis().getTagMarkers();
        final FrameDataModel frameDataModel = getSensorAnalysis().getFrameDataModel();
        if (markerDataModel.getMarkerCount() <= 1)
            deleteItem.setVisible(false);
        else
            deleteItem.setVisible(true);
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int selectedIndex = markerDataModel.getSelectedMarkerData();
                if (selectedIndex < 0 || markerDataModel.getMarkerCount() == 1) {
                    getActivity().invalidateOptionsMenu();
                    return true;
                }

                markerDataModel.removeMarkerData(selectedIndex);

                int newSelectedIndex;
                if (selectedIndex < markerDataModel.getMarkerCount())
                    newSelectedIndex = selectedIndex;
                else
                    newSelectedIndex = selectedIndex - 1;
                frameDataModel.setCurrentFrame(markerDataModel.getMarkerDataAt(newSelectedIndex).getId());

                if (markerDataModel.getMarkerCount() <= 1)
                    getActivity().invalidateOptionsMenu();
                return true;
            }
        });

        final MenuItem viewItem = menu.findItem(R.id.action_view);
        assert viewItem != null;
        viewItem.setIcon(view.getSideBarStatusIcon());
        viewItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                view.onToggleSidebar();
                viewItem.setIcon(view.getSideBarStatusIcon());
                return true;
            }
        });

        final MenuItem settingsItem = menu.findItem(R.id.action_video_settings);
        assert settingsItem != null;
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startRunSettingsActivity(null);
                return true;
            }
        });

        final MenuItem calibrationMenu = menu.findItem(R.id.length_scale);
        assert calibrationMenu != null;
        calibrationMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showCalibrationPopup();
                return true;
            }
        });

        setupStandardMenu(menu, inflater);
    }

    private void showLengthScaleDialog() {
        MotionAnalysis analysis = getSensorAnalysis();
        ScaleSettingsDialog scaleSettingsDialog = new ScaleSettingsDialog(getActivity(),
                analysis.getLengthCalibrationSetter(), analysis.getXUnit(), analysis.getYUnit());
        scaleSettingsDialog.show();
    }

    private void showCalibrationPopup() {
        final View menuView = getActivity().findViewById(R.id.length_scale);
        final PopupMenu popup = new PopupMenu(getActivity(), menuView);
        popup.inflate(R.menu.calibration_popup);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int item = menuItem.getItemId();
                if (item == R.id.length_scale) {
                    showLengthScaleDialog();
                } else if (item == R.id.showCoordinateSystem) {
                    getSensorAnalysis().setShowCoordinateSystem(!menuItem.isChecked());
                } else if (item == R.id.swapAxis) {
                    getSensorAnalysis().getCalibrationXY().setSwapAxis(!menuItem.isChecked());
                }
                return false;
            }
        });
        LengthCalibrationSetter lengthCalibrationSetter = getSensorAnalysis().getLengthCalibrationSetter();
        MenuItem lengthItem = popup.getMenu().findItem(R.id.length_scale);
        String lengthTitle = lengthItem.getTitle() + " (" + lengthCalibrationSetter.getCalibrationValue() + " "
                + getSensorAnalysis().getXUnit().getUnit() + ")";
        popup.getMenu().findItem(R.id.length_scale).setTitle(lengthTitle);
        popup.getMenu().findItem(R.id.showCoordinateSystem).setChecked(getSensorAnalysis().getShowCoordinateSystem());
        popup.getMenu().findItem(R.id.swapAxis).setChecked(getSensorAnalysis().getCalibrationXY().getSwapAxis());
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

        Intent intent = new Intent(getActivity(), MotionAnalysisSettingsActivity.class);
        ExperimentHelper.packStartAnalysisSettingsIntent(intent, analysisRef, experimentPath, options);
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
                if (settings != null)
                    sensorAnalysis.setVideoAnalysisSettings(settings);
                boolean settingsChanged = extras.getBoolean("run_settings_changed", false);
                if (settingsChanged) {
                    sensorAnalysis.getTagMarkers().clear();
                    sensorAnalysis.getFrameDataModel().setCurrentFrame(0);
                }
            }
        }
    }

    private MarkerDataModel.IListener menuDataListener = new MarkerDataModel.IListener() {
        @Override
        public void onDataAdded(MarkerDataModel model, int index) {
            if (model.getMarkerCount() > 1)
                getActivity().invalidateOptionsMenu();
        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {

        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        getSensorAnalysis().getTagMarkers().addListener(menuDataListener);
    }

    private MotionAnalysisFragmentView view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = new MotionAnalysisFragmentView(getActivity(), getSensorAnalysis());
        return view;
    }

    @Override
    public void onDestroyView() {
        view.release();
        view = null;
        super.onDestroyView();
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
