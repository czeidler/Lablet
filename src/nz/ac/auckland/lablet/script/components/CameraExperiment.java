/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.*;
import nz.ac.auckland.lablet.camera.CameraSensorData;
import nz.ac.auckland.lablet.camera.CameraSensorPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentPluginHelper;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.script.ScriptTreeNode;
import nz.ac.auckland.lablet.script.ScriptComponentViewHolder;
import nz.ac.auckland.lablet.script.ScriptRunnerActivity;

import java.io.File;


/**
 * Script component that has view for starting  a camera experiment.
 */
public class CameraExperiment extends ScriptComponentViewHolder {
    private ScriptExperimentRef experiment = new ScriptExperimentRef();
    private String descriptionText = "Please take a video:";
    private int requestedVideoWidth = -1;
    private int requestedVideoHeight = -1;

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentCameraExperimentView(context, (ScriptComponentSheetFragment)parent, this);
    }

    @Override
    public boolean initCheck() {
        return true;
    }

    public ScriptExperimentRef getExperiment() {
        return experiment;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public int getRequestedVideoWidth() {
        return requestedVideoWidth;
    }
    public int getRequestedVideoHeight() {
        return requestedVideoHeight;
    }

    public void setRequestedResolution(int width, int height) {
        requestedVideoWidth = width;
        requestedVideoHeight = height;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        if (experiment.getExperimentPath() != null)
            bundle.putString("experiment_path", experiment.getExperimentPath());
    }

    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        experiment.setExperimentPath(bundle.getString("experiment_path", ""));
        return true;
    }
}

/**
 * View to start a camera experiment activity.
 */
class ScriptComponentCameraExperimentView extends ActivityStarterView {
    static final int PERFORM_EXPERIMENT = 0;

    private CameraExperiment cameraComponent;
    private CheckedTextView takenExperimentInfo = null;
    private VideoView videoView = null;

    public ScriptComponentCameraExperimentView(Context context, ScriptComponentSheetFragment sheetFragment,
                                               CameraExperiment cameraComponent) {
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

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        if (!cameraComponent.getExperiment().getExperimentPath().equals(""))
            updateExperimentPath();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE)
            return;
        videoView.start();
    }

    private void startExperimentActivity() {
        Intent intent = new Intent(getContext(), ExperimentActivity.class);
        Bundle options = new Bundle();
        options.putBoolean("show_analyse_menu", false);
        options.putBoolean("sensors_editable", false);
        options.putInt("max_number_of_runs", 1);
        // requested resolution
        int requestedWidth = cameraComponent.getRequestedVideoWidth();
        int requestedHeight = cameraComponent.getRequestedVideoHeight();
        if (requestedWidth > 0 && requestedHeight > 0) {
            options.putInt("requested_video_width", requestedWidth);
            options.putInt("requested_video_height", requestedHeight);
        }
        options.putString("experiment_base_directory", getScriptExperimentsDir().getPath());

        String[] pluginName = new String[] {new CameraSensorPlugin().getIdentifier()};
        ExperimentPluginHelper.packStartExperimentIntent(intent, pluginName, options);
        intent.putExtras(options);

        startActivityForResult(intent, PERFORM_EXPERIMENT);
    }

    private File getScriptExperimentsDir() {
        ScriptComponentSheetFragment sheetFragment = (ScriptComponentSheetFragment)parent;
        ScriptRunnerActivity activity = (ScriptRunnerActivity)sheetFragment.getActivity();
        File scriptUserDataDir = activity.getScriptUserDataDir();

        File scriptExperimentDir = new File(scriptUserDataDir, "experiments");
        if (!scriptExperimentDir.exists())
            scriptExperimentDir.mkdirs();
        return scriptExperimentDir;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            if (data == null)
                return;
            if (data.hasExtra("experiment_path")) {
                String oldExperiment = cameraComponent.getExperiment().getExperimentPath();
                if (!oldExperiment.equals(""))
                    StorageLib.recursiveDeleteFile(new File(oldExperiment));

                String experimentPath = data.getStringExtra("experiment_path");
                cameraComponent.getExperiment().setExperimentPath(experimentPath);
                cameraComponent.setState(ScriptTreeNode.SCRIPT_STATE_DONE);

                setExperimentPath(experimentPath);
            }
        }
    }

    private void setExperimentPath(String experimentPath) {
        cameraComponent.getExperiment().setExperimentPath(experimentPath);

        updateExperimentPath();
    }

    private void updateExperimentPath() {
        // Loading a fragment page with 3 camera experiments is very slow. Make it more responsive by loading the
        // experiments asynchronously.
        final String experimentPath = cameraComponent.getExperiment().getExperimentPath();
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                ExperimentData experimentData = new ExperimentData();
                if (!experimentData.load(getContext(), new File(experimentPath)))
                    return null;

                // TODO fix if there are more than one runs or sensors
                SensorData sensorData = experimentData.getRuns().get(0).sensorDataList.get(0);
                CameraSensorData cameraSensorData = (CameraSensorData)sensorData;
                return new File(cameraSensorData.getStorageDir(), cameraSensorData.getVideoFileName());
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result == null) {
                    takenExperimentInfo.setVisibility(View.INVISIBLE);
                    return;
                }

                File videoFile = (File)result;
                takenExperimentInfo.setVisibility(View.VISIBLE);
                takenExperimentInfo.setChecked(true);
                File experimentPathFile = new File(experimentPath);
                takenExperimentInfo.setText(experimentPathFile.getName());

                videoView.setVideoURI(Uri.parse(videoFile.getPath()));
                videoView.start();
            }
        };

        task.execute();
    }
}
