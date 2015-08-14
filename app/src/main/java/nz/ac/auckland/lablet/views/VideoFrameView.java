/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
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
    protected long targetSeekPosition = -1;
    protected SeekToFrameExtractor seekToFrameExtractor;
    protected FrameRenderer frameRenderer;

    protected String videoFilePath = "";

    public VideoFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameView(Context context) {
        super(context);
    }

    private void init(int videoRotation) {
        setWillNotDraw(false);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);

        frameRenderer = new FrameRenderer(videoRotation, new Runnable() {
            @Override
            public void run() {
                pauseStartSeekToFrameExtractor();
                startSeekToFrameExtractor();
            }
        });
        setRenderer(frameRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        pauseStartSeekToFrameExtractor();
    }

    private void pauseStartSeekToFrameExtractor() {
        if (seekToFrameExtractor != null)
            seekToFrameExtractor.release();
        seekToFrameExtractor = null;
    }

    private void startSeekToFrameExtractor() {
        File videoFile = new File(videoFilePath);
        try {
            seekToFrameExtractor = new SeekToFrameExtractor(videoFile, frameRenderer.getSurface());
            frameRenderer.getSurfaceTexture().setOnFrameAvailableListener(
                    new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    requestRender();
                }
            });
            if (targetSeekPosition >= 0)
                seekToFrame(targetSeekPosition);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        // sanitize video rotation
        while (videoRotation < 0)
            videoRotation += 360;
        videoRotation = videoRotation % 360;

        init(videoRotation);
    }

    public void seekToFrame(long positionMicroSeconds) {
        targetSeekPosition = positionMicroSeconds;
        if (seekToFrameExtractor == null)
            return;

        seekToFrameExtractor.seekToFrame(positionMicroSeconds);
    }
}