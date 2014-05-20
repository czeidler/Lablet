/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.script.components;

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
import nz.ac.aucklanduni.physics.lablet.ExperimentAnalyserActivity;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.R;
import nz.ac.aucklanduni.physics.lablet.script.Script;
import nz.ac.aucklanduni.physics.lablet.script.ScriptComponent;
import nz.ac.aucklanduni.physics.lablet.script.ScriptComponentTreeFragmentHolder;
import nz.ac.aucklanduni.physics.lablet.views.graph.GraphView2D;
import nz.ac.aucklanduni.physics.lablet.views.graph.MarkerGraphAdapter;
import nz.ac.aucklanduni.physics.lablet.views.graph.XPositionMarkerGraphAxis;
import nz.ac.aucklanduni.physics.lablet.views.graph.YPositionMarkerGraphAxis;

import java.io.File;


/**
 * Script component that can create a fragment for the experiment analysis.
 */
class ScriptComponentTreeExperimentAnalysis extends ScriptComponentTreeFragmentHolder {
    private ScriptComponentExperiment experiment;
    private String descriptionText = "";

    public ScriptComponentTreeExperimentAnalysis(Script script) {
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
        ScriptComponentGenericFragment fragment = new ScriptComponentExperimentAnalysisFragment();
        fragment.setScriptComponent(this);
        return fragment;
    }

    public void setExperiment(ScriptComponentExperiment experiment) {
        this.experiment = experiment;
    }

    public ScriptComponentExperiment getExperiment() {
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
public class ScriptComponentExperimentAnalysisFragment extends ScriptComponentGenericFragment {
    static final int ANALYSE_EXPERIMENT = 0;

    private CheckedTextView takenExperimentInfo = null;
    private GraphView2D graphView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_analyze_experiment);
        assert child != null;

        ScriptComponentTreeExperimentAnalysis analysisComponent = (ScriptComponentTreeExperimentAnalysis)this.component;

        TextView descriptionTextView = (TextView)child.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        if (!analysisComponent.getDescriptionText().equals(""))
            descriptionTextView.setText(analysisComponent.getDescriptionText());

        Button takeExperiment = (Button)child.findViewById(R.id.analyzeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ExperimentAnalyserActivity.class);
                intent.putExtra("experiment_path",
                        ((ScriptComponentTreeExperimentAnalysis)component).getExperiment().getExperimentPath());
                intent.putExtra("first_start_with_run_settings", true);
                intent.putExtra("first_start_with_run_settings_help", true);
                startActivityForResult(intent, ANALYSE_EXPERIMENT);
            }
        });

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        File experimentPathFile = new File(analysisComponent.getExperiment().getExperimentPath());
        takenExperimentInfo.setText(experimentPathFile.getName());

        graphView = (GraphView2D)view.findViewById(R.id.graphView);
        assert graphView != null;

        updateViews();

        return view;
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        Activity activity = getActivity();

        ScriptComponentExperiment experiment = ((ScriptComponentTreeExperimentAnalysis)component).getExperiment();
        experiment.reloadExperimentAnalysis(activity);

        if (!validateAnalysis()) {
            setState(ScriptComponent.SCRIPT_STATE_ONGOING);

            Toast toast = Toast.makeText(activity, "Mark more data points!", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        setState(ScriptComponent.SCRIPT_STATE_DONE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (component.getState() >= ScriptComponent.SCRIPT_STATE_DONE && !validateAnalysis())
            setState(ScriptComponent.SCRIPT_STATE_ONGOING);
    }

    private boolean validateAnalysis() {
        ScriptComponentExperiment experiment = ((ScriptComponentTreeExperimentAnalysis)component).getExperiment();
        ExperimentAnalysis experimentAnalysis = experiment.getExperimentAnalysis(getActivity());
        if (experimentAnalysis == null)
            return false;

        if (experimentAnalysis.getTagMarkers().getMarkerCount() < 3)
            return false;

        return true;
    }

    @Override
    protected void setState(int state) {
        super.setState(state);
        updateViews();
    }

    private void updateViews() {
        // is view already init?
        if (takenExperimentInfo == null || graphView == null)
            return;

        if (component.getState() == ScriptComponent.SCRIPT_STATE_DONE) {
            takenExperimentInfo.setChecked(true);
            ScriptComponentExperiment experiment = ((ScriptComponentTreeExperimentAnalysis)component).getExperiment();
            ExperimentAnalysis experimentAnalysis = experiment.getExperimentAnalysis(getActivity());
            if (experimentAnalysis == null)
                return;
            MarkerGraphAdapter adapter = new MarkerGraphAdapter(experimentAnalysis, "Position Data:",
                    new XPositionMarkerGraphAxis(), new YPositionMarkerGraphAxis());
            graphView.setAdapter(adapter);
            graphView.setVisibility(View.VISIBLE);
        } else {
            takenExperimentInfo.setChecked(false);
            graphView.setVisibility(View.INVISIBLE);
            graphView.setAdapter(null);
        }
    }
}
