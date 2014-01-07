package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


public class ExperimentAnalyserActivity extends Activity {
    private View experimentRunView = null;
    private ExperimentPlugin plugin = null;
    private Experiment experiment = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String experimentPath = "";
        File storageDir = null;
        Intent intent = getIntent();
        Bundle bundle = null;
        if (intent != null) {
            experimentPath = intent.getStringExtra("experiment_path");
            if (experimentPath != null) {
                storageDir = new File(experimentPath);
                File file = new File(storageDir, "experiment.xml");

                InputStream inStream = null;
                try {
                    inStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showErrorAndFinish("experiment file not found");
                }

                PersistentBundle persistentBundle = new PersistentBundle();
                try {
                    bundle = persistentBundle.unflattenBundle(inStream);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    showErrorAndFinish("can't read experiment file");
                }
                String experimentIdentifier = bundle.getString("experiment_identifier");

                ExperimentPluginFactory factory = ExperimentPluginFactory.getFactory();
                plugin = factory.findExperimentPlugin(experimentIdentifier);
            }
        }
        if (plugin == null)
            showErrorAndFinish("unknown experiment type");

        assert bundle != null;
        Bundle experimentData = bundle.getBundle("data");
        if (experimentData == null)
            showErrorAndFinish("failed to load experiment data");
        experiment = plugin.loadExperiment(this, experimentData, storageDir);

        setupViews();
    }

    private void setupViews() {
        // setup views
        setContentView(R.layout.experimentanalyser);

        experimentRunView = plugin.createExperimentRunView(this, experiment);

        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)findViewById(
                R.id.experimentRunViewControl);
        runViewControl.setTo(experiment.getNumberOfRuns());

        RunContainerView runContainerView = (RunContainerView)findViewById(R.id.experimentRunContainer);
        runContainerView.setRunView(experimentRunView, experiment);
        runContainerView.setExperimentRunViewControl(runViewControl);

        // marker table view
        TableView tableView = (TableView)findViewById(R.id.tagMarkerTableView);
        tableView.setAdapter(new MarkerDataTableAdapter(experiment.getTagMarkers()));

        // marker graph view
        GraphView2D graphView = (GraphView2D)findViewById(R.id.tagMarkerGraphView);
        graphView.setAdapter(new MarkerGraphAdapter(experiment.getTagMarkers()));
    }

    private void showErrorAndFinish(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        builder.setNeutralButton("Ok", null);
        builder.create().show();
        finish();
    }
}