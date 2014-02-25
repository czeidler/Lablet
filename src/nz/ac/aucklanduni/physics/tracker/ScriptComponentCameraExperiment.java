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


class ScriptComponentCameraExperimentFragment extends android.support.v4.app.Fragment {
    private ScriptComponentCameraExperiment component;
    static final int PERFORM_EXPERIMENT = 0;

    public ScriptComponentCameraExperimentFragment(ScriptComponentCameraExperiment component) {
        this.component = component;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.script_component_camera_experiment, container, false);
        assert view != null;

        Button takeExperiment = (Button)view.findViewById(R.id.takeExperimentButton);
        assert(takeExperiment != null);
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraExperimentActivity.class);
                startActivityForResult(intent, PERFORM_EXPERIMENT);
            }
        });

        Button okButton = (Button)view.findViewById(R.id.doneButton);
        assert(okButton != null);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                component.setState(ScriptComponent.SCRIPT_STATE_DONE);
                component.getScript().notifyGoToComponent(component.getNext());
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
                component.getExperiment().setExperimentPath(experimentPath);
            }
            return;
        }
    }
}