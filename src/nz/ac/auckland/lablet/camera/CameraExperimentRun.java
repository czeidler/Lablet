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
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.VideoView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.RatioSurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


class CameraExperimentView extends FrameLayout implements IExperimentRun.IExperimentRunListener {
    private RatioSurfaceView preview = null;
    private VideoView videoView = null;
    private SurfaceHolder previewHolder = null;
    final private CameraExperimentRun cameraExperimentRun;
    final private Camera camera;

    public CameraExperimentView(Context context, CameraExperimentRun cameraExperimentRun) {
        super(context);

        this.cameraExperimentRun = cameraExperimentRun;
        this.camera = cameraExperimentRun.getCamera();

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.camera_experiment_view, null, false);
        addView(view);

        preview = (RatioSurfaceView)view.findViewById(R.id.surfaceView);
        previewHolder = preview.getHolder();
        assert previewHolder != null;
        previewHolder.addCallback(surfaceCallback);

        videoView = (VideoView)view.findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(context);
        videoView.setMediaController(mediaController);

        preview.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.INVISIBLE);

        setRatio();
    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
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

    public void startPlayback(String path) {

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
        File videoFile = cameraExperimentRun.getVideoFile();
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
        preview.setRatio(cameraExperimentRun.getPreviewRatio());
        preview.requestLayout();
    }
}

public class CameraExperimentRun extends AbstractExperimentRun {
    private Activity activity;

    private CameraExperimentRunData experimentData;

    private Camera camera = null;
    private Camera.Size videoSize;
    private MediaRecorder recorder = null;

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

    public Camera getCamera() {
        return camera;
    }

    public File getVideoFile() {
        return videoFile;
    }

    /**
     * Helper class to match a camera recording size with a camcorder profile matching for that size.
     */
    class VideoSettings {
        public Integer cameraProfile;
        public Camera.Size videoSize;
    }

    @Override
    public View createExperimentView(Context context) {
        CameraExperimentView cameraExperimentView = new CameraExperimentView(context, this);
        setListener(cameraExperimentView);
        return cameraExperimentView;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem) {
        menuItem.setVisible(false);
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

        experimentData = new CameraExperimentRunData(activity);
        File experimentDir = new File(getExperimentRunGroup().getStorageDir(), experimentData.getUid());
        experimentData.setStorageDir(experimentDir);

        recorder = new MediaRecorder();

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
        selectCamcorderProfile(newVideoSettings);

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
        camera.release();
        camera = null;

        orientationEventListener.disable();

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
        }
    }

    @Override
    public void finish(boolean discardExperiment) throws IOException {
        if (discardExperiment)
            deleteTempFiles();
        else {
            if (!moveTempFilesToExperimentDir())
                throw new IOException();
            experimentData.setVideoFileName(getVideoFileName());
            experimentData.saveExperimentDataToFile();
        }
        videoFile = null;
    }

    private Activity getActivity() {
        return activity;
    }

    private void showQualityPopup(MenuItem menuItem) {
        View menuView = activity.findViewById(R.id.action_settings);
        PopupMenu popup = new PopupMenu(menuView.getContext(), menuView);

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
    public void startPreview() {
        super.startPreview();

        videoFile = null;
        camera.startPreview();
    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void startRecording() throws Exception {
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

        File outputDir = activity.getExternalCacheDir();
        videoFile = new File(outputDir, getVideoFileName());

        recorder.setOutputFile(videoFile.getPath());
        recorder.prepare();

        recorder.start();

        super.startRecording();
    }

    @Override
    public boolean stopRecording() {
        boolean dataTaken = true;
        try {
            recorder.stop();
        } catch (RuntimeException e) {
            // this can happen when the recoding is stopped to quickly and no data has been taken
            e.printStackTrace();
            dataTaken = false;
        }
        recorder.reset();

        camera.lock();
        camera.stopPreview();

        super.stopRecording();

        return dataTaken;
    }

    @Override
    public ExperimentRunData getExperimentData() {
        return experimentData;
    }

    private boolean deleteTempFiles() {
        if (videoFile != null && videoFile.exists())
            return videoFile.delete();
        return true;
    }

    private boolean moveTempFilesToExperimentDir() {
        File storageDir = experimentData.getStorageDir();
        if (!storageDir.exists())
            if (!storageDir.mkdirs())
                return false;
        File target = new File(storageDir, getVideoFileName());
        return StorageLib.moveFile(videoFile, target);
    }

    private void selectCamcorderProfile(VideoSettings videoSettings) {
        selectedVideoSettings = videoSettings;

        videoSize = selectedVideoSettings.videoSize;
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(videoSize.width, videoSize.height);
        camera.setParameters(parameters);

        notifySettingsChanged();
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
        int orientation = activity.getResources().getConfiguration().orientation;
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

    private String getVideoFileName() {
        return videoFileName;
    }

}
