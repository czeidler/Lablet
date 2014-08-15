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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


class ExperimentRunViewManager {
    private int maxNumberOfRuns = -1;
    final private Experiment experiment;

    private ImageButton addRunGroupButton = null;
    private Button nextRunGroupButton = null;
    private Button prevRunGroupButton = null;
    private TextView runGroupView = null;

    private Experiment.IExperimentListener experimentListener = new Experiment.IExperimentListener() {
        @Override
        public void onExperimentRunGroupAdded(ExperimentRun runGroup) {
            updateViews();
        }

        @Override
        public void onExperimentRunGroupRemoved(ExperimentRun runGroup) {
            updateViews();
        }

        @Override
        public void onCurrentRunGroupChanged(ExperimentRun newGroup, ExperimentRun oldGroup) {
            updateViews();
        }
    };

    public ExperimentRunViewManager(final Activity activity, final int maxNumberOfRuns, final Experiment experiment) {
        this.maxNumberOfRuns = maxNumberOfRuns;
        this.experiment = experiment;

        addRunGroupButton = (ImageButton)activity.findViewById(R.id.addRunButton);
        nextRunGroupButton = (Button)activity.findViewById(R.id.nextButton);
        prevRunGroupButton = (Button)activity.findViewById(R.id.prevButton);
        runGroupView = (TextView)activity.findViewById(R.id.runGroupView);

        addRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperimentRun oldGroup = experiment.getCurrentExperimentRun();
                List<String> experimentNamesList = new ArrayList<>();
                for (IExperimentSensor experimentRun : oldGroup.getExperimentSensors())
                    experimentNamesList.add(experimentRun.getClass().getSimpleName());
                ExperimentRun experimentRun = ExperimentRun.createExperimentRunGroup(
                        experimentNamesList, activity);

                experiment.addExperimentRunGroup(experimentRun);
                setCurrentExperimentRunGroup(experimentRun);

                if (maxNumberOfRuns > 0) {
                    // check if we have the maximal number of runs
                    List<ExperimentRun> runGroups = experiment.getExperimentRuns();
                    if (runGroups.size() >= maxNumberOfRuns)
                        addRunGroupButton.setEnabled(false);
                }
            }
        });

        nextRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ExperimentRun> runGroups = experiment.getExperimentRuns();
                int index = runGroups.indexOf(experiment.getCurrentExperimentRun());
                index++;
                if (index >= runGroups.size())
                    return;
                setCurrentExperimentRunGroup(runGroups.get(index));
            }
        });

        prevRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ExperimentRun> runGroups = experiment.getExperimentRuns();
                int index = runGroups.indexOf(experiment.getCurrentExperimentRun());
                index--;
                if (index < 0)
                    return;
                setCurrentExperimentRunGroup(runGroups.get(index));
            }
        });

        experiment.addListener(experimentListener);

        if (maxNumberOfRuns == 1)
            setVisibility(View.INVISIBLE);

        updateViews();
    }

    private void setVisibility(int visibility) {
        addRunGroupButton.setVisibility(visibility);
        nextRunGroupButton.setVisibility(visibility);
        prevRunGroupButton.setVisibility(visibility);
        runGroupView.setVisibility(visibility);
    }

    private void updateViews() {
        ExperimentRun currentRunGroup = experiment.getCurrentExperimentRun();
        List<ExperimentRun> runGroups = experiment.getExperimentRuns();
        int index = runGroups.indexOf(currentRunGroup);

        runGroupView.setText(Integer.toString(index));

        nextRunGroupButton.setEnabled(index + 1 < runGroups.size());
        prevRunGroupButton.setEnabled(index - 1 >= 0);
    }

    private void setCurrentExperimentRunGroup(ExperimentRun experimentRun) {
        experiment.setCurrentExperimentRun(experimentRun);
    }
}


/**
 * Manage a menu item that may not exist yet.
 *
 * The option menu and its items are created after onResume. However, we like to configure the items there.
 */
class MenuItemProxy {
    private MenuItem item = null;
    private boolean visible = true;
    private boolean enabled = true;

    public void setMenuItem(MenuItem item) {
        this.item = item;
        if (item != null)
            update();
    }

    public MenuItem getMenuItem() {
        return item;
    }

    private void update() {
        item.setVisible(visible);
        item.setEnabled(enabled);
    }

    public void setVisible(boolean visible) {
        if (item != null)
            item.setVisible(visible);
        this.visible = visible;
    }

    public void setEnabled(boolean enabled) {
        if (item != null)
            item.setEnabled(enabled);
        this.enabled = enabled;
    }

    public boolean getVisible() {
        return visible;
    }
}


public class ExperimentActivity extends FragmentActivity {
    private Experiment experiment;

    private File experimentBaseDir;

    private ViewPager pager = null;
    private ImageButton startButton = null;
    private ImageButton stopButton = null;
    private ImageButton newButton = null;
    final private MenuItemProxy analyseMenuItem = new MenuItemProxy();
    final private MenuItemProxy settingsMenuItem = new MenuItemProxy();
    final private MenuItemProxy sensorMenuItem = new MenuItemProxy();

    private MenuItem viewMenu = null;

    private AbstractViewState state = null;

    private ExperimentRunViewManager experimentRunViewManager;
    private ExperimentRun activeExperimentRun = null;

    private Experiment.IExperimentListener experimentListener = new Experiment.IExperimentListener() {
        @Override
        public void onExperimentRunGroupAdded(ExperimentRun runGroup) {
        }

        @Override
        public void onExperimentRunGroupRemoved(ExperimentRun runGroup) {
        }

        @Override
        public void onCurrentRunGroupChanged(ExperimentRun newGroup, ExperimentRun oldGroup) {
            activateExperimentRun(newGroup, true);

            updateAdapter();
            setState(new PreviewState());
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.perform_experiment_activity_actions, menu);

        // back item
        MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onBackPressed();
                return false;
            }
        });

        // analyse item
        MenuItem analyseItem = menu.findItem(R.id.action_analyse);
        assert analyseItem != null;
        analyseMenuItem.setMenuItem(analyseItem);
        analyseItem.setEnabled(false);
        analyseItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finishExperiment(true);
                return true;
            }
        });

        // settings item
        MenuItem settingsMenu = menu.findItem(R.id.action_settings);
        assert settingsMenu != null;
        settingsMenuItem.setMenuItem(settingsMenu);

        // sensor view item
        viewMenu = menu.findItem(R.id.action_view);
        assert viewMenu != null;
        viewMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showViewMenu(menuItem);
                return true;
            }
        });

        // activate sensors item
        MenuItem sensorMenu = menu.findItem(R.id.action_sensors);
        assert sensorMenu != null;
        sensorMenuItem.setMenuItem(sensorMenu);
        sensorMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showSensorMenu(menuItem);
                return true;
            }
        });

        // config the menu
        Bundle options = AbstractExperimentPlugin.unpackStartExperimentOptions(getIntent());
        if (options != null) {
            boolean showAnalyseMenu = options.getBoolean("show_analyse_menu", true);
            analyseMenuItem.setVisible(showAnalyseMenu);

            boolean sensorsEditable = options.getBoolean("sensors_editable", true);
            sensorMenu.setVisible(sensorsEditable);
            if (!sensorsEditable) {
                if (getExperiment().getCurrentExperimentRun().getExperimentSensors().size() == 1)
                    viewMenu.setVisible(false);
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        IExperimentSensor currentExperimentSensor = experiment.getCurrentExperimentSensor();
        if (currentExperimentSensor != null) {
            boolean hasOptions = currentExperimentSensor.onPrepareOptionsMenu(settingsMenuItem.getMenuItem());
            settingsMenuItem.setVisible(hasOptions);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public List<IExperimentSensor> getActiveSensors() {
        ExperimentRun currentRun = getExperiment().getCurrentExperimentRun();
        if (currentRun.isActive())
            return currentRun.getExperimentSensors();
        return new ArrayList<>();
    }

    private void showViewMenu(MenuItem menuItem) {
        View menuView = findViewById(R.id.action_view);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

        final List<IExperimentSensor> experimentRuns = getActiveSensors();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentSensor experiment = experimentRuns.get(i);

            MenuItem item = popup.getMenu().add(1, i, i, experiment.getClass().getSimpleName());
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(1, true, true);
        IExperimentSensor currentExperimentSensor = experiment.getCurrentExperimentSensor();
        if (currentExperimentSensor != null)
            popup.getMenu().getItem(experimentRuns.indexOf(currentExperimentSensor)).setChecked(true);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemPosition = menuItem.getItemId();
                IExperimentSensor experimentRun = experimentRuns.get(itemPosition);
                setCurrentSensor(experimentRun);
                pager.requestLayout();
                pager.setCurrentItem(itemPosition, true);
                return true;
            }
        });

        popup.show();
    }

    private IExperimentSensor getExperiment(IExperimentPlugin plugin) {
        for (IExperimentSensor experiment : getActiveSensors()) {
            if (experiment.getPlugin() == plugin)
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
                IExperimentSensor experimentRun = getExperiment(plugin);
                if (experimentRun != null)
                    removeExperimentRun(experimentRun);
                else
                    addExperiment(plugin);
                return true;
            }
        });

        popup.show();
    }

    private void addExperiment(IExperimentPlugin plugin) {
        IExperimentSensor experimentRun = plugin.createExperimentSensor(this);
        experiment.getCurrentExperimentRun().addExperimentSensor(experimentRun);

        experimentRun.startPreview();

        updateAdapter();
    }

    private void removeExperimentRun(IExperimentSensor experimentRun) {
        experiment.getCurrentExperimentRun().removeExperimentSensor(experimentRun);
        experimentRun.stopPreview();

        updateAdapter();
    }

    /**
     * Gets/loads the Experiment.
     *
     * This must be called from the fragment views to obtain the experiment.
     *
     * @return
     */
    public Experiment getExperiment() {
        if (experiment != null)
            return experiment;

        final Intent intent = getIntent();
        final List<String> experimentList = new ArrayList<>();
        final Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("experiment_base_directory"))
                experimentBaseDir = new File(extras.getString("experiment_base_directory"));
        }
        String[] pluginNames = AbstractExperimentPlugin.unpackStartExperimentPlugins(intent);
        if (pluginNames != null) {
            for (String pluginName : pluginNames) {
                IExperimentPlugin plugin = ExperimentPluginFactory.getFactory().findExperimentPlugin(pluginName);
                experimentList.add(plugin.getName());
            }
        }

        if (experimentBaseDir == null)
            experimentBaseDir = new File(getExternalFilesDir(null), "experiments");

        if (experimentList.size() == 0)
            return null;

        experiment = new Experiment(this, experimentBaseDir);

        final ExperimentRun experimentRun = ExperimentRun.createExperimentRunGroup(experimentList, this);
        experiment.addExperimentRunGroup(experimentRun);
        experiment.setCurrentExperimentRun(experimentRun);
        activateExperimentRun(experiment.getCurrentExperimentRun(), true);
        setCurrentSensor(experimentRun.getExperimentRunAt(0));

        experiment.addListener(experimentListener);
        return experiment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make sure the experiment gets loaded (but it will be loaded beforehand from the fragments in onCreateView)
        if (getExperiment() == null) {
            finish();
            return;
        }

        // gui
        setContentView(R.layout.experiment_recording);

        pager = (ViewPager)findViewById(R.id.centerLayout);
        updateAdapter();
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentSensor(experiment.getCurrentExperimentRun().getExperimentRunAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

        int maxNumberOfRuns = -1;
        Bundle options = AbstractExperimentPlugin.unpackStartExperimentOptions(getIntent());
        if (options != null) {
            maxNumberOfRuns = options.getInt("max_number_of_runs", -1);
        }

        experimentRunViewManager = new ExperimentRunViewManager(this, maxNumberOfRuns, experiment);
    }

    private void updateAdapter() {
        ExperimentRunFragmentPagerAdapter pagerAdapter = new ExperimentRunFragmentPagerAdapter(
                getSupportFragmentManager());
        pagerAdapter.setExperimentRun(experiment.getCurrentExperimentRun());
        pager.setAdapter(pagerAdapter);
    }

    private void activateExperimentRun(ExperimentRun experimentRun, boolean activate) {
        if (activeExperimentRun != experimentRun && activeExperimentRun != null)
            activeExperimentRun.activateSensors(null);

        if (activate) {
            experimentRun.activateSensors(this);
            activeExperimentRun = experimentRun;
        } else {
            experimentRun.activateSensors(null);
            activeExperimentRun = null;
        }
    }

    private void setCurrentSensor(IExperimentSensor sensor) {
        sensor.getExperimentRun().setCurrentExperimentRun(sensor);
        invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<IExperimentSensor> activeSensors = getActiveSensors();
        for (int i = 0; i < activeSensors.size(); i++) {
            IExperimentSensor sensor = activeSensors.get(i);

            Bundle experimentState = new Bundle();
            sensor.onSaveInstanceState(experimentState);

            String experimentId = Integer.toString(i);
            outState.putBundle(experimentId, experimentState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        List<IExperimentSensor> experimentRuns = getActiveSensors();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentSensor experiment = experimentRuns.get(i);

            String experimentId = Integer.toString(i);
            Bundle experimentState = savedInstanceState.getBundle(experimentId);
            experiment.onRestoreInstanceState(experimentState);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        activateExperimentRun(experiment.getCurrentExperimentRun(), true);
    }

    @Override
    public void onStop() {
        super.onStop();

        activateExperimentRun(experiment.getCurrentExperimentRun(), false);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateAdapter();

        if (!experiment.getCurrentExperimentRun().dataTaken()) {
            setState(new PreviewState());
        } else {
            // we have unsaved experiment data means we are in the PlaybackState state
            setState(new PlaybackState());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        setState(null);
    }

    @Override
    public void onBackPressed() {
        final List<IExperimentSensor> experimentRuns = getActiveSensors();
        if (experiment.dataTaken()) {
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
                    finishDiscardExperiment();
                }
            });

            builder.create().show();
        } else {
            finishDiscardExperiment();
        }
    }

    private void startRecording() {
        try {
            for (IExperimentSensor experiment : getActiveSensors())
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
        for (IExperimentSensor experiment : getActiveSensors()) {
            if (!experiment.stopRecording())
                dataTaken = false;
        }
        return dataTaken;
    }

    private void finishDiscardExperiment() {
        try {
            experiment.finishExperiment(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setResult(RESULT_CANCELED);
        finish();
    }

    private void finishExperiment(boolean startAnalysis) {
        try {
            experiment.finishExperiment(true);

            Intent data = new Intent();
            File outputDir = experiment.getStorageDir();
            data.putExtra("experiment_path", outputDir.getPath());
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
            settingsMenuItem.setEnabled(true);
            sensorMenuItem.setEnabled(true);

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            for (IExperimentSensor experiment : getActiveSensors())
                experiment.startPreview();

            invalidateOptionsMenu();
        }

        public void leaveState() {
            settingsMenuItem.setEnabled(false);
            sensorMenuItem.setEnabled(false);

            for (IExperimentSensor experiment : getActiveSensors())
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
            pager.setKeepScreenOn(true);
        }

        public void leaveState() {
            if (isRecording) {
                stopRecording();
                isRecording = false;
            }

            setRequestedOrientation(initialRequestedOrientation);

            // sleep if tired
            pager.setKeepScreenOn(false);
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

            for (IExperimentSensor experiment : getActiveSensors())
                experiment.startPlayback();

            analyseMenuItem.setEnabled(true);
        }

        public void leaveState() {
            for (IExperimentSensor experiment : getActiveSensors())
                experiment.stopPlayback();
        }

        @Override
        public void onNewClicked() {
            setState(new PreviewState());
        }
    }
}

class ExperimentRunFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private ExperimentRun experimentRun;

    public ExperimentRunFragmentPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void setExperimentRun(ExperimentRun experimentRun) {
        this.experimentRun = experimentRun;
        notifyDataSetChanged();
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        List<IExperimentSensor> list = experimentRun.getExperimentSensors();
        return new ExperimentRunFragment(list.get(position).getClass().getSimpleName());
    }

    @Override
    public int getCount() {
        List<IExperimentSensor> list = experimentRun.getExperimentSensors();
        return list.size();
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}