/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.data;

import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.HashMap;


/**
 * Data model for the set of frames of an experiment.
 */
public class FrameDataList extends WeakListenable<FrameDataList.IListener>{
    public interface IListener {
        public void onFrameChanged(int newFrame);
        public void onNumberOfFramesChanged();
    }

    private int currentFrame;
    private int numberOfFrames;

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        if (currentFrame < 0 || currentFrame >= getNumberOfFrames())
            return;

        this.currentFrame = currentFrame;
        notifyFrameChanged(currentFrame);
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
        notifyNumberOfFramesChanged();
    }

    private void notifyFrameChanged(int currentFrame) {
        for (IListener listener : getListeners())
            listener.onFrameChanged(currentFrame);
    }

    private void notifyNumberOfFramesChanged() {
        for (IListener listener : getListeners())
            listener.onNumberOfFramesChanged();
    }
}