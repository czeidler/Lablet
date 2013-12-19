package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class CameraExperimentRunView extends SurfaceView implements IExperimentRunView {
    private CameraExperiment experiment;
    private int positionMicroSeconds = 0;

    private SeekToFrameExtractor seekToFrameExtractor = null;

    public CameraExperimentRunView(Context context, Experiment experiment) {
        super(context);

        setWillNotDraw(false);

        assert(experiment instanceof CameraExperiment);
        this.experiment = (CameraExperiment)experiment;

        getHolder().addCallback(surfaceCallback);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            File storageDir = experiment.getStorageDir();
            File videoFile = new File(storageDir, experiment.getVideoFileName());
            try {
                seekToFrameExtractor = new SeekToFrameExtractor(videoFile, holder.getSurface());
            } catch (IOException e) {
                e.printStackTrace();
                Context context = getContext();
                assert context != null;
                Toast toast = Toast.makeText(context, "can't open video file", Toast.LENGTH_LONG);
                toast.show();
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            seekToFrameExtractor.release();
            seekToFrameExtractor = null;
        }
    };

    @Override
    public void setCurrentRun(Bundle bundle) {
        positionMicroSeconds = bundle.getInt("frame_position");
        positionMicroSeconds *= 1000;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (seekToFrameExtractor != null)
            seekToFrameExtractor.seekToFrame(positionMicroSeconds);
    }
}
