package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.File;


public class CameraRunSettingsActivity extends ExperimentActivity {
    private CameraExperiment cameraExperiment;
    private VideoFrameView videoFrameView;

    private SeekBar seekBar = null;
    private EditText editVideoStart = null;
    private EditText editVideoEnd = null;

    private int videoStartValue;
    private int videoEndValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!loadExperiment(getIntent()))
            return;

        cameraExperiment = (CameraExperiment)getExperiment();

        setContentView(R.layout.camera_run_settings);

        videoFrameView = (VideoFrameView)findViewById(R.id.videoFrameView);
        File storageDir = cameraExperiment.getStorageDir();
        File videoFile = new File(storageDir, cameraExperiment.getVideoFileName());
        videoFrameView.setVideoFilePath(videoFile.getPath());

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        int duration = cameraExperiment.getVideoDuration();
        seekBar.setMax(duration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        NumberPicker frameRatePicker = (NumberPicker)findViewById(R.id.frameRatePicker);
        frameRatePicker.setMinValue(1);
        frameRatePicker.setMaxValue(videoFrameView.getVideoFrameRate());
        frameRatePicker.setValue(cameraExperiment.getAnalysisFrameRate());

        videoStartValue = cameraExperiment.getAnalysisVideoStart();
        videoEndValue = cameraExperiment.getAnalysisVideoEnd();

        editVideoStart = (EditText)findViewById(R.id.editStart);
        String string = "";
        string += videoStartValue;
        editVideoStart.setText(string);
        editVideoEnd = (EditText)findViewById(R.id.editEnd);
        string = "";
        string += videoEndValue;
        editVideoEnd.setText(string);

        Button setStartButton = (Button)findViewById(R.id.buttonSetStart);
        setStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seekBar.getProgress() >= videoEndValue) {
                    toastMessage("Start value must be smaller than the end value!");
                    return;
                }
                videoStartValue = seekBar.getProgress();
                String string = "";
                string += videoStartValue;
                editVideoStart.setText(string);
            }
        });

        Button setEndButton = (Button)findViewById(R.id.buttonSetEnd);
        setEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoStartValue >= seekBar.getProgress()) {
                    toastMessage("Start value must be smaller than the end value!");
                    return;
                }
                videoEndValue = seekBar.getProgress();
                String string = "";
                string += videoEndValue;
                editVideoEnd.setText(string);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void seekTo(int time) {
        videoFrameView.seekToFrame(time * 1000);
    }

    protected void toastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}