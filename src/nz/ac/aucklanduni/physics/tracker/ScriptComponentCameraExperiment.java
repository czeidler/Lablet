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
import android.widget.TextView;


public class ScriptComponentCameraExperiment extends ScriptComponentFragmentHolder {
    private ScriptComponentExperiment experiment = new ScriptComponentExperiment();

    public ScriptComponentCameraExperiment(Script script) {
        super(script);
    }

    @Override
    public Fragment createFragment() {
        return new ScriptComponentCameraExperimentFragment(this);
    }

    public ScriptComponentExperiment getExperiment() {
        return experiment;
    }
}


class ScriptComponentCameraExperimentFragment extends ScriptComponentGenericFragment {
    static final int PERFORM_EXPERIMENT = 0;

    public ScriptComponentCameraExperimentFragment(ScriptComponentCameraExperiment component) {
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
                Intent intent = new Intent(getActivity(), CameraExperimentActivity.class);
                startActivityForResult(intent, PERFORM_EXPERIMENT);
            }
        });

        Button okButton = (Button)child.findViewById(R.id.doneButton);
        assert(okButton != null);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setState(ScriptComponent.SCRIPT_STATE_DONE);
            }
        });

        return view;
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            if (data == null)
                return;
            if (data.hasExtra("experiment_path")) {
                String experimentPath = data.getStringExtra("experiment_path");
                ((ScriptComponentCameraExperiment)component).getExperiment().setExperimentPath(experimentPath);
            }
            return;
        }
    }
}