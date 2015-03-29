/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;
import nz.ac.auckland.lablet.ExperimentActivity;
import nz.ac.auckland.lablet.ExperimentAnalysisActivity;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.script.Script;
import nz.ac.auckland.lablet.script.ScriptComponent;
import nz.ac.auckland.lablet.script.ScriptTreeNodeFragmentHolder;

import java.io.File;


/**
 * Script component that can create a fragment for the experiment analysis.
 */
class ScriptTreeNodeAccelerometerAnalysis extends ScriptTreeNodeFragmentHolder {
    private ScriptExperimentRef experiment;
    private String descriptionText = "";

    public ScriptTreeNodeAccelerometerAnalysis(Script script) {
        super(script);
    }

    @Override
    public boolean initCheck() {
        if (experiment == null) {
            lastErrorMessage = "no experiment given";
            return false;
        }
        return true;
    }

    @Override
    public ScriptComponentGenericFragment createFragment() {
        ScriptComponentGenericFragment fragment = new AccelerometerAnalysisFragment();
        fragment.setScriptComponent(this);
        return fragment;
    }

    public void setExperiment(ScriptExperimentRef experiment) {
        this.experiment = experiment;
    }

    public ScriptExperimentRef getExperiment() {
        return experiment;
    }

    public void setDescriptionText(String text) {
        descriptionText = text;
    }

    public String getDescriptionText() {
        return descriptionText;
    }
}


/**
 * Fragment to start an experiment analysis.
 */
public class AccelerometerAnalysisFragment extends ScriptComponentGenericFragment {
    static final int ANALYSE_EXPERIMENT = 0;

    private CheckedTextView takenExperimentInfo = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (component == null)
            return view;

        View child = setChild(R.layout.script_component_accelerometer_analysis);
        assert child != null;

        ScriptTreeNodeAccelerometerAnalysis analysisComponent = (ScriptTreeNodeAccelerometerAnalysis)this.component;

        TextView descriptionTextView = (TextView)child.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        if (!analysisComponent.getDescriptionText().equals(""))
            descriptionTextView.setText(analysisComponent.getDescriptionText());

        Button takeExperiment = (Button)child.findViewById(R.id.analyzeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ExperimentAnalysisActivity.class);
                intent.putExtra(ExperimentActivity.PATH,
                        ((ScriptTreeNodeAccelerometerAnalysis)component).getExperiment().getExperimentPath());
                intent.putExtra("first_start_with_run_settings", true);
                intent.putExtra("first_start_with_run_settings_help", true);
                startActivityForResult(intent, ANALYSE_EXPERIMENT);
            }
        });

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        File experimentPathFile = new File(analysisComponent.getExperiment().getExperimentPath());
        takenExperimentInfo.setText(experimentPathFile.getName());

        updateViews();

        return view;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        setState(ScriptComponent.SCRIPT_STATE_DONE);
    }

    @Override
    protected void setState(int state) {
        super.setState(state);
        updateViews();
    }

    private void updateViews() {
        // is view already init?
        if (takenExperimentInfo == null)
            return;

        if (component.getState() == ScriptComponent.SCRIPT_STATE_DONE) {
            takenExperimentInfo.setChecked(true);
        } else {
            takenExperimentInfo.setChecked(false);
        }
    }
}
