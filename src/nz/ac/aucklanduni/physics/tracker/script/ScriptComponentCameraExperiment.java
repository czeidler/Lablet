/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.aucklanduni.physics.tracker.ExperimentLoader;
import nz.ac.aucklanduni.physics.tracker.R;
import nz.ac.aucklanduni.physics.tracker.camera.CameraExperiment;
import nz.ac.aucklanduni.physics.tracker.camera.CameraExperimentActivity;

import java.io.File;


public class ScriptComponentCameraExperiment extends ScriptComponentViewHolder {
    private ScriptComponentExperiment experiment = new ScriptComponentExperiment();
    private String descriptionText = "Please take a video:";

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentCameraExperimentView(context, (ScriptComponentSheetFragment)parent, this);
    }

    @Override
    public boolean initCheck() {
        return true;
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

class ScriptComponentCameraExperimentView extends ActivityStarterView {
    static final int PERFORM_EXPERIMENT = 0;

    private ScriptComponentCameraExperiment cameraComponent;
    private CheckedTextView takenExperimentInfo = null;
    private VideoView videoView = null;

    public ScriptComponentCameraExperimentView(Context context, ScriptComponentSheetFragment sheetFragment,
                                               ScriptComponentCameraExperiment cameraComponent) {
        super(context, sheetFragment);
        this.cameraComponent = cameraComponent;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.script_component_camera_experiment, null, false);
        assert view != null;

        addView(view);

        TextView descriptionTextView = (TextView)view.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        if (!cameraComponent.getDescriptionText().equals(""))
            descriptionTextView.setText(cameraComponent.getDescriptionText());

        Button takeExperiment = (Button)view.findViewById(R.id.takeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startExperimentActivity();
            }
        });

        videoView = (VideoView)view.findViewById(R.id.videoView);
        assert videoView != null;
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        updateExperimentPath();
    }

    private void startExperimentActivity() {
        Intent intent = new Intent(getContext(), CameraExperimentActivity.class);
        startActivityForResult(intent, PERFORM_EXPERIMENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            if (data == null)
                return;
            if (data.hasExtra("experiment_path")) {
                String experimentPath = data.getStringExtra("experiment_path");
                cameraComponent.getExperiment().setExperimentPath(experimentPath);
                cameraComponent.setState(ScriptComponentTree.SCRIPT_STATE_DONE);

                setExperimentPath(experimentPath);
            }
            return;
        }
    }

    private void setExperimentPath(String experimentPath) {
        cameraComponent.getExperiment().setExperimentPath(experimentPath);
        updateExperimentPath();
    }

    private void updateExperimentPath() {
        String experimentPath = cameraComponent.getExperiment().getExperimentPath();

        ExperimentLoader.Result result = new ExperimentLoader.Result();
        if (ExperimentLoader.loadExperiment(getContext(), experimentPath, result)) {
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
