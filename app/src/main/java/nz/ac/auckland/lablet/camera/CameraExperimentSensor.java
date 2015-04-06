/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.VideoView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.camera.recorder.CameraGLTextureProducer;
import nz.ac.auckland.lablet.camera.recorder.CameraPreviewRender;
import nz.ac.auckland.lablet.camera.recorder.VideoRecorder;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.RatioGLSurfaceView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;


class CameraExperimentView extends AbstractExperimentSensorView {
    private RatioGLSurfaceView preview = null;
    private VideoView videoView = null;
    final private CameraExperimentSensor cameraExperimentSensor;
    final private Camera camera;

    public CameraExperimentView(Context context, CameraExperimentSensor cameraExperimentSensor) {
        super(context);

        this.cameraExperimentSensor = cameraExperimentSensor;
        this.camera = cameraExperimentSensor.getCamera();

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.camera_experiment_view, this, false);
        addView(view);

        preview = (RatioGLSurfaceView)view.findViewById(R.id.glSurfaceView);
        preview.setEGLContextClientVersion(2);
        preview.setRenderer(new CameraPreviewRender(preview, cameraExperimentSensor.getCameraGLTextureProducer()));
        preview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        preview.setPreserveEGLContextOnPause(false);

        preview.setVisibility(INVISIBLE);

        videoView = (VideoView)view.findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(context);
        videoView.setMediaController(mediaController);

        preview.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);

        // set ratio on orientation changes
        preview.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setRatio();
                        }
                    });
                }
            }
        });
        setRatio();
    }

    private void selectPreview() {
        preview.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStartPreview() {
        selectPreview();
    }

    @Override
    public void onStopPreview() {
    }

    @Override
    public void onStartRecording() {
        selectPreview();
        setKeepScreenOn(true);
    }

    @Override
    public void onStopRecording() {
        setKeepScreenOn(false);
    }

    @Override
    public void onStartPlayback() {
        final File videoFile = cameraExperimentSensor.getVideoFile();
        if (videoFile == null)
            return;

        preview.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);

        videoView.setVideoPath(videoFile.getPath());
        videoView.requestFocus();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                try {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(videoFile.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        videoView.start();
    }

    @Override
    public void onStopPlayback() {
        videoView.stopPlayback();
    }

    @Override
    public void onSettingsChanged() {
        setRatio();
    }

    private void setRatio() {
        preview.setRatio(cameraExperimentSensor.getPreviewRatio());
        preview.requestLayout();
    }
}

/**
 * Helper class to match a camera recording size with a camcorder profile matching for that size.
 */
class VideoSettings {
    public CamcorderSettings camcorderSettings;
    public int width;
    public int height;
    public int previewWidth;
    public int previewHeight;
}

class CamcorderSettings {
    public Integer cameraProfile;
    public int width;
    public int height;
    public int bitRate;
}

interface IRecorderStrategy {
    void release();

    void start(int hintRotation, float recordingFrameRate, VideoSettings videoSettings, String path);
    void stop();
}

/**
 * The VideoRecorder can only record an image from a Surface, i.e. the camera video preview. However, some cameras have
 * smaller preview than video resolution. That can make this strategy unsuitable for some devices because the recorded
 * video resolution is too small.
 */
class VideoRecorderStrategy implements IRecorderStrategy {
    private VideoRecorder videoRecorder;
    private Camera camera;

    public VideoRecorderStrategy(CameraGLTextureProducer producer, Camera camera) {
        videoRecorder = new VideoRecorder();
        videoRecorder.setCameraSource(producer);
        this.camera = camera;
    }

    @Override
    public void release() {
        if (videoRecorder != null) {
            videoRecorder.release();
            videoRecorder = null;
        }
    }

    @Override
    public void start(int hintRotation, float recordingFrameRate, VideoSettings settings, String path) {
        videoRecorder.setRotation(hintRotation);
        videoRecorder.setRecordingFrameRate(recordingFrameRate);
        videoRecorder.startRecording(settings.previewWidth, settings.previewHeight, settings.camcorderSettings.bitRate,
                path);
    }

    @Override
    public void stop() {
        videoRecorder.stopRecording();
    }
}

class MediaRecorderStrategy implements IRecorderStrategy {
    private MediaRecorder recorder;
    private Camera camera;

    public MediaRecorderStrategy(Camera camera) {
        recorder = new MediaRecorder();
        this.camera = camera;
    }

    @Override
    public void release() {
        recorder.release();
    }

    @Override
    public void start(int hintRotation, float recordingFrameRate, VideoSettings settings, String path) {
        // init state
        camera.unlock();
        recorder.setCamera(camera);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // move to data source config state
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // data source config state
        CamcorderProfile profile = CamcorderProfile.get(settings.camcorderSettings.cameraProfile);
        recorder.setVideoEncoder(profile.videoCodec);
        recorder.setVideoFrameRate(profile.videoFrameRate);
        recorder.setVideoEncodingBitRate(profile.videoBitRate);
        recorder.setVideoSize(settings.width, settings.height);
        recorder.setOrientationHint(hintRotation);

        recorder.setOutputFile(path);

        // prepare
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        recorder.start();   // Recording is now started
    }

    @Override
    public void stop() {
        recorder.stop();
        recorder.reset();

        camera.lock();
    }
}


public class CameraExperimentSensor extends AbstractExperimentSensor {
    private Activity activity;

    private VideoData experimentData;

    private Camera camera = null;
    private CameraGLTextureProducer producer = null;
    private IRecorderStrategy recorderStrategy;

    private int cameraId = 0;
    private int rotation;
    private int rotationDegree = 0;
    private OrientationEventListener orientationEventListener;

    private List<VideoSettings> supportedVideoSettings;
    private VideoSettings selectedVideoSettings = null;
    private int requestedVideoWidth = -1;
    private int requestedVideoHeight = -1;

    private File videoFile = null;

    static final int CAMERA_FACE = Camera.CameraInfo.CAMERA_FACING_BACK;

    static final String videoFileName = "video.mp4";

    public CameraExperimentSensor() {
        super();
    }

    public Camera getCamera() {
        return camera;
    }

    public File getVideoFile() {
        return videoFile;
    }

    final static public String SENSOR_NAME = "Camera";

    private float recordingFrameRate = 30f;

    public void setRecordingFrameRate(float recordingFrameRate) {
        boolean wasLowFrameRate = isLowRecordingFrameRate();

        this.recordingFrameRate = recordingFrameRate;

        // re-enable all video settings, also see onCamcorderProfileChanged
        if (wasLowFrameRate && !isLowRecordingFrameRate()) {
            supportedVideoSettings = getSupportedVideoSettings(cameraId, camera, false);
            if (selectedVideoSettings != null) {
                if (!selectVideoSettings(selectedVideoSettings.width, selectedVideoSettings.height))
                    selectedVideoSettings = supportedVideoSettings.get(0);
            }
        }
        onCamcorderProfileChanged(selectedVideoSettings);
    }

    private boolean selectVideoSettings(int width, int height) {
        for (VideoSettings settings : supportedVideoSettings) {
            if (settings.width == width && settings.height == height) {
                selectedVideoSettings = settings;
                return true;
            }
        }
        return false;
    }

    public float getRecordingFrameRate() {
        return recordingFrameRate;
    }

    private boolean isLowRecordingFrameRate() {
        if (recordingFrameRate < 29)
            return true;
        return false;
    }

    public List<Float> getListOfAllowedFrameRates() {
        List<Float> frameRateList = new ArrayList<>();
        frameRateList.add(0.1f);
        frameRateList.add(0.2f);
        frameRateList.add(0.3f);
        frameRateList.add(0.4f);
        frameRateList.add(0.5f);
        frameRateList.add(1f);
        frameRateList.add(1.5f);
        frameRateList.add(2f);
        frameRateList.add(2.5f);
        frameRateList.add(3f);
        frameRateList.add(5f);
        frameRateList.add(6f);
        frameRateList.add(10f);
        frameRateList.add(15f);
        frameRateList.add(30f);

        return frameRateList;
    }

    @Override
    public String getSensorName() {
        return SENSOR_NAME;
    }

    @Override
    public View createExperimentView(Context context) {
        CameraExperimentView cameraExperimentView = new CameraExperimentView(context, this);
        setListener(cameraExperimentView);
        return cameraExperimentView;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem) {
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showQualityPopup(menuItem);
                return true;
            }
        });

        return true;
    }

    @Override
    public void init(Activity activity) {
        this.activity = activity;
        Intent intent = activity.getIntent();
        if (intent.hasExtra("requested_video_width") && intent.hasExtra("requested_video_height")) {
            requestedVideoWidth = intent.getIntExtra("requested_video_width", -1);
            requestedVideoHeight = intent.getIntExtra("requested_video_height", -1);
        }
        if (intent.hasExtra("recording_frame_rate")) {
            float frameRate = intent.getFloatExtra("recording_frame_rate", -1);
            // find best matching frame rate
            float bestFrameRate = -1;
            float bestDiff = Float.MAX_VALUE;
            List<Float> allowedFrameRates = getListOfAllowedFrameRates();
            for (int i = 0; i < allowedFrameRates.size(); i++) {
                Float current = allowedFrameRates.get(i);
                float diff = Math.abs(current - frameRate);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestFrameRate = current;
                }
            }
            recordingFrameRate = bestFrameRate;
        }

        experimentData = new VideoData(this);

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

        producer = new CameraGLTextureProducer(camera);

        // video size
        supportedVideoSettings = getSupportedVideoSettings(cameraId, camera, false);
        // select the first or the best matching requested settings
        VideoSettings newVideoSettings = pickBestVideoSettings(supportedVideoSettings);
        if (requestedVideoWidth > 0 && requestedVideoHeight > 0) {
            int bestMatchValue = Integer.MAX_VALUE;
            for (VideoSettings settings : supportedVideoSettings) {
                int matchValue = (int)Math.pow(settings.previewWidth - requestedVideoWidth, 2)
                        + (int)Math.pow(settings.previewHeight - requestedVideoHeight, 2);
                if (matchValue < bestMatchValue) {
                    bestMatchValue = matchValue;
                    newVideoSettings = settings;
                }
                if (matchValue == 0)
                    break;
            }
        }
        onCamcorderProfileChanged(newVideoSettings);

        // orientation
        rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        setCameraDisplayOrientation(cameraId, camera);

        orientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                int prevRotation = rotation;
                rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
                if (prevRotation != rotation) {
                    if (camera != null)
                        setCameraDisplayOrientation(cameraId, camera);
                }
            }
        };

        if (orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();
    }

    @Override
    public void destroy() {
        camera.stopPreview();
        camera.release();
        camera = null;

        orientationEventListener.disable();

        setRecorderStrategy(null);
    }

    private void setRecorderStrategy(IRecorderStrategy strategy) {
        if (recorderStrategy != null)
            recorderStrategy.release();
        recorderStrategy = strategy;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected_video_width", selectedVideoSettings.previewWidth);
        outState.putInt("selected_video_height", selectedVideoSettings.previewHeight);

        outState.putFloat("selected_recording_frame_rate", recordingFrameRate);

        if (videoFile != null)
            outState.putString("unsaved_recording", videoFile.getPath());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("selected_video_width")
                && savedInstanceState.containsKey("selected_video_height")) {
            requestedVideoWidth = savedInstanceState.getInt("selected_video_width");
            requestedVideoHeight = savedInstanceState.getInt("selected_video_height");
        }
        if (savedInstanceState.containsKey("selected_recording_frame_rate"))
            recordingFrameRate = savedInstanceState.getFloat("selected_recording_frame_rate");

        if (savedInstanceState.containsKey("unsaved_recording")) {
            String filePath = savedInstanceState.getString("unsaved_recording");
            videoFile = new File(filePath);
            unsavedExperimentData = true;
        }
    }

    @Override
    public void finishExperiment(boolean saveData, File storageBaseDir) throws IOException {
        super.finishExperiment(saveData, storageBaseDir);

        if (!saveData)
            deleteTempFiles();
        else {
            File storageDir = getSensorDataStorage(storageBaseDir, this.getClass().getSimpleName());
            if (!moveTempFilesToExperimentDir(storageDir))
                throw new IOException();
            experimentData.setVideoFileName(storageDir, getVideoFileName());
            experimentData.setRecordingFrameRate(recordingFrameRate);
            experimentData.saveExperimentData(storageDir);
        }
        videoFile = null;
    }

    public CameraGLTextureProducer getCameraGLTextureProducer() {
        return producer;
    }

    private Activity getActivity() {
        return activity;
    }

    private void showQualityPopup(MenuItem menuItem) {
        View menuView = activity.findViewById(R.id.action_settings);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

        final int RESOLUTION_ITEM_BASE = 0;
        final int RESOLUTION_GROUP_ID = 1;
        for (int i = 0; i < supportedVideoSettings.size(); i++) {
            VideoSettings videoSettings = supportedVideoSettings.get(i);
            String label = "";
            label += videoSettings.width;
            label += " x ";
            label += videoSettings.height;
            if (isLowRecordingFrameRate() && (videoSettings.width != videoSettings.previewWidth
                    || videoSettings.height != videoSettings.previewHeight))
                label += " (!)";

            MenuItem item = popup.getMenu().add(RESOLUTION_GROUP_ID, RESOLUTION_ITEM_BASE + i, Menu.NONE, label);
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(RESOLUTION_GROUP_ID, true, true);

        final int FRAME_RATE_GROUP_ID = 2;
        final int FRAME_RATE_ITEM_ID = RESOLUTION_ITEM_BASE + supportedVideoSettings.size();

        // frame rate
        String recordingFrameRateLabel = "Recording Frame Rate";
        recordingFrameRateLabel += " (" + new DecimalFormat("#.##").format(recordingFrameRate) + "fps)";
        popup.getMenu().add(FRAME_RATE_GROUP_ID, FRAME_RATE_ITEM_ID, Menu.NONE, recordingFrameRateLabel);
        popup.getMenu().getItem(supportedVideoSettings.indexOf(selectedVideoSettings)).setChecked(true);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == FRAME_RATE_ITEM_ID) {
                    showFrameRateSettingsDialog();
                    return true;
                }
                VideoSettings videoSettings = supportedVideoSettings.get(itemId);
                onCamcorderProfileChanged(videoSettings);
                return true;
            }
        });

        popup.show();
    }

    private void showFrameRateSettingsDialog() {
        FrameRateSettingsDialog dialog = new FrameRateSettingsDialog(getActivity(), this);
        dialog.show();
    }

    @Override
    public void startPreview() {
        super.startPreview();

        videoFile = null;
    }

    @Override
    public void startRecording() throws Exception {
        File outputDir = activity.getExternalCacheDir();
        videoFile = new File(outputDir, getVideoFileName());

        if (isLowRecordingFrameRate())
            setRecorderStrategy(new VideoRecorderStrategy(producer, camera));
        else
            setRecorderStrategy(new MediaRecorderStrategy(camera));

        recorderStrategy.start(getHintRotation(), recordingFrameRate, selectedVideoSettings, videoFile.getPath());

        super.startRecording();
    }

    @Override
    public boolean stopRecording() {
        boolean dataTaken = true;

        recorderStrategy.stop();

        super.stopRecording();

        return dataTaken;
    }

    @Override
    public ISensorData getExperimentData() {
        return experimentData;
    }

    private boolean deleteTempFiles() {
        if (videoFile != null && videoFile.exists())
            return videoFile.delete();
        return true;
    }

    private boolean moveTempFilesToExperimentDir(File storageDir) {
        if (!storageDir.exists())
            if (!storageDir.mkdirs())
                return false;
        File target = new File(storageDir, getVideoFileName());
        try {
            return StorageLib.moveFile(videoFile, target);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns true if the device supports preview == video size even it says it does not do that.
     *
     * This is a workaround for GT-P5210, when recording at 1280 x 720 but with a smaller preview size the recorded
     * video is messed up.
     *
     * @return
     */
    private boolean forcePreviewEqualVideo() {
        if (Build.MODEL.equals("GT-P5210"))
            return true;

        return false;
    }

    private void onCamcorderProfileChanged(VideoSettings videoSettings) {
        selectedVideoSettings = videoSettings;
        Camera.Parameters parameters = camera.getParameters();
        if (isLowRecordingFrameRate()) {
            parameters.setPreviewSize(selectedVideoSettings.width, selectedVideoSettings.height);
        } else {
            if (forcePreviewEqualVideo())
                parameters.setPreviewSize(selectedVideoSettings.width, selectedVideoSettings.height);
            else
                parameters.setPreviewSize(selectedVideoSettings.previewWidth, selectedVideoSettings.previewHeight);
            parameters.set("video-size", "" + selectedVideoSettings.width + "x" + selectedVideoSettings.height);
            parameters.setRecordingHint(true);
        }
        camera.stopPreview();

        try {
            camera.setParameters(parameters);
        } catch (RuntimeException e) {
            if (isLowRecordingFrameRate()) {
                // We really want the video displayed at the preview size because its a requirement for the
                // VideoRecorder.
                supportedVideoSettings = getSupportedVideoSettings(cameraId, camera, true);
                if (!selectVideoSettings(selectedVideoSettings.width, selectedVideoSettings.height))
                    selectedVideoSettings = pickBestVideoSettings(supportedVideoSettings);

                parameters = camera.getParameters();
                parameters.setPreviewSize(selectedVideoSettings.previewWidth, selectedVideoSettings.previewHeight);
                camera.setParameters(parameters);
            } else
                throw new RuntimeException(e);
        }
        camera.startPreview();

        notifySettingsChanged();
    }

    private int getHintRotation() {
        int orientation = activity.getResources().getConfiguration().orientation;
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

    private void addProfileIfSupported(int cameraId, List<CamcorderSettings> list, int profile) {
        if (CamcorderProfile.hasProfile(cameraId, profile)) {
            CamcorderSettings camcorderSettings = new CamcorderSettings();
            camcorderSettings.cameraProfile = profile;
            CamcorderProfile camcorderProfile = CamcorderProfile.get(profile);
            camcorderSettings.bitRate = camcorderProfile.videoBitRate;
            camcorderSettings.width = camcorderProfile.videoFrameWidth;
            camcorderSettings.height = camcorderProfile.videoFrameHeight;
            list.add(camcorderSettings);
        }
    }

    private List<VideoSettings> getSupportedVideoSettings(int cameraId, Camera camera, boolean previewOnly) {
        // use the camcorder profile only to get the video bit rate
        List<CamcorderSettings> supportedProfiles = new ArrayList<>();
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_QCIF);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_QVGA);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_CIF);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_480P);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_720P);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_1080P);
        addProfileIfSupported(cameraId, supportedProfiles, CamcorderProfile.QUALITY_HIGH);

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> videoSizes = parameters.getSupportedVideoSizes();
        if (videoSizes == null || previewOnly)
            videoSizes = camera.getParameters().getSupportedPreviewSizes();

        Collections.sort(videoSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                Integer area1 = size.height * size.width;
                Integer area2 = size2.height * size2.width;
                return area2.compareTo(area1);
            }
        });

        List<VideoSettings> filteredProfiles = new ArrayList<>();
        for (Camera.Size videoSize : videoSizes) {
            double minDiff = Double.MAX_VALUE;
            CamcorderSettings bestMatch = null;
            for (CamcorderSettings settings : supportedProfiles) {
                double diff = Math.pow(videoSize.width - settings.width, 2)
                        + Math.pow(videoSize.height - settings.height, 2);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestMatch = settings;
                }
            }
            VideoSettings settings = new VideoSettings();
            settings.camcorderSettings = bestMatch;
            settings.width = videoSize.width;
            settings.height = videoSize.height;
            filteredProfiles.add(settings);
        }

        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        for (VideoSettings videoSettings : filteredProfiles) {
            Camera.Size bestMatch = matchPreviewSize(videoSettings, previewSizes, 0.0001f);
            if (bestMatch == null)
                bestMatch = matchPreviewSize(videoSettings, previewSizes, Float.MAX_VALUE);
            videoSettings.previewWidth = bestMatch.width;
            videoSettings.previewHeight = bestMatch.height;
        }

        return filteredProfiles;
    }

    private Camera.Size matchPreviewSize(VideoSettings videoSettings, List<Camera.Size> previewSizes, float ratioTolerance) {
        float settingsRatio = (float)videoSettings.width / videoSettings.height;
        Camera.Size bestMatch = null;
        double minDiff = Double.MAX_VALUE;
        for (Camera.Size previewSize : previewSizes) {
            float ratio = (float)previewSize.width / previewSize.height;
            if (Math.abs(settingsRatio - ratio) < ratioTolerance) {
                double diff = Math.pow(videoSettings.width - previewSize.width, 2)
                        + Math.pow(videoSettings.height - previewSize.height, 2);
                if (diff < minDiff) {
                    minDiff = diff;
                    bestMatch = previewSize;
                }
            }
        }
        return bestMatch;
    }

    public VideoSettings pickBestVideoSettings(List<VideoSettings> videoSettings) {
        return videoSettings.get(0);
    }

    // partly copied from android dev page
    private void setCameraDisplayOrientation(int cameraId, android.hardware.Camera camera) {
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

    public float getPreviewRatio() {
        // get preview ratio
        int orientation = activity.getResources().getConfiguration().orientation;
        float width = selectedVideoSettings.previewWidth;
        float height = selectedVideoSettings.previewHeight;

        float r = width / height;
        if (Math.abs(r - 4f/3f) < Math.abs(r - 16f/9f)) {
            width = 4;
            height = 3;
        } else {
            width = 16;
            height = 9;
        }

        float ratio = height / width;
        switch (rotation) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    ratio = 1.f / ratio;
                break;
            default:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    ratio = 1.f / ratio;
        }
        return ratio;
    }

    private String getVideoFileName() {
        return videoFileName;
    }

}
