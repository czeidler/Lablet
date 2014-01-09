package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class CameraExperimentRunView extends VideoFrameView implements IExperimentRunView {
    private CameraExperiment experiment;
    private int positionMicroSeconds = 0;

    public CameraExperimentRunView(Context context, Experiment experiment) {
        super(context);

        setWillNotDraw(false);

        assert(experiment instanceof CameraExperiment);
        this.experiment = (CameraExperiment)experiment;

        File storageDir = experiment.getStorageDir();
        File videoFile = new File(storageDir, this.experiment.getVideoFileName());
        setVideoFilePath(videoFile.getPath());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        float ratio = (float)(experiment.getVideoWidth()) / experiment.getVideoHeight();

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        if ((float)(specWidth) / specHeight < ratio) {
            // smaller ratio than the video
            width = specWidth;
            height = (int)(width / ratio);
        } else {
            height = specHeight;
            width = (int)(height * ratio);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    public void setCurrentRun(int run) {
        Bundle bundle = experiment.getRunAt(run);
        if (bundle == null) {
            toastMessage("can't get run information!");
            return;
        }
        positionMicroSeconds = bundle.getInt("frame_position");
        positionMicroSeconds *= 1000;

        seekToFrame(positionMicroSeconds);
    }

    @Override
    public int getNumberOfRuns() {
        return experiment.getNumberOfRuns();
    }

    @Override
    public void fromScreen(PointF screen, PointF real) {
        real.x = screen.x / frame.width() * 100;
        real.y = 100 - screen.y / frame.height() * 100;
    }

    @Override
    public void toScreen(PointF real, PointF screen) {
        screen.x = real.x * frame.width() / 100;
        screen.y = (100 - real.y) * frame.height() / 100;
    }
}
