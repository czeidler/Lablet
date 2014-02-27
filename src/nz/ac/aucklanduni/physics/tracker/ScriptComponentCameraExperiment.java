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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.File;


public class ScriptComponentCameraExperiment extends ScriptComponentFragmentHolder {
    private ScriptComponentExperiment experiment = new ScriptComponentExperiment();
    private String descriptionText = "";

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

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        bundle.putString("experiment_path", experiment.getExperimentPath());
    }

    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        experiment.setExperimentPath(bundle.getString("experiment_path"));
        return true;
    }
}


class ScriptComponentCameraExperimentFragment extends ScriptComponentGenericFragment {
    static final int PERFORM_EXPERIMENT = 0;

    private CheckedTextView takenExperimentInfo = null;
    private VideoView videoView = null;

    public ScriptComponentCameraExperimentFragment(ScriptComponentCameraExperiment component) {
        super(component);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_camera_experiment);
        assert child != null;

        TextView descriptionTextView = (TextView)child.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        ScriptComponentCameraExperiment cameraComponent = (ScriptComponentCameraExperiment)this.component;
        if (!cameraComponent.getDescriptionText().equals(""))
            descriptionTextView.setText(cameraComponent.getDescriptionText());

        Button takeExperiment = (Button)child.findViewById(R.id.takeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CameraExperimentActivity.class);
                startActivityForResult(intent, PERFORM_EXPERIMENT);
            }
        });

        videoView = (VideoView)view.findViewById(R.id.videoView);
        assert videoView != null;
        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        updateExperimentPath();

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
                setState(ScriptComponent.SCRIPT_STATE_DONE);

                setExperimentPath(experimentPath);
            }
            return;
        }
    }

    private void setExperimentPath(String experimentPath) {
        ScriptComponentCameraExperiment cameraComponent = (ScriptComponentCameraExperiment)this.component;
        cameraComponent.getExperiment().setExperimentPath(experimentPath);
        updateExperimentPath();
    }

    private void updateExperimentPath() {
        ScriptComponentCameraExperiment cameraComponent = (ScriptComponentCameraExperiment)this.component;
        String experimentPath = cameraComponent.getExperiment().getExperimentPath();

        ExperimentLoaderResult result = new ExperimentLoaderResult();
        if (ExperimentLoader.loadExperiment(getActivity(), experimentPath, result)) {
            takenExperimentInfo.setVisibility(View.VISIBLE);
            takenExperimentInfo.setChecked(true);
            File experimentPathFile = new File(experimentPath);
            takenExperimentInfo.setText(experimentPathFile.getName());

            CameraExperiment experiment = (CameraExperiment)result.experiment;
            File videoFile = new File(experiment.getStorageDir(), experiment.getVideoFileName());
            videoView.setVideoURI(Uri.parse(videoFile.getPath()));
            videoView.start();
        } else
            takenExperimentInfo.setVisibility(View.INVISIBLE);
    }
}