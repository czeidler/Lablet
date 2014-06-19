/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import nz.ac.auckland.lablet.accelerometer.AccelerometerExperimentRun;
import nz.ac.auckland.lablet.camera.CameraExperimentRun;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExperimentActivity extends Activity {
    final private Map<IExperimentRun, View> experimentViews = new HashMap<>();

    private Experiment experiment;

    private File experimentBaseDir;

    private FrameLayout centerView = null;
    private ImageButton startButton = null;
    private ImageButton stopButton = null;
    private ImageButton newButton = null;
    private MenuItem analyseMenuItem = null;
    private MenuItem settingsMenu = null;
    private MenuItem viewMenu = null;
    private MenuItem sensorMenu = null;

    private AbstractViewState state = null;

    private boolean unsavedExperimentData = false;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.perform_experiment_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onBackPressed();
                return false;
            }
        });
        analyseMenuItem = menu.findItem(R.id.action_analyse);
        assert analyseMenuItem != null;
        analyseMenuItem.setEnabled(false);
        analyseMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finishExperiment(true);
                return true;
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            Bundle options = intent.getExtras();
            if (options != null) {
                boolean showAnalyseMenu = options.getBoolean("show_analyse_menu", true);
                analyseMenuItem.setVisible(showAnalyseMenu);
            }
        }

        settingsMenu = menu.findItem(R.id.action_settings);
        assert settingsMenu != null;
        IExperimentRun currentExperimentRun = experiment.getCurrentExperimentRun();
        if (currentExperimentRun != null) {
            boolean hasOptions = currentExperimentRun.onPrepareOptionsMenu(settingsMenu,
                    new IExperimentRun.IExperimentParent() {
                private AbstractViewState previousState;

                @Override
                public void startEditingSettings() {
                    previousState = state;
                    setState(null);
                }

                @Override
                public void finishEditingSettings() {
                    setState(previousState);
                }
            });
            settingsMenu.setEnabled(hasOptions);
            settingsMenu.setVisible(hasOptions);
        }

        viewMenu = menu.findItem(R.id.action_view);
        assert viewMenu != null;
        viewMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showViewMenu(menuItem);
                return true;
            }
        });

        sensorMenu = menu.findItem(R.id.action_sensors);
        assert sensorMenu != null;
        sensorMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showSensorMenu(menuItem);
                return true;
            }
        });

        // set states after menu has been init
        if (!unsavedExperimentData) {
            setState(new PreviewState());
        } else {
            // we have unsaved experiment data means we are in the PlaybackState state
            setState(new PlaybackState());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private List<IExperimentRun> getCurrentExperimentRuns() {
        return experiment.getCurrentExperimentRunGroup().getExperimentRuns();
    }

    private void showViewMenu(MenuItem menuItem) {
        View menuView = findViewById(R.id.action_view);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

        final List<IExperimentRun> experimentRuns = getCurrentExperimentRuns();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentRun experiment = experimentRuns.get(i);

            MenuItem item = popup.getMenu().add(1, i, i, experiment.getClass().getSimpleName());
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(1, true, true);
        popup.getMenu().getItem(experimentRuns.indexOf(experiment.getCurrentExperimentRun())).setChecked(true);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setCurrentExperimentRun(experimentRuns.get(menuItem.getItemId()));
                return true;
            }
        });

        popup.show();
    }

    private IExperimentRun getExperiment(IExperimentPlugin plugin) {
        for (IExperimentRun experiment : getCurrentExperimentRuns()) {
            if (experiment.getClass().getSimpleName().equals(plugin.getName()))
                return experiment;
        }
        return null;
    }

    private void showSensorMenu(MenuItem menuItem) {
        View menuView = findViewById(R.id.action_view);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

        final List<IExperimentPlugin> plugins = ExperimentPluginFactory.getFactory().getPluginList();
        for (int i = 0; i < plugins.size(); i++) {
            IExperimentPlugin plugin = plugins.get(i);

            MenuItem item = popup.getMenu().add(1, i, i, plugin.getName());
            item.setCheckable(true);

            if (getExperiment(plugin) != null)
                item.setChecked(true);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                IExperimentPlugin plugin = plugins.get(menuItem.getItemId());
                IExperimentRun experiment = getExperiment(plugin);
                if (experiment != null)
                    removeExperiment(experiment);
                else
                    addExperimentRun(plugin, experimentBaseDir, true);
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.experiment_recording);

        centerView = (FrameLayout)findViewById(R.id.centerLayout);

        startButton = (ImageButton)findViewById(R.id.recordButton);
        stopButton = (ImageButton)findViewById(R.id.stopButton);
        newButton = (ImageButton)findViewById(R.id.newButton);

        setState(null);

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onRecordClicked();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onStopClicked();
            }
        });

        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onNewClicked();
            }
        });

        experimentBaseDir = new File(getExternalFilesDir(null), "experiments");

        experiment = new Experiment(this, experimentBaseDir);
        ExperimentRunGroup runGroup = new ExperimentRunGroup(experiment, new File(experimentBaseDir,
                "run" + Integer.toString(experiment.createRunGroupId())));
        experiment.addRun(runGroup);
        experiment.setCurrentExperimentRunGroup(runGroup);

        IExperimentPlugin plugin = ExperimentPluginFactory.getFactory().findExperimentPlugin(AccelerometerExperimentRun.class.getSimpleName());
        addExperimentRun(plugin, experimentBaseDir, false);

        plugin = ExperimentPluginFactory.getFactory().findExperimentPlugin(CameraExperimentRun.class.getSimpleName());
        addExperimentRun(plugin, experimentBaseDir, false);
    }

    private void addExperimentRun(IExperimentPlugin plugin, File experimentBaseDir, boolean activityIsRunning) {
        IExperimentRun experiment = plugin.createExperiment(this, experimentBaseDir);
        addExperimentRun(experiment, activityIsRunning);
    }

    private void addExperimentRun(IExperimentRun experimentRun, boolean activityIsRunning) {
        getCurrentExperimentRuns().add(experimentRun);
        centerView.addView(getExperimentView(experimentRun));
        if (activityIsRunning)
            experimentRun.init(this, experimentBaseDir);
        setCurrentExperimentRun(experimentRun);
    }

    private void removeExperiment(IExperimentRun experimentRun) {
        setState(null);

        List<IExperimentRun> experimentRuns = getCurrentExperimentRuns();
        experimentRuns.remove(experimentRun);
        centerView.removeView(getExperimentView(experimentRun));
        experimentViews.remove(experimentRun);

        try {
            experimentRun.finish(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        experimentRun.destroy();

        if (experiment.getCurrentExperimentRun() == experimentRun)
            setCurrentExperimentRun(experimentRuns.get(0));

        setState(new PreviewState());
    }

    private void setCurrentExperimentRun(IExperimentRun experimentRun) {
        IExperimentRun currentExperimentRun = experiment.getCurrentExperimentRun();
        if (currentExperimentRun != null)
            getExperimentView(currentExperimentRun).setVisibility(View.INVISIBLE);
        currentExperimentRun = experimentRun;
        invalidateOptionsMenu();
        View view = getExperimentView(currentExperimentRun);
        view.setVisibility(View.VISIBLE);

        centerView.requestLayout();
    }

    private View getExperimentView(IExperimentRun experiment) {
        if (experimentViews.containsKey(experiment))
            return experimentViews.get(experiment);

        View view = experiment.createExperimentView(this);
        experimentViews.put(experiment, view);
        return view;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<IExperimentRun> experimentRuns = getCurrentExperimentRuns();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentRun experiment = experimentRuns.get(i);

            String experimentId = "";
            experimentId += i;
            Bundle experimentState = new Bundle();
            experiment.onSaveInstanceState(experimentState);

            outState.putBundle(experimentId, experimentState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        List<IExperimentRun> experimentRuns = getCurrentExperimentRuns();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentRun experiment = experimentRuns.get(i);

            String experimentId = "";
            experimentId += i;
            Bundle experimentState = savedInstanceState.getBundle(experimentId);
            experiment.onRestoreInstanceState(experimentState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        for (IExperimentRun experiment : getCurrentExperimentRuns())
            experiment.init(this, experimentBaseDir);
    }

    @Override
    public void onPause() {
        setState(null);

        for (IExperimentRun experiment : getCurrentExperimentRuns())
            experiment.destroy();

        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final List<IExperimentRun> experimentRuns = getCurrentExperimentRuns();
        if (unsavedExperimentData) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Experiment is not saved");
            builder.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishExperiment(false);
                }
            });
            builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    for (IExperimentRun experiment : experimentRuns)
                        try {
                            experiment.finish(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    unsavedExperimentData = false;
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

            builder.create().show();
        } else {
            for (IExperimentRun experiment : experimentRuns)
                try {
                    experiment.finish(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void startRecording() {
        try {
            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.startRecording();

        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Unable to start recording!");
            builder.setNeutralButton("Ok", null);
            builder.create().show();
            setState(null);
            // in case some experiment are running stop them
            stopRecording();
            return;
        }
    }

    private boolean stopRecording() {
        boolean dataTaken = true;
        for (IExperimentRun experiment : getCurrentExperimentRuns()) {
            if (!experiment.stopRecording())
                dataTaken = false;
        }
        return dataTaken;
    }

    private void finishExperiment(boolean startAnalysis) {
        unsavedExperimentData = false;

        try {
            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.finish(false);

            Intent data = new Intent();
            //TODO: FIX
            //File outputDir = experimentData.getStorageDir();
            //data.putExtra("experiment_path", outputDir.getPath());
            data.putExtra("start_analysis", startAnalysis);
            setResult(RESULT_OK, data);
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    abstract class AbstractViewState {
        abstract public void enterState();
        abstract public void leaveState();
        public void onRecordClicked() {}
        public void onStopClicked() {}
        public void onNewClicked() {}
    }

    private void setState(AbstractViewState newState) {
        if (state != null)
            state.leaveState();
        state = newState;
        if (state == null) {
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);
        } else
            state.enterState();
    }

    class PreviewState extends AbstractViewState {
        public void enterState() {
            settingsMenu.setVisible(true);

            unsavedExperimentData = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.startPreview();
        }

        public void leaveState() {
            settingsMenu.setVisible(false);

            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.stopPreview();
        }

        @Override
        public void onRecordClicked() {
            setState(new RecordState());
        }
    }

    /**
     * Lock the screen to the current orientation.
     * @return the previous orientation settings
     */
    private int lockScreenOrientation() {
        int initialRequestedOrientation = getRequestedOrientation();

        // Note: a surface rotation of 90 degrees means a physical device rotation of -90 degrees.
        int orientation = getResources().getConfiguration().orientation;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_90:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_180:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Surface.ROTATION_270:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
        return initialRequestedOrientation;
    }

    class RecordState extends AbstractViewState {
        private boolean isRecording = false;
        private int initialRequestedOrientation;
        public void enterState() {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            // disable screen rotation during recording
            initialRequestedOrientation = lockScreenOrientation();

            startRecording();
            isRecording = true;

            // don't fall asleep!
            centerView.setKeepScreenOn(true);
        }

        public void leaveState() {
            if (isRecording) {
                stopRecording();
                isRecording = false;
            }
            unsavedExperimentData = true;

            setRequestedOrientation(initialRequestedOrientation);

            // sleep if tired
            centerView.setKeepScreenOn(false);
        }

        @Override
        public void onStopClicked() {
            boolean dataTaken = stopRecording();
            isRecording = false;
            if (dataTaken)
                setState(new PlaybackState());
            else
                setState(new PreviewState());
        }
    }

    class PlaybackState extends AbstractViewState {
        public void enterState() {
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.VISIBLE);

            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.startPlayback();

            analyseMenuItem.setEnabled(true);
        }

        public void leaveState() {
            for (IExperimentRun experiment : getCurrentExperimentRuns())
                experiment.stopPlayback();
        }

        @Override
        public void onNewClicked() {
            setState(new PreviewState());
        }
    }
}
