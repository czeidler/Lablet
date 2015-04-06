/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import nz.ac.auckland.lablet.camera.decoder.CodecOutputSurface;
import nz.ac.auckland.lablet.camera.decoder.FrameRenderer;
import nz.ac.auckland.lablet.camera.decoder.SeekToFrameExtractor;

import java.io.File;
import java.io.IOException;


/**
 * Displays a video at a certain frame.
 * <p>
 * A normal {@link android.widget.VideoView} can't seek to an exact position in a video, this view can. To do so the
 * {@link nz.ac.auckland.lablet.camera.decoder.SeekToFrameExtractor} is leveraged.
 * </p>
 */
public class VideoFrameView extends RatioGLSurfaceView {
    protected SeekToFrameExtractor seekToFrameExtractor;
    private CodecOutputSurface outputSurface;

    protected String videoFilePath = "";

    public VideoFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameView(Context context) {
        super(context);
    }

    private void init(int width, int height, int videoRotation) {
        setWillNotDraw(false);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        outputSurface = new CodecOutputSurface(width, height);

        FrameRenderer frameRenderer = new FrameRenderer(outputSurface, videoRotation);
        setRenderer(frameRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        startSeekToFrameExtractor();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        pauseStartSeekToFrameExtractor();
        outputSurface.release();
    }

    private void pauseStartSeekToFrameExtractor() {
        if (seekToFrameExtractor != null)
            seekToFrameExtractor.release();
        seekToFrameExtractor = null;
    }

    private void startSeekToFrameExtractor() {
        File videoFile = new File(videoFilePath);
        try {
            seekToFrameExtractor = new SeekToFrameExtractor(videoFile, outputSurface.getSurface());
            seekToFrameExtractor.setListener(new SeekToFrameExtractor.IListener() {
                @Override
                public void onFrameExtracted() {
                    requestRender();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int rotationToScreen(int rotation, int deviceOrientation, int displayRotation) {
        if (rotation == 90)
            rotation = 270;
        else if (rotation == 270)
            rotation = 90;

        int shift = 0;
        if (rotation == 90)
            shift = -90;
        if (rotation == 180)
            shift = 180;
        if (rotation == 270)
            shift = 90;

        if (deviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (displayRotation == Surface.ROTATION_0 || displayRotation == Surface.ROTATION_180)
                rotation = (rotation + shift) % 360;
        }
        if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (displayRotation == Surface.ROTATION_90 || displayRotation == Surface.ROTATION_270)
                rotation = (rotation + shift) % 360;
        }

        return rotation;
    }

    public void setVideoFilePath(String path, int videoRotation) {
        videoFilePath = path;

        Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayRotation = display.getRotation();
        int deviceOrientation = getContext().getResources().getConfiguration().orientation;
        int screenVideoRotation = rotationToScreen(videoRotation, deviceOrientation, displayRotation);

        int videoWidth = 1;
        int videoHeight = 1;

        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(videoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);

                videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);

                if (videoRotation == 90 || videoRotation == 270) {
                    int temp = videoWidth;
                    videoWidth = videoHeight;
                    videoHeight = temp;
                }
                break;
            }
        }

        setRatio(((float)(videoWidth) / videoHeight));

        init(videoWidth, videoHeight, screenVideoRotation);
    }

    public void seekToFrame(long positionMicroSeconds) {
        if (seekToFrameExtractor == null)
            startSeekToFrameExtractor();
        seekToFrameExtractor.seekToFrame(positionMicroSeconds);
    }

    @Override
    public void onPause() {
        super.onPause();

        pauseStartSeekToFrameExtractor();
    }
}