/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.ExperimentActivity;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.RatioSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Activity that runs a {@link CameraExperiment}, i,e., records a video.
 */
public class CameraExperimentActivity extends ExperimentActivity {
    private RatioSurfaceView preview = null;
    private VideoView videoView = null;
    private ImageButton startButton = null;
    private ImageButton stopButton = null;
    private ImageButton newButton = null;

    private SurfaceHolder previewHolder = null;
    private Camera camera = null;
    private Camera.Size videoSize;
    private MediaRecorder recorder = null;
    private AbstractViewState state = null;

    private List<VideoSettings> supportedVideoSettings;
    private VideoSettings selectedVideoSettings = null;
    private int previousVideoWidth = -1;
    private int previousVideoHeight= -1;

    private MenuItem analyseMenuItem = null;
    private MenuItem qualityMenu = null;
    private int cameraId = 0;
    private int rotation;
    private int rotationDegree = 0;
    private OrientationEventListener orientationEventListener;

    private File videoFile = null;
    private boolean unsavedExperimentData = false;

    static final int CAMERA_FACE = Camera.CameraInfo.CAMERA_FACING_BACK;

    static final String videoFileName = "video.mp4";

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.perform_experiment_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                onBackPressed();
                return false;
            }
        });
        analyseMenuItem = menu.findItem(R.id.action_analyse);
        assert analyseMenuItem != null;
        analyseMenuItem.setEnabled(false);
        analyseMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finishExperiment(true);
                return true;
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            Bundle options = intent.getExtras();
            if (options != null) {
                boolean showAnalyseMenu = options.getBoolean("showAnalyseMenu", true);
                analyseMenuItem.setVisible(showAnalyseMenu);
            }
        }

        qualityMenu = menu.findItem(R.id.action_camera_quality_settings);
        assert qualityMenu != null;
        qualityMenu.setVisible(false);
        qualityMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showQualityPopup();
                return true;
            }
        });

        // set states after menu has been init
        if (previewHolder.getSurface() != null) {
            if (!unsavedExperimentData) {
                setState(new PreviewState());
            } else {
                // we have unsaved experiment data means we are in the PlaybackState state
                setState(new PlaybackState());
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void showQualityPopup() {
        View menuView = findViewById(R.id.action_camera_quality_settings);
        PopupMenu popup = new PopupMenu(this, menuView);

        for (int i = 0; i < supportedVideoSettings.size(); i++) {
            Camera.Size size = supportedVideoSettings.get(i).videoSize;
            String label = "";
            label += size.width;
            label += " x ";
            label += size.height;

            MenuItem item = popup.getMenu().add(1, i, i, label);
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(1, true, true);

        popup.getMenu().getItem(supportedVideoSettings.indexOf(selectedVideoSettings)).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                VideoSettings videoSettings = supportedVideoSettings.get(menuItem.getItemId());
                selectCamcorderProfile(videoSettings);
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExperiment(new CameraExperiment(this));

        setContentView(R.layout.camera_experiment);

        preview = (RatioSurfaceView)findViewById(R.id.surfaceView);
        previewHolder = preview.getHolder();
        assert previewHolder != null;
        previewHolder.addCallback(surfaceCallback);

        videoView = (VideoView)findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setKeepScreenOn(true);
        videoView.setMediaController(mediaController);

        recorder = new MediaRecorder();

        startButton = (ImageButton)findViewById(R.id.recordButton);
        stopButton = (ImageButton)findViewById(R.id.stopButton);
        newButton = (ImageButton)findViewById(R.id.newButton);

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
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_video_width", selectedVideoSettings.videoSize.width);
        outState.putInt("selected_video_height", selectedVideoSettings.videoSize.height);

        if (unsavedExperimentData)
            outState.putString("unsaved_recording", videoFile.getPath());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey("selected_video_width")
                && savedInstanceState.containsKey("selected_video_height")) {
            previousVideoWidth = savedInstanceState.getInt("selected_video_width");
            previousVideoHeight = savedInstanceState.getInt("selected_video_height");
        }

        if (savedInstanceState.containsKey("unsaved_recording")) {
            String filePath = savedInstanceState.getString("unsaved_recording");
            videoFile = new File(filePath);

            // setting unsavedExperimentData here; from this information the correct state is set in onResume (after
            // everything has been init)
            unsavedExperimentData = true;
        }
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

        // video size
        supportedVideoSettings = filterCamcorderProfiles(getSupportedCamcorderProfiles(cameraId),
                getSuitableCameraVideoSizes(camera));
        // select the first or a previous settings
        VideoSettings newVideoSettings = supportedVideoSettings.get(0);
        for (VideoSettings settings : supportedVideoSettings) {
            if (settings.videoSize.width == previousVideoWidth && settings.videoSize.height == previousVideoHeight) {
                newVideoSettings = settings;
                break;
            }
        }
        selectCamcorderProfile(newVideoSettings);

        // orientation
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        setCameraDisplayOrientation(cameraId, camera);

        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int prevRotation = rotation;
                rotation = getWindowManager().getDefaultDisplay().getRotation();
                if (prevRotation != rotation) {
                    if (camera != null)
                        setCameraDisplayOrientation(cameraId, camera);
                }
            }
        };
        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();

        // called because we set the state there (we might return from a pause where the menu is not recreated)
        invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        setState(null);

        camera.release();
        camera = null;

        orientationEventListener.disable();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (unsavedExperimentData) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Experiment is not saved");
            builder.setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finishExperiment(false);
                }
            });
            builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    deleteTempFiles();
                    unsavedExperimentData = false;
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

            builder.create().show();
        } else {
            deleteTempFiles();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void selectCamcorderProfile(VideoSettings videoSettings) {
        AbstractViewState previousState = state;
        setState(null);
        selectedVideoSettings = videoSettings;

        videoSize = selectedVideoSettings.videoSize;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(videoSize.width, videoSize.height);
        camera.setParameters(parameters);

        preview.setRatio(getPreviewRatio());
        preview.requestLayout();
        setState(previousState);
    }

    private boolean deleteTempFiles() {
        if (videoFile != null && videoFile.exists())
            return videoFile.delete();
        return true;
    }

    private void startRecording() {
        try {
            camera.unlock();
            recorder.setCamera(camera);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            recorder.setOrientationHint(getHintRotation());

            CamcorderProfile profile = CamcorderProfile.get(cameraId, selectedVideoSettings.cameraProfile);
            if (profile == null)
                throw new Exception("no camcorder profile!");
            recorder.setVideoSize(videoSize.width, videoSize.height);
            recorder.setVideoFrameRate(profile.videoFrameRate);
            recorder.setVideoEncodingBitRate(profile.videoBitRate);

            File outputDir = getCacheDir();
            videoFile = new File(outputDir, getVideoFileName());

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

    private boolean moveTempFilesToExperimentDir() {
        File storageDir = experiment.getStorageDir();
        if (!storageDir.exists())
            if (!storageDir.mkdirs())
                return false;
        File target = new File(storageDir, getVideoFileName());
        return StorageLib.moveFile(videoFile, target);
    }

    private void finishExperiment(boolean startAnalysis) {
        unsavedExperimentData = false;

        try {
            if (!moveTempFilesToExperimentDir())
                throw new IOException();
            ((CameraExperiment)experiment).setVideoFileName(getVideoFileName());
            experiment.saveExperimentDataToFile();

            Intent data = new Intent();
            File outputDir = experiment.getStorageDir();
            data.putExtra("experiment_path", outputDir.getPath());
            data.putExtra("start_analysis", startAnalysis);
            setResult(RESULT_OK, data);
        } catch (IOException e) {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    /**
     * Use the actual camera preview and video sizes to find the matching sizes for preview and recording.
     */
    private List<Camera.Size> getSuitableCameraVideoSizes(Camera camera) {
        List<Camera.Size> videoSizes = camera.getParameters().getSupportedVideoSizes();
        assert videoSizes != null;
        List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
        assert previewSizes != null;

        Collections.sort(previewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                Integer area1 = size.height * size.width;
                Integer area2 = size2.height * size2.width;
                return area2.compareTo(area1);
            }
        });

        List<Camera.Size> matches = new ArrayList<>();
        for (Camera.Size previewSize : previewSizes) {
            for (Camera.Size videoSize : videoSizes) {
                if (previewSize.equals(videoSize)) {
                    matches.add(previewSize);
                    break;
                }
            }
        }
        return matches;
    }

    /**
     * Checks for supported camcorder profiles.
     *
     * The profile with the lowest quality is at the first position.
     *
     * @param cameraId the camera id
     * @return list of supported camcorder profiles
     */
    private List<Integer> getSupportedCamcorderProfiles(int cameraId) {
        List<Integer> supportedCamcorderProfiles = new ArrayList<>();

        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QCIF))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_QCIF);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_QVGA);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_CIF))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_CIF);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_480P);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_720P);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_1080P);
        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH))
            supportedCamcorderProfiles.add(CamcorderProfile.QUALITY_HIGH);
        return supportedCamcorderProfiles;
    }

    /**
     * Helper class to match a camera recording size with a camcorder profile matching for that size.
     */
    class VideoSettings {
        public Integer cameraProfile;
        public Camera.Size videoSize;
    }

    /**
     * Find a suitable profile for the provided camera sizes.
     *
     * For each camera size a profile is selected that has a video size that is equal or just bigger than the camera
     * size. It is assumed that the profiles are sorted; lowest quality first.
     *
     * @param profiles list of profiles, has to be sorted low quality comes first
     * @param cameraSizes list of camera sizes
     * @return the list of found VideoSettings
     */
    private List<VideoSettings> filterCamcorderProfiles(List<Integer> profiles, List<Camera.Size> cameraSizes) {
        List<VideoSettings> filteredProfiles = new ArrayList<>();
        for (Camera.Size cameraSize : cameraSizes) {
            for (Integer profileId : profiles) {
                CamcorderProfile profile = CamcorderProfile.get(profileId);
                if (profile.videoFrameWidth >= cameraSize.width && profile.videoFrameHeight >= cameraSize.height) {
                    VideoSettings settings = new VideoSettings();
                    settings.cameraProfile = profileId;
                    settings.videoSize = cameraSize;
                    filteredProfiles.add(settings);
                    break;
                }
            }
        }
        return filteredProfiles;
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                if (camera != null)
                    camera.setPreviewDisplay(previewHolder);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
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
            qualityMenu.setVisible(true);

            unsavedExperimentData = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            preview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            camera.startPreview();
        }

        public void leaveState() {
            qualityMenu.setVisible(false);
        }

        @Override
        public void onRecordClicked() {
            setState(new RecordState());
        }
    }

    private float getPreviewRatio() {
        // get preview ratio
        int orientation = getResources().getConfiguration().orientation;
        float ratio;
        switch (rotation) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                ratio = (float)videoSize.height / videoSize.width;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    ratio = 1.f / ratio;
                break;
            default:
                ratio = (float)videoSize.width / videoSize.height;
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    ratio = 1.f / ratio;
        }
        return ratio;
    }

    private int getHintRotation() {
        int orientation = getResources().getConfiguration().orientation;
        // 90 degrees surface is -90 device...
        int hintRotation = rotationDegree;
        switch (rotationDegree) {
            case 0:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    hintRotation = 90;
                break;

            case 90:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    hintRotation = 0;
                else
                    hintRotation = 270;
                break;

            case 180:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    hintRotation = 270;
                break;

            case 270:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    hintRotation = 180;
                else
                    hintRotation = 90;
                break;
        }

        return hintRotation;
    }

    // partly copied from android dev page
    private void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
        int orientation = getResources().getConfiguration().orientation;
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        rotationDegree = 0;
        switch (rotation) {
            case Surface.ROTATION_0: rotationDegree = 0; break;
            case Surface.ROTATION_90: rotationDegree = 90; break;
            case Surface.ROTATION_180: rotationDegree = 180; break;
            case Surface.ROTATION_270: rotationDegree = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + rotationDegree) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - rotationDegree + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Lock the screen to the current orientation.
     * @return the previous orientation settings
     */
    private int lockScreenOrientation() {
        int initialRequestedOrientation = getRequestedOrientation();

        // Note: a surface rotation of 90 degrees means a physical device rotation of -90 degrees.
        int orientation = getResources().getConfiguration().orientation;
        switch (rotation) {
            case Surface.ROTATION_0:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_90:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_180:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Surface.ROTATION_270:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
        return initialRequestedOrientation;
    }

    class RecordState extends AbstractViewState {
        private boolean isRecording = false;
        private int initialRequestedOrientation;
        public void enterState() {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            newButton.setVisibility(View.INVISIBLE);

            analyseMenuItem.setEnabled(false);

            preview.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);

            startRecording();
            isRecording = true;

            // disable screen rotation during recording
            initialRequestedOrientation = lockScreenOrientation();

            // don't fall asleep!
            preview.setKeepScreenOn(true);
        }

        public void leaveState() {
            if (isRecording) {
                stopRecording();
                isRecording = false;
            }
            unsavedExperimentData = true;
            camera.stopPreview();

            setRequestedOrientation(initialRequestedOrientation);

            // sleep if tired
            preview.setKeepScreenOn(false);
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
