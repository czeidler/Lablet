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
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.*;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.VideoView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.RatioSurfaceView;
import nz.ac.auckland.lablet.views.RatioTextureView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class CameraExperimentView extends AbstractExperimentSensorView {
    private RatioSurfaceView preview = null;
    private RatioTextureView texturePreview = null;
    private VideoView videoView = null;
    private SurfaceHolder previewHolder = null;
    final private CameraExperimentSensor cameraExperimentSensor;
    final private Camera camera;

    public CameraExperimentView(Context context, CameraExperimentSensor cameraExperimentSensor) {
        super(context);

        this.cameraExperimentSensor = cameraExperimentSensor;
        this.camera = cameraExperimentSensor.getCamera();

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.camera_experiment_view, null, false);
        addView(view);

        preview = (RatioSurfaceView)view.findViewById(R.id.surfaceView);
        /*previewHolder = preview.getHolder();
        assert previewHolder != null;
        previewHolder.addCallback(surfaceCallback);
*/

        preview.setVisibility(INVISIBLE);

        texturePreview = (RatioTextureView)view.findViewById(R.id.textureView);
        //texturePreview.setSurfaceTexture(cameraExperimentSensor.getCameraSurfaceTexture());

        camera.startPreview();

        videoView = (VideoView)view.findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(context);
        videoView.setMediaController(mediaController);

        preview.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);

        setRatio();
    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewDisplay(previewHolder);
                    camera.startPreview();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

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
        File videoFile = cameraExperimentSensor.getVideoFile();
        if (videoFile == null)
            return;

        preview.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.VISIBLE);

        videoView.setVideoPath(videoFile.getPath());
        videoView.requestFocus();
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

        texturePreview.setRatio(cameraExperimentSensor.getPreviewRatio());
        texturePreview.requestLayout();
    }
}

public class CameraExperimentSensor extends AbstractExperimentSensor {
    private Activity activity;

    private CameraSensorData experimentData;

    private Camera camera = null;
    private MediaRecorder recorder = null;
    private TimeLapseRecording timeLapseRecording;

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

    private boolean timeLapseEnabled = false;
    private float timeLapseCaptureRate = 1f;

    public void setTimeLapse(boolean enabled, float captureRate) {
        timeLapseEnabled = enabled;
        timeLapseCaptureRate = captureRate;

        onCamcorderProfileChanged(selectedVideoSettings);
    }

    public float getTimeLapseCaptureRate() {
        return timeLapseCaptureRate;
    }

    public boolean isTimeLapseEnabled() {
        return timeLapseEnabled;
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return timeLapseRecording.getCameraSurfaceTexture();
    }

    /**
     * Helper class to match a camera recording size with a camcorder profile matching for that size.
     */
    class VideoSettings {
        public Integer cameraProfile;
        public Integer cameraTimeLapseProfile = -1;
        public Camera.Size videoSize;
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

        experimentData = new CameraSensorData(activity, this);

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
        supportedVideoSettings = getSupportedCamcorderProfiles();
        // select the first or the best matching requested settings
        VideoSettings newVideoSettings = supportedVideoSettings.get(0);
        if (requestedVideoWidth > 0 && requestedVideoHeight > 0) {
            int bestMatchValue = Integer.MAX_VALUE;
            for (VideoSettings settings : supportedVideoSettings) {
                int matchValue = (int)Math.pow(settings.videoSize.width - requestedVideoWidth, 2)
                        + (int)Math.pow(settings.videoSize.height - requestedVideoHeight, 2);
                if (matchValue < bestMatchValue) {
                    bestMatchValue = matchValue;
                    newVideoSettings = settings;
                }
                if (matchValue == 0)
                    break;
            }
        }
        onCamcorderProfileChanged(newVideoSettings);

        try {
            timeLapseRecording = new TimeLapseRecording(camera,
                    CamcorderProfile.get(cameraId, selectedVideoSettings.cameraProfile));
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        if (timeLapseRecording != null) {
            timeLapseRecording.release();
            timeLapseRecording = null;
        }
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected_video_width", selectedVideoSettings.videoSize.width);
        outState.putInt("selected_video_height", selectedVideoSettings.videoSize.height);

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

        if (savedInstanceState.containsKey("unsaved_recording")) {
            String filePath = savedInstanceState.getString("unsaved_recording");
            videoFile = new File(filePath);
            unsavedExperimentData = true;
        }
    }

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

        if (!saveData)
            deleteTempFiles();
        else {
            if (!moveTempFilesToExperimentDir(storageDir))
                throw new IOException();
            experimentData.setVideoFileName(storageDir, getVideoFileName());
            if (isTimeLapseEnabled())
                experimentData.setTimeLapseCaptureRate(timeLapseCaptureRate);
            experimentData.saveExperimentDataToFile(storageDir);
        }
        videoFile = null;
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
            Camera.Size size = supportedVideoSettings.get(i).videoSize;
            String label = "";
            label += size.width;
            label += " x ";
            label += size.height;

            MenuItem item = popup.getMenu().add(RESOLUTION_GROUP_ID, RESOLUTION_ITEM_BASE + i, Menu.NONE, label);
            item.setCheckable(true);
        }
        popup.getMenu().setGroupCheckable(RESOLUTION_GROUP_ID, true, true);

        final int TIME_LAPSE_GROUP_ID = 2;
        final int TIME_LAPSE_ITEM_ID = RESOLUTION_ITEM_BASE + supportedVideoSettings.size();
        if (supportsTimeLapse()) {
            String timeLapseLabel = "Time Lapse";
            if (isTimeLapseEnabled())
                timeLapseLabel += " (ON)";
            else
                timeLapseLabel += " (OFF)";
            popup.getMenu().add(TIME_LAPSE_GROUP_ID, TIME_LAPSE_ITEM_ID, Menu.NONE, timeLapseLabel);
            popup.getMenu().getItem(supportedVideoSettings.indexOf(selectedVideoSettings)).setChecked(true);

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    int itemId = menuItem.getItemId();
                    if (itemId == TIME_LAPSE_ITEM_ID) {
                        showTimeLapseSettingsDialog();
                        return true;
                    }
                    VideoSettings videoSettings = supportedVideoSettings.get(itemId);
                    onCamcorderProfileChanged(videoSettings);
                    return true;
                }
            });
        }

        popup.show();
    }

    private boolean supportsTimeLapse() {
        for (VideoSettings settings : supportedVideoSettings) {
            if (settings.cameraTimeLapseProfile >= 0)
                return true;
        }
        return false;
    }

    private void showTimeLapseSettingsDialog() {
        TimeLapseSettingsDialog dialog = new TimeLapseSettingsDialog(getActivity(), this);
        dialog.show();
    }

    @Override
    public void startPreview() {
        super.startPreview();

        videoFile = null;
    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void startRecording() throws Exception {
        camera.unlock();
/*
        recorder = new MediaRecorder();
        recorder.setCamera(camera);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        if (timeLapseEnabled)
            recorder.setProfile(CamcorderProfile.get(cameraId, selectedVideoSettings.cameraTimeLapseProfile));
        else {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            CamcorderProfile profile = CamcorderProfile.get(cameraId, selectedVideoSettings.cameraProfile);
            if (profile == null)
                throw new Exception("no camcorder profile!");
            recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            recorder.setVideoFrameRate(profile.videoFrameRate);
            recorder.setVideoEncodingBitRate(profile.videoBitRate);
        }
*/
        File outputDir = activity.getExternalCacheDir();
        videoFile = new File(outputDir, getVideoFileName());

//      camera.startPreview();

        timeLapseRecording.startRecording(videoFile.getPath());
/*
        recorder.setOutputFile(videoFile.getPath());

        recorder.setOrientationHint(getHintRotation());

        if (timeLapseEnabled)
            recorder.setCaptureRate(timeLapseCaptureRate);

        recorder.prepare();

        recorder.start();
*/
        super.startRecording();
    }

    @Override
    public boolean stopRecording() {
        boolean dataTaken = true;
        /*try {
            recorder.stop();
        } catch (RuntimeException e) {
            // this can happen when the recoding is stopped to quickly and no data has been taken
            e.printStackTrace();
            dataTaken = false;
        }
        recorder.reset();
*/
        try {
            timeLapseRecording.stopRecording();
            timeLapseRecording.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            camera.reconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.stopPreview();

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
        return StorageLib.moveFile(videoFile, target);
    }

    private void onCamcorderProfileChanged(VideoSettings videoSettings) {
        selectedVideoSettings = videoSettings;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRecordingHint(true);
        parameters.setPreviewSize(selectedVideoSettings.videoSize.width, selectedVideoSettings.videoSize.height);

        camera.stopPreview();
        camera.setParameters(parameters);
        camera.startPreview();
        notifySettingsChanged();
    }


    private void addProfileIfSupported(List<VideoSettings> list, int profile, int timeLapseProfile) {
        if (CamcorderProfile.hasProfile(cameraId, profile)) {
            VideoSettings videoSettings = new VideoSettings();
            videoSettings.cameraProfile = profile;
            if (CamcorderProfile.hasProfile(cameraId, timeLapseProfile))
                videoSettings.cameraTimeLapseProfile = timeLapseProfile;
            list.add(videoSettings);
        }
    }

    /**
     * Find a suitable profile for the provided camera sizes.
     *
     * For each camera size a profile is selected that has a video size that is equal or just bigger than the camera
     * size. It is assumed that the profiles are sorted; lowest quality first.
     *
     * @return the list of found VideoSettings
     */
    private List<VideoSettings> getSupportedCamcorderProfiles() {
        List<VideoSettings> supportedProfiles = new ArrayList<>();
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_QCIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_QCIF);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_QVGA,
                CamcorderProfile.QUALITY_TIME_LAPSE_QVGA);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_CIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_CIF);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_480P,
                CamcorderProfile.QUALITY_TIME_LAPSE_480P);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_720P,
                CamcorderProfile.QUALITY_TIME_LAPSE_720P);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_1080P,
                CamcorderProfile.QUALITY_TIME_LAPSE_1080P);
        addProfileIfSupported(supportedProfiles, CamcorderProfile.QUALITY_HIGH,
                CamcorderProfile.QUALITY_TIME_LAPSE_HIGH);

        List<Camera.Size> cameraSizes = camera.getParameters().getSupportedVideoSizes();

        Collections.sort(cameraSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size size2) {
                Integer area1 = size.height * size.width;
                Integer area2 = size2.height * size2.width;
                return area2.compareTo(area1);
            }
        });

        List<VideoSettings> filteredProfiles = new ArrayList<>();
        for (Camera.Size cameraSize : cameraSizes) {
            for (VideoSettings settings : supportedProfiles) {
                CamcorderProfile profile = CamcorderProfile.get(settings.cameraProfile);
                if (profile.videoFrameWidth == cameraSize.width && profile.videoFrameHeight == cameraSize.height) {
                    settings.videoSize = cameraSize;
                    filteredProfiles.add(settings);
                    break;
                }
            }
        }
        return filteredProfiles;
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
        float ratio;
        switch (rotation) {
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                ratio = (float)selectedVideoSettings.videoSize.height / selectedVideoSettings.videoSize.width;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    ratio = 1.f / ratio;
                break;
            default:
                ratio = (float)selectedVideoSettings.videoSize.width / selectedVideoSettings.videoSize.height;
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    ratio = 1.f / ratio;
        }
        return ratio;
    }

    private String getVideoFileName() {
        return videoFileName;
    }

}
