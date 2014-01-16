package com.example.AndroidPhysicsTracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class CameraExperimentActivity extends ExperimentActivity {
    private SurfaceView preview = null;
    private VideoView videoView = null;
    private Button startButton = null;
    private Button stopButton = null;
    private Button newButton = null;

    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private MediaRecorder recorder = null;
    private AbstractViewState state = null;

    private MenuItem analyseMenuItem = null;
    private int cameraId = 0;

    private File videoFile = null;
    private boolean done = false;

    static final int CAMERA_FACE = Camera.CameraInfo.CAMERA_FACING_BACK;

    static private String videoFileName = "video.mp4";

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.perform_experiment_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return false;
            }
        });
        analyseMenuItem = menu.findItem(R.id.action_analyse);
        analyseMenuItem.setEnabled(false);
        analyseMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finishExperiment();
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExperiment(new CameraExperiment(this));

        setContentView(R.layout.perform_camera_experiment);

        preview = (SurfaceView)findViewById(R.id.surfaceView);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);

        videoView = (VideoView)findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setKeepScreenOn(true);
        videoView.setMediaController(mediaController);

        recorder = new MediaRecorder();

        startButton = (Button) findViewById(R.id.recordButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        newButton = (Button) findViewById(R.id.newButton);

        setState(null);

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onRecordClicked();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onStopClicked();
            }
        });

        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (state != null)
                    state.onNewClicked();
            }
        });
    }

    @Override
    public void onDestroy() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }

        if (!done) {
            // delete all files
            deleteStorageDir();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (camera == null) {
            Camera.CameraInfo info = new Camera.CameraInfo();

            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);

                if (info.facing == CAMERA_FACE) {
                    cameraId = i;
                    camera = Camera.open(i);
                }
            }
        }
        if (camera == null)
            camera = Camera.open();


        if (previewHolder.getSurface() != null)
            setState(new PreviewState());
    }

    @Override
    public void onPause() {
        setState(null);

        camera.release();
        camera = null;
        super.onPause();
    }

    private void startRecording() {
        try {
            camera.unlock();
            recorder.setCamera(camera);

            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            //TODO: the quality may change for different cameras. Find the quality matching with the pref preview size.
            CamcorderProfile profile = CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P);
            recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            recorder.setVideoFrameRate(profile.videoFrameRate);
            recorder.setVideoEncodingBitRate(profile.videoBitRate);

            File outputDir = getStorageDir();
            videoFile = new File(outputDir, getVideoFileName());
            videoFile.createNewFile();
            recorder.setOutputFile(videoFile.getPath());

            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Unable to start recording!");
            builder.setNeutralButton("Ok", null);
            builder.create().show();
            setState(null);
            return;
        }
        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.reset();

        camera.lock();
    }

    private String getVideoFileName() {
        return videoFileName;
    }

    private void finishExperiment() {
        done = true;

        ((CameraExperiment)experiment).setVideoFileName(getVideoFileName());
        try {
            saveExperimentDataToFile();
            Intent data = new Intent();
            File outputDir = getStorageDir();
            data.putExtra("experiment_path", outputDir.getPath());
            setResult(RESULT_OK, data);
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            setState(new PreviewState());
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    abstract class AbstractViewState {
        abstract public void enterState();
        abstract public void leaveState();
        public void onRecordClicked() {}
        public void onStopClicked() {}
        public void onNewClicked() {}
    }

    void setState(AbstractViewState newState) {
        if (state != null)
            state.leaveState();
        state = newState;
        if (state == null) {
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            preview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);
        } else
            state.enterState();
    }

    class PreviewState extends AbstractViewState {
        public void enterState() {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            if (analyseMenuItem != null)
                analyseMenuItem.setEnabled(false);

            preview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            camera.startPreview();
        }

        public void leaveState() {
        }

        @Override
        public void onRecordClicked() {
            setState(new RecordState());
        }
    }

    class RecordState extends AbstractViewState {
        private boolean isRecording = false;
        public void enterState() {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            preview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            startRecording();
            isRecording = true;
        }

        public void leaveState() {
            if (isRecording) {
                stopRecording();
                isRecording = false;
            }
            camera.stopPreview();
        }

        @Override
        public void onStopClicked() {
            stopRecording();
            isRecording = false;
            setState(new PlaybackState());
        }
    }

    class PlaybackState extends AbstractViewState {
        public void enterState() {
            startButton.setEnabled(false);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.VISIBLE);

            analyseMenuItem.setEnabled(true);

            preview.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.VISIBLE);

            if (videoFile == null)
                return;
            videoView.setVideoPath(videoFile.getPath());
            videoView.requestFocus();
            videoView.start();
        }

        public void leaveState() {
            videoView.stopPlayback();
        }

        @Override
        public void onNewClicked() {
            setState(new PreviewState());
        }
    }
}
