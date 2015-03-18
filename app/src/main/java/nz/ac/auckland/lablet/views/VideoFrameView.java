/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.graphics.Rect;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.Toast;
import nz.ac.auckland.lablet.camera.decoder.CodecOutputSurface;
import nz.ac.auckland.lablet.camera.decoder.FrameRenderer;
import nz.ac.auckland.lablet.camera.decoder.SeekToFrameExtractor;
import nz.ac.auckland.lablet.camera.recorder.CameraPreviewRender;

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

    private long queuedRequest = -1;

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

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (seekToFrameExtractor != null)
            seekToFrameExtractor.release();
        seekToFrameExtractor = null;

        outputSurface.release();
    }

    public void setVideoFilePath(String path) {
        setVideoFilePath(path, 0);
    }

    public void setVideoFilePath(String path, int videoRotation) {
        videoFilePath = path;

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

        init(videoWidth, videoHeight, videoRotation);
        if (queuedRequest > 0)
            seekToFrame(queuedRequest);
    }

    public void seekToFrame(long positionMicroSeconds) {
        if (seekToFrameExtractor != null)
            seekToFrameExtractor.seekToFrame(positionMicroSeconds);
        else
            queuedRequest = positionMicroSeconds;
    }

}