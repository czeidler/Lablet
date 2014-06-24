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
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


/**
 * Displays a video at a certain frame.
 * <p>
 * A normal {@link android.widget.VideoView} can't seek to an exact position in a video, this view can. To do so the
 * {@link nz.ac.auckland.lablet.views.SeekToFrameExtractor} is leveraged.
 * </p>
 */
public class VideoFrameView extends RatioSurfaceView {
    protected SeekToFrameExtractor seekToFrameExtractor = null;
    protected Rect frame = new Rect();

    protected String videoFilePath = "";

    private int videoWidth;
    private int videoHeight;
    private int videoFrameRate;

    private int queuedRequest = -1;

    public VideoFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoFrameView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        getHolder().addCallback(surfaceCallback);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            getDrawingRect(frame);
            File videoFile = new File(videoFilePath);
            try {
                if (seekToFrameExtractor != null) {
                    seekToFrameExtractor.release();
                    seekToFrameExtractor = null;
                }
                seekToFrameExtractor = new SeekToFrameExtractor(videoFile, holder.getSurface());

                if (queuedRequest >= 0) {
                    seekToFrame(queuedRequest);
                    queuedRequest = -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                toastMessage("can't open video file");
            }
            requestLayout();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (seekToFrameExtractor != null)
                seekToFrameExtractor.release();
            seekToFrameExtractor = null;
        }
    };

    public void setVideoFilePath(String path) {
        videoFilePath = path;

        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(videoFilePath);

        for (int i = 0; i < extractor.getTrackCount(); i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);

            if (mime.startsWith("video/")) {
                extractor.selectTrack(i);

                videoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
                videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
                if (format.containsKey(MediaFormat.KEY_FRAME_RATE))
                    videoFrameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                if (videoFrameRate == 0)
                    videoFrameRate = 30;
                break;
            }
        }

        setRatio(((float)getVideoWidth()) / getVideoHeight());
    }

    public void seekToFrame(int positionMicroSeconds) {
        if (seekToFrameExtractor != null)
            seekToFrameExtractor.seekToFrame(positionMicroSeconds);
        else
            queuedRequest = positionMicroSeconds;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getVideoFrameRate() {
        return videoFrameRate;
    }


    protected void toastMessage(String message) {
        Context context = getContext();
        assert context != null;
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

}