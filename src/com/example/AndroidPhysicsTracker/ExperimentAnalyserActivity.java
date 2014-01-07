package com.example.AndroidPhysicsTracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


public class ExperimentAnalyserActivity extends FragmentActivity {
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

        setContentView(R.layout.experimentanalyser);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        ScreenSlidePagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        //setupViews();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0)
                return new AnalysisRunViewFragment(plugin, experiment);
            else if (position == 1)
                return new AnalysisMixedDataFragment(plugin, experiment);
            else if (position == 2)
                return new AnalysisTableGraphDataFragment(plugin, experiment);
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private void setupViews() {
        // setup views
        setContentView(R.layout.experimentanalyser);

        View experimentRunView = plugin.createExperimentRunView(this, experiment);

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


