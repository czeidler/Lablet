/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


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

import java.io.File;

public class ScriptComponentTreeExperimentAnalysis extends ScriptComponentTreeFragmentHolder {
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
    public android.support.v4.app.Fragment createFragment() {
        return new ScriptComponentExperimentAnalysisFragment(this);
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

class ScriptComponentExperimentAnalysisFragment extends ScriptComponentGenericFragment {
    static final int ANALYSE_EXPERIMENT = 0;

    private CheckedTextView takenExperimentInfo = null;

    public ScriptComponentExperimentAnalysisFragment(ScriptComponentTreeExperimentAnalysis component) {
        super(component);
    }

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
                startActivityForResult(intent, ANALYSE_EXPERIMENT);
            }
        });

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        File experimentPathFile = new File(analysisComponent.getExperiment().getExperimentPath());
        takenExperimentInfo.setText(experimentPathFile.getName());

        return view;
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (!validateAnalysis())
            return;

        setState(ScriptComponentTree.SCRIPT_STATE_DONE);
    }

    private boolean validateAnalysis() {
        ExperimentAnalysis experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(getActivity(),
                ((ScriptComponentTreeExperimentAnalysis)component).getExperiment().getExperimentPath());
        if (experimentAnalysis == null)
            return false;

        if (experimentAnalysis.getTagMarkers().getMarkerCount() < 3) {
            Toast toast = Toast.makeText(getActivity(), "Mark more data points!", Toast.LENGTH_LONG);
            toast.show();
            return false;
        }

        return true;
    }
}
