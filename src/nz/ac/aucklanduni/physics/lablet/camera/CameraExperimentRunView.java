/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.camera;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import nz.ac.aucklanduni.physics.lablet.experiment.Experiment;
import nz.ac.aucklanduni.physics.lablet.views.IExperimentRunView;
import nz.ac.aucklanduni.physics.lablet.views.VideoFrameView;

import java.io.File;


public class CameraExperimentRunView extends VideoFrameView implements IExperimentRunView {
    private CameraExperiment experiment;
    private int currentRun = -1;

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
    public void setCurrentRun(int run) {
        currentRun = run;
        Bundle bundle = experiment.getRunAt(run);
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
            setCurrentRun(currentRun);
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
