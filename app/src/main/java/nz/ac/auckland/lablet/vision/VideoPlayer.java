/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import android.os.Handler;
import android.os.Looper;
import nz.ac.auckland.lablet.experiment.FrameDataModel;


public class VideoPlayer {
    public interface IListener {
        void onFinished();
    }

    private IListener listener;
    private boolean isPlaying = false;
    private FrameDataModel frameDataModel;

    public VideoPlayer(FrameDataModel frameDataModel) {
        this.frameDataModel = frameDataModel;
    }

    public void play() {
        isPlaying = true;
        postGoToNextFrame();
    }

    public void stop()
    {
        isPlaying = false;
    }

    public void setListener(IListener listener)
    {
        this.listener = listener;
    }

    private void postGoToNextFrame() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNextFrame();
            }
        }, 300);
    }

    private void goToNextFrame() {
        int targetFrame = frameDataModel.getCurrentFrame() + 1;
        int endFrame = frameDataModel.getNumberOfFrames() - 1;
        if (targetFrame >= endFrame) {
            targetFrame = endFrame;
            isPlaying = false;
        }
        frameDataModel.setCurrentFrame(targetFrame);
        if (isPlaying)
            postGoToNextFrame();
        else {
            if(listener != null)
                listener.onFinished();
        }
    }
}
