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
import nz.ac.auckland.lablet.views.VideoFrameView;

import java.io.File;


/**
 * Displays the video at a certain frame, depending on the current run value.
 */
class CameraExperimentFrameView extends VideoFrameView {
    final private MotionAnalysis motionAnalysis;
    final private VideoData sensorData;
    private int currentRun = -1;

    public CameraExperimentFrameView(Context context, MotionAnalysis motionAnalysis) {
        super(context);

        setWillNotDraw(false);

        this.motionAnalysis = motionAnalysis;
        this.sensorData = motionAnalysis.getVideoData();

        File storageDir = motionAnalysis.getVideoData().getStorageDir();
        File videoFile = new File(storageDir, sensorData.getVideoFileName());
        setVideoFilePath(videoFile.getPath(), motionAnalysis.getVideoRotation());
    }

    public void setCurrentFrame(int frame) {
        currentRun = frame;
        CalibrationVideoTimeData timeData = motionAnalysis.getCalibrationVideoTimeData();
        long positionMicroSeconds = (long)timeData.getTimeAt(frame);
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

    public RectF getDataRange() {
        RectF range = new RectF();
        range.left = 0;
        range.right = sensorData.getMaxRawX();
        range.top = sensorData.getMaxRawY();
        range.bottom = 0;
        return range;
    }
}
