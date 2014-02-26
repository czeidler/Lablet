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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ScriptComponentExperimentAnalysis extends ScriptComponentFragmentHolder {
    private ScriptComponentExperiment experiment;

    public ScriptComponentExperimentAnalysis(Script script) {
        super(script);
    }

    @Override
    public Fragment createFragment() {
        return new ScriptComponentExperimentAnalysisFragment(this);
    }

    public void setExperiment(ScriptComponentExperiment experiment) {
        this.experiment = experiment;
    }

    public ScriptComponentExperiment getExperiment() {
        return experiment;
    }

}

class ScriptComponentExperimentAnalysisFragment extends ScriptComponentGenericFragment {
    static final int ANALYSE_EXPERIMENT = 0;

    public ScriptComponentExperimentAnalysisFragment(ScriptComponentExperimentAnalysis component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_camera_experiment);
        assert child != null;

        Button takeExperiment = (Button)child.findViewById(R.id.takeExperimentButton);
        assert(takeExperiment != null);
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ExperimentAnalyserActivity.class);
                intent.putExtra("experiment_path",
                        ((ScriptComponentExperimentAnalysis)component).getExperiment().getExperimentPath());
                startActivityForResult(intent, ANALYSE_EXPERIMENT);
            }
        });

        return view;
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        setState(ScriptComponent.SCRIPT_STATE_DONE);
    }
}
