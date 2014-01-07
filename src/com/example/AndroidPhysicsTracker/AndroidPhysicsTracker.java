package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class AndroidPhysicsTracker extends Activity {
    private List<ExperimentPlugin> experimentPluginList = null;
    private List<String> experimentList = null;
    private ListView experimentListView = null;
    private ExperimentDirObserver experimentDirObserver = null;

    static final int PERFORM_EXPERIMENT = 0;
    static final int ANALYSE_EXPERIMENT = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // plugin list
        experimentPluginList = ExperimentPluginFactory.getFactory().getPluginList();

        int grey = 70;
        int listBackgroundColor = Color.rgb(grey, grey, grey);

        ListView newExperimentList = (ListView)findViewById(R.id.newExperiments);
        newExperimentList.setBackgroundColor(listBackgroundColor);
        newExperimentList.setAdapter(new ArrayAdapter<ExperimentPlugin>(this,
                android.R.layout.simple_list_item_1, experimentPluginList));

        newExperimentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ExperimentPlugin plugin = experimentPluginList.get(i);
                startExperiment(plugin);
            }
        });

        // experiment list
        experimentListView = (ListView)findViewById(R.id.existingExperimentListView);
        experimentListView.setBackgroundColor(listBackgroundColor);
        experimentList = new ArrayList<String>();

        experimentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = experimentList.get(i);
                startAnalyzeActivityById(id);
            }
        });

        File experimentDir = Experiment.getMainExperimentDir(this);
        if (experimentDir.exists()) {
            // TODO: Events are never received, check why. Manually call updateExperimentList in onResume for now.
            experimentDirObserver = new ExperimentDirObserver(experimentDir.getPath());
            experimentDirObserver.startWatching();
        }
    }

    private void startAnalyzeActivityById(String id) {
        File experimentDir = Experiment.getMainExperimentDir(this);
        File experimentPath = new File(experimentDir, id);
        startAnalyzeActivity(experimentPath.getPath());
    }

    private void startAnalyzeActivity(String experimentPath) {
        Intent intent = new Intent(this, ExperimentAnalyserActivity.class);
        intent.putExtra("experiment_path", experimentPath);
        startActivityForResult(intent, ANALYSE_EXPERIMENT);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateExperimentList();
    }

    @Override
    public void onDestroy() {
        if (experimentDirObserver != null)
            experimentDirObserver.stopWatching();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            String experimentPath = data.getStringExtra("experiment_path");
            startAnalyzeActivity(experimentPath);
            return;
        }

        if (requestCode == ANALYSE_EXPERIMENT) {

        }
    }

    private void startExperiment(ExperimentPlugin plugin) {
        plugin.startExperimentActivity(this, PERFORM_EXPERIMENT);
    }

    private void updateExperimentList() {
        experimentList.clear();
        File experimentDir = Experiment.getMainExperimentDir(this);
        if (experimentDir.isDirectory()) {
            File[] children = experimentDir.listFiles();
            for (File child : children != null ? children : new File[0])
                experimentList.add(child.getName());
        }

        experimentListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, experimentList));
    }

    class ExperimentDirObserver extends FileObserver {
        public ExperimentDirObserver(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            switch (event) {
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                case FileObserver.MOVED_TO:
                    updateExperimentList();
                    break;
            }
        }
    }
}
