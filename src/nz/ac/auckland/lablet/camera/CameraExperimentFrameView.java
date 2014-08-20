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
import android.os.Bundle;
import android.view.View;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.views.IExperimentFrameView;
import nz.ac.auckland.lablet.views.VideoFrameView;

import java.io.File;


/**
 * Implementation of {@link nz.ac.auckland.lablet.views.IExperimentFrameView}.
 * <p>
 * Displays the video at a certain frame, depending on the current run value.
 * </p>
 */
public class CameraExperimentFrameView extends VideoFrameView implements IExperimentFrameView {
    private CameraSensorData experiment;
    private int currentRun = -1;

    public CameraExperimentFrameView(Context context, SensorData sensorData) {
        super(context);

        setWillNotDraw(false);

        assert(sensorData instanceof CameraSensorData);
        this.experiment = (CameraSensorData) sensorData;

        File storageDir = sensorData.getStorageDir();
        File videoFile = new File(storageDir, this.experiment.getVideoFileName());
        setVideoFilePath(videoFile.getPath());
    }

    @Override
    public void setCurrentFrame(int frame) {
        currentRun = frame;
        Bundle bundle = experiment.getRunAt(frame);
        if (bundle == null) {
            toastMessage("can't get run information!");
            return;
        }
        int positionMicroSeconds = bundle.getInt("frame_position");
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
        range.right = experiment.getMaxRawX();
        range.top = experiment.getMaxRawY();
        range.bottom = 0;
        return range;
    }
}
