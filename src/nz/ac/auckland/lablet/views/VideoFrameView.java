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

    private long queuedRequest = -1;

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
                break;
            }
        }

        setRatio(((float)(videoWidth) / videoHeight));
    }

    public void seekToFrame(long positionMicroSeconds) {
        if (seekToFrameExtractor != null)
            seekToFrameExtractor.seekToFrame(positionMicroSeconds);
        else
            queuedRequest = positionMicroSeconds;
    }

    protected void toastMessage(String message) {
        Context context = getContext();
        assert context != null;
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

}