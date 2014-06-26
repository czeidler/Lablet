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
import nz.ac.auckland.lablet.accelerometer.AccelerometerExperimentRun;
import nz.ac.auckland.lablet.camera.CameraExperimentRun;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


class ExperimentRunViewManager {
    final private Experiment experiment;

    private ImageButton addRunGroupButton = null;
    private Button nextRunGroupButton = null;
    private Button prevRunGroupButton = null;
    private TextView runGroupView = null;

    private Experiment.IExperimentListener experimentListener = new Experiment.IExperimentListener() {
        @Override
        public void onExperimentRunGroupAdded(ExperimentRunGroup runGroup) {
            updateViews();
        }

        @Override
        public void onExperimentRunGroupRemoved(ExperimentRunGroup runGroup) {
            updateViews();
        }

        @Override
        public void onCurrentRunGroupChanged(ExperimentRunGroup newGroup, ExperimentRunGroup oldGroup) {
            updateViews();
        }
    };

    public ExperimentRunViewManager(final Activity activity, final Experiment experiment) {
        this.experiment = experiment;

        addRunGroupButton = (ImageButton)activity.findViewById(R.id.addRunButton);
        nextRunGroupButton = (Button)activity.findViewById(R.id.nextButton);
        prevRunGroupButton = (Button)activity.findViewById(R.id.prevButton);
        runGroupView = (TextView)activity.findViewById(R.id.runGroupView);

        addRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExperimentRunGroup oldGroup = experiment.getCurrentExperimentRunGroup();
                List<String> experimentNamesList = new ArrayList<String>();
                for (IExperimentRun experimentRun : oldGroup.getExperimentRuns())
                    experimentNamesList.add(experimentRun.getClass().getSimpleName());
                ExperimentRunGroup experimentRunGroup = ExperimentRunGroup.createExperimentRunGroup(
                        experimentNamesList, activity);

                experiment.addExperimentRunGroup(experimentRunGroup);
                setCurrentExperimentRunGroup(experimentRunGroup);
            }
        });

        nextRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ExperimentRunGroup> runGroups = experiment.getExperimentRunGroups();
                int index = runGroups.indexOf(experiment.getCurrentExperimentRunGroup());
                index++;
                if (index >= runGroups.size())
                    return;
                setCurrentExperimentRunGroup(runGroups.get(index));
            }
        });

        prevRunGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ExperimentRunGroup> runGroups = experiment.getExperimentRunGroups();
                int index = runGroups.indexOf(experiment.getCurrentExperimentRunGroup());
                index--;
                if (index < 0)
                    return;
                setCurrentExperimentRunGroup(runGroups.get(index));
            }
        });

        experiment.addListener(experimentListener);

        updateViews();
    }

    private void updateViews() {
        ExperimentRunGroup currentRunGroup = experiment.getCurrentExperimentRunGroup();
        List<ExperimentRunGroup> runGroups = experiment.getExperimentRunGroups();
        int index = runGroups.indexOf(currentRunGroup);

        runGroupView.setText(Integer.toString(index));

        nextRunGroupButton.setEnabled(index + 1 < runGroups.size());
        prevRunGroupButton.setEnabled(index - 1 >= 0);
    }

    private void setCurrentExperimentRunGroup(ExperimentRunGroup experimentRunGroup) {
        experiment.setCurrentExperimentRunGroup(experimentRunGroup);
    }
}


public class ExperimentActivity extends FragmentActivity {
    private Experiment experiment;

    private File experimentBaseDir;

    private ViewPager pager = null;
    private ImageButton startButton = null;
    private ImageButton stopButton = null;
    private ImageButton newButton = null;
    private MenuItem analyseMenuItem = null;
    private MenuItem settingsMenu = null;
    private MenuItem viewMenu = null;
    private MenuItem sensorMenu = null;

    private AbstractViewState state = null;

    private ExperimentRunViewManager experimentRunViewManager;
    private ExperimentRunGroup activeExperimentRunGroup = null;

    private Experiment.IExperimentListener experimentListener = new Experiment.IExperimentListener() {
        @Override
        public void onExperimentRunGroupAdded(ExperimentRunGroup runGroup) {
        }

        @Override
        public void onExperimentRunGroupRemoved(ExperimentRunGroup runGroup) {
        }

        @Override
        public void onCurrentRunGroupChanged(ExperimentRunGroup newGroup, ExperimentRunGroup oldGroup) {
            activateExperimentRunGroup(newGroup, true);

            updateAdapter();
            setState(new PreviewState());
        }
    };

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
            boolean hasOptions = currentExperimentRun.onPrepareOptionsMenu(settingsMenu);
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
        if (!experiment.getCurrentExperimentRunGroup().dataTaken()) {
            setState(new PreviewState());
        } else {
            // we have unsaved experiment data means we are in the PlaybackState state
            setState(new PlaybackState());
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public List<IExperimentRun> getActiveExperimentRuns() {
        ExperimentRunGroup currentGroup = experiment.getCurrentExperimentRunGroup();
        if (currentGroup.isActive())
            return currentGroup.getExperimentRuns();
        return new ArrayList<>();
    }

    private void showViewMenu(MenuItem menuItem) {
        View menuView = findViewById(R.id.action_view);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

        final List<IExperimentRun> experimentRuns = getActiveExperimentRuns();
        for (int i = 0; i < experimentRuns.size(); i++) {
            IExperimentRun experiment = experimentRuns.get(i);

            MenuItem item = popup.getMenu().add(1, i, i, experiment.getClass().getSimpleName());
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(1, true, true);
        IExperimentRun currentExperimentRun = experiment.getCurrentExperimentRun();
        if (currentExperimentRun != null)
            popup.getMenu().getItem(experimentRuns.indexOf(currentExperimentRun)).setChecked(true);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemPosition = menuItem.getItemId();
                IExperimentRun experimentRun = experimentRuns.get(itemPosition);
                setCurrentExperimentRun(experimentRun);
                pager.requestLayout();
                pager.setCurrentItem(itemPosition, true);
                return true;
            }
        });

        popup.show();
    }

    private IExperimentRun getExperiment(IExperimentPlugin plugin) {
        for (IExperimentRun experiment : getActiveExperimentRuns()) {
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
                IExperimentRun experimentRun = getExperiment(plugin);
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
        IExperimentRun experimentRun = plugin.createExperiment(this);
        experiment.getCurrentExperimentRunGroup().addExperimentRun(experimentRun);

        updateAdapter();
    }

    private void removeExperimentRun(IExperimentRun experimentRun) {
        experiment.getCurrentExperimentRunGroup().removeExperimentRun(experimentRun);

        updateAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        experimentBaseDir = new File(getExternalFilesDir(null), "experiments");

        experiment = new Experiment(this, experimentBaseDir);

        final List<String> experimentList = new ArrayList<>();
        experimentList.add(AccelerometerExperimentRun.class.getSimpleName());
        experimentList.add(CameraExperimentRun.class.getSimpleName());

        ExperimentRunGroup runGroup = ExperimentRunGroup.createExperimentRunGroup(experimentList, this);
        experiment.addExperimentRunGroup(runGroup);
        experiment.setCurrentExperimentRunGroup(runGroup);
        setCurrentExperimentRun(runGroup.getExperimentRunAt(0));

        experiment.addListener(experimentListener);

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
                setCurrentExperimentRun(experiment.getCurrentExperimentRunGroup().getExperimentRunAt(position));
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

        experimentRunViewManager = new ExperimentRunViewManager(this, experiment);
    }

    private void updateAdapter() {
        ExperimentRunFragmentPagerAdapter pagerAdapter = new ExperimentRunFragmentPagerAdapter(
                getSupportFragmentManager());
        pagerAdapter.setExperimentRunGroup(experiment.getCurrentExperimentRunGroup());
        pager.setAdapter(pagerAdapter);
    }

    private void activateExperimentRunGroup(ExperimentRunGroup experimentRunGroup, boolean activate) {
        if (activeExperimentRunGroup != experimentRunGroup && activeExperimentRunGroup != null)
            activeExperimentRunGroup.activateExperimentRuns(null);

        if (activate) {
            experimentRunGroup.activateExperimentRuns(this);
            activeExperimentRunGroup = experimentRunGroup;
        } else {
            experimentRunGroup.activateExperimentRuns(null);
            activeExperimentRunGroup = null;
        }
    }

    private void setCurrentExperimentRun(IExperimentRun experimentRun) {
        experimentRun.getExperimentRunGroup().setCurrentExperimentRun(experimentRun);
        invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        List<IExperimentRun> experimentRuns = getActiveExperimentRuns();
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

        List<IExperimentRun> experimentRuns = getActiveExperimentRuns();
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

        activateExperimentRunGroup(experiment.getCurrentExperimentRunGroup(), true);
        updateAdapter();
    }

    @Override
    public void onPause() {
        setState(null);

        ExperimentRunGroup currentGroup = experiment.getCurrentExperimentRunGroup();
        activateExperimentRunGroup(currentGroup, false);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        final List<IExperimentRun> experimentRuns = getActiveExperimentRuns();
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
                    try {
                        experiment.finishExperiment(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setResult(RESULT_OK);
                    finish();
                }
            });
            builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        experiment.finishExperiment(false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

            builder.create().show();
        } else {
            try {
                experiment.finishExperiment(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void startRecording() {
        try {
            for (IExperimentRun experiment : getActiveExperimentRuns())
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
        for (IExperimentRun experiment : getActiveExperimentRuns()) {
            if (!experiment.stopRecording())
                dataTaken = false;
        }
        return dataTaken;
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
            settingsMenu.setVisible(true);

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            for (IExperimentRun experiment : getActiveExperimentRuns())
                experiment.startPreview();
        }

        public void leaveState() {
            settingsMenu.setVisible(false);

            for (IExperimentRun experiment : getActiveExperimentRuns())
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

            for (IExperimentRun experiment : getActiveExperimentRuns())
                experiment.startPlayback();

            analyseMenuItem.setEnabled(true);
        }

        public void leaveState() {
            for (IExperimentRun experiment : getActiveExperimentRuns())
                experiment.stopPlayback();
        }

        @Override
        public void onNewClicked() {
            setState(new PreviewState());
        }
    }
}

class ExperimentRunFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private ExperimentRunGroup experimentRunGroup;

    public ExperimentRunFragmentPagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void setExperimentRunGroup(ExperimentRunGroup experimentRunGroup) {
        this.experimentRunGroup = experimentRunGroup;
        notifyDataSetChanged();
    }

    class ExperimentRunFragment extends android.support.v4.app.Fragment {
        private IExperimentRun experimentRun;

        public ExperimentRunFragment(String experimentRunName) {
            super();

            Bundle args = new Bundle();
            args.putString("experiment_name", experimentRunName);
            setArguments(args);
        }

        public ExperimentRunFragment() {
            super();
        }

        public IExperimentRun findExperimentFromArguments(Activity activity) {
            String name = getArguments().getString("experiment_name", "");
            ExperimentActivity experimentActivity = (ExperimentActivity)activity;
            List<IExperimentRun> list = experimentActivity.getActiveExperimentRuns();
            for (IExperimentRun experimentRun : list) {
                if (experimentRun.getClass().getSimpleName().equals(name))
                    return experimentRun;
            }
            return null;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return experimentRun.createExperimentView(getActivity());
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);

            experimentRun = findExperimentFromArguments(activity);
        }

        public IExperimentRun getExperimentRun() {
            return experimentRun;
        }
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {


        List<IExperimentRun> list = experimentRunGroup.getExperimentRuns();
        return new ExperimentRunFragment(list.get(position).getClass().getSimpleName());
    }

    @Override
    public int getCount() {
        List<IExperimentRun> list = experimentRunGroup.getExperimentRuns();
        return list.size();
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}