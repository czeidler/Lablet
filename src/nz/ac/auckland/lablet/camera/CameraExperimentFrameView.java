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
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
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
    private CameraExperimentRunData experiment;
    private int currentRun = -1;

    public CameraExperimentFrameView(Context context, ExperimentRunData experimentRunData) {
        super(context);

        setWillNotDraw(false);

        assert(experimentRunData instanceof CameraExperimentRunData);
        this.experiment = (CameraExperimentRunData) experimentRunData;

        File storageDir = experimentRunData.getStorageDir();
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
    public void fromScreen(PointF screen, PointF real) {
        float xMax = getMaxRawX();
        float yMax = getMaxRawY();
        real.x = screen.x / frame.width() * xMax;
        real.y = yMax - screen.y / frame.height() * yMax;
    }

    @Override
    public void toScreen(PointF real, PointF screen) {
        float xMax = getMaxRawX();
        float yMax = getMaxRawY();
        screen.x = real.x * frame.width() / xMax;
        screen.y = (yMax - real.y) * frame.height() / yMax;
    }

    @Override
    public float getMaxRawX() {
        return experiment.getMaxRawX();
    }

    @Override
    public float getMaxRawY() {
        return experiment.getMaxRawY();
    }
}
