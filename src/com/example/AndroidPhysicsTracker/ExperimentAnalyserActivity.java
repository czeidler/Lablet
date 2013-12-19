package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;

import static com.example.AndroidPhysicsTracker.ExperimentRunViewControl.*;


/**
 * Created by lec on 11/12/13.
 */
public class ExperimentAnalyserActivity extends Activity {
    private RelativeLayout experimentRunLayout = null;
    private View experimentRunView = null;
    private MarkerView markerView = null;
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

        experimentRunLayout = (RelativeLayout)findViewById(R.id.experimentRunLayout);

        experimentRunView = plugin.createExperimentRunView(this, experiment);
        markerView = new MarkerView(this, experimentRunView);

        RelativeLayout.LayoutParams runViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        experimentRunLayout.addView(experimentRunView, runViewParams);

        RelativeLayout.LayoutParams makerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        makerViewParams.addRule(RelativeLayout.ALIGN_LEFT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_TOP, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_RIGHT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, experimentRunView.getId());

        experimentRunLayout.addView(markerView, makerViewParams);


        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)findViewById(
                R.id.experimentRunViewControl);
        runViewControl.setTo(experiment.getNumberOfRuns());

        runViewControl.setOnRunChangedListener(new ExperimentRunViewControl.RunChangedListener() {
            @Override
            public void onRunChanged(int run) {
                ((IExperimentRunView)experimentRunView).setCurrentRun(run);
                markerView.setCurrentRun(run);
            }
        });
    }

    private void showErrorAndFinish(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        builder.setNeutralButton("Ok", null);
        builder.create().show();
        finish();
    }
}