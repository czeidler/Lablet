/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import nz.ac.auckland.lablet.views.IExperimentFrameView;
import nz.ac.auckland.lablet.views.VideoFrameView;

import java.io.File;


/**
 * Implementation of {@link nz.ac.auckland.lablet.views.IExperimentFrameView}.
 * <p>
 * Displays the video at a certain frame, depending on the current run value.
 * </p>
 */
class CameraExperimentFrameView extends VideoFrameView implements IExperimentFrameView {
    final private MotionAnalysis motionAnalysis;
    final private CameraSensorData sensorData;
    private int currentRun = -1;

    public CameraExperimentFrameView(Context context, MotionAnalysis motionAnalysis) {
        super(context);

        setWillNotDraw(false);

        this.motionAnalysis = motionAnalysis;
        this.sensorData = (CameraSensorData) motionAnalysis.getData();

        File storageDir = motionAnalysis.getData().getStorageDir();
        File videoFile = new File(storageDir, sensorData.getVideoFileName());
        setVideoFilePath(videoFile.getPath());
    }

    @Override
    public void setCurrentFrame(int frame) {
        currentRun = frame;
        int positionMicroSeconds = (int) motionAnalysis.getCalibrationVideoFrame().getTimeAt(frame);
        positionMicroSeconds *= 1000;

        seekToFrame(positionMicroSeconds);
    }

    // make sure the view gets redrawn
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != View.VISIBLE)
            return;
        if (currentRun >= 0)
            setCurrentFrame(currentRun);
    }

    @Override
    public RectF getDataRange() {
        RectF range = new RectF();
        range.left = 0;
        range.right = sensorData.getMaxRawX();
        range.top = sensorData.getMaxRawY();
        range.bottom = 0;
        return range;
    }
}
