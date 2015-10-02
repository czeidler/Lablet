/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.*;
import nz.ac.auckland.lablet.camera.VideoData;
import nz.ac.auckland.lablet.camera.CameraSensorPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.script.Script;

import java.io.File;


/**
 * Script component that has view for starting  a camera experiment.
 */
public class CameraExperiment extends SingleExperimentBase {
    private int requestedVideoWidth = -1;
    private int requestedVideoHeight = -1;
    private float recordingFrameRate = -1;

    public CameraExperiment(Script script) {
        super(script);
        setDescriptionText("Please take a video:");
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentCameraExperimentView(context, (ScriptComponentSheetFragment)parent, this);
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

    public void setRecordingFrameRate(float frameRate) {
        recordingFrameRate = frameRate;
    }

    public float getRecordingFrameRate() {
        return recordingFrameRate;
    }
}

/**
 * View to start a camera experiment activity.
 */
class ScriptComponentCameraExperimentView extends ScriptComponentSingleExperimentBaseView {
    private CameraExperiment cameraComponent;
    private CheckedTextView takenExperimentInfo = null;
    private VideoView videoView = null;

    public ScriptComponentCameraExperimentView(Context context, ScriptComponentSheetFragment sheetFragment,
                                               CameraExperiment cameraComponent) {
        super(context, sheetFragment, cameraComponent);
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
        takeExperiment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startExperimentActivity(new CameraSensorPlugin());
            }
        });

        videoView = (VideoView)view.findViewById(R.id.videoView);
        assert videoView != null;
        // Don't show a "Can't play this video." alert.
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        if (!cameraComponent.getExperiment().getExperimentPath().equals(""))
            onExperimentPerformed();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE)
            return;
        videoView.start();
    }

    @Override
    protected Bundle getExperimentOptions() {
        Bundle options = super.getExperimentOptions();

        // requested resolution
        int requestedWidth = cameraComponent.getRequestedVideoWidth();
        int requestedHeight = cameraComponent.getRequestedVideoHeight();
        if (requestedWidth > 0 && requestedHeight > 0) {
            options.putInt("requested_video_width", requestedWidth);
            options.putInt("requested_video_height", requestedHeight);
        }

        float recordingFrameRate = cameraComponent.getRecordingFrameRate();
        if (recordingFrameRate > 0)
            options.putFloat("recording_frame_rate", recordingFrameRate);

        return options;
    }

    @Override
    protected void onExperimentPerformed() {
        // Loading a fragment page with 3 camera experiments is very slow. Make it more responsive by loading the
        // experiments asynchronously.
        final String experimentPath = cameraComponent.getExperiment().getExperimentPath();
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object... objects) {
                ExperimentData experimentData = ExperimentHelper.loadExperimentData(experimentPath);
                if (experimentData == null || experimentData.getRunDataList().size() == 0)
                    return null;

                // TODO fix if there are more than one runs or sensors
                ISensorData sensorData = experimentData.getRunDataList().get(0).sensorDataList.get(0);
                VideoData videoData = (VideoData)sensorData;
                return new File(videoData.getStorageDir(), videoData.getVideoFileName());
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
