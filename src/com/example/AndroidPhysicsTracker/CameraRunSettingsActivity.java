package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.widget.SeekBar;

import java.io.File;


public class CameraRunSettingsActivity extends ExperimentActivity {
    CameraExperiment cameraExperiment;
    VideoFrameView videoFrameView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadExperiment(getIntent());

        cameraExperiment = (CameraExperiment)getExperiment();

        setContentView(R.layout.camera_run_settings);

        videoFrameView = (VideoFrameView)findViewById(R.id.videoFrameView);
        File storageDir = experiment.getStorageDir();
        File videoFile = new File(storageDir, cameraExperiment.getVideoFileName());
        videoFrameView.setVideoFilePath(videoFile.getPath());

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void seekTo(int time) {
        videoFrameView.seekToFrame(time * 1000);
    }
}