package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by lec on 11/12/13.
 */
public class ExperimentAnalyserActivity extends Activity {
    private VideoView videoView = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.experimentanalyser);

        videoView = (VideoView)findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setKeepScreenOn(true);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath("/sdcard/recordvideooutput.3gpp");
        videoView.requestFocus();
        videoView.start();
    }
}