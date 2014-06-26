/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.util.ArrayList;
import java.util.List;


/**
 * Data model for the set of frames of an experiment.
 */
public class FrameDataModel {
    public interface IFrameDataModelListener {
        public void onFrameChanged(int newFrame);
        public void onNumberOfFramesChanged();
    }

    private int currentFrame;
    private int numberOfFrames;

    private List<IFrameDataModelListener> listeners;

    public FrameDataModel() {
        listeners = new ArrayList<>();
    }

    public void addListener(IFrameDataModelListener listener) {
        listeners.add(listener);
    }

    public boolean removeListener(IFrameDataModelListener listener) {
        return listeners.remove(listener);
    }

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
        for (IFrameDataModelListener listener : listeners)
            listener.onFrameChanged(currentFrame);
    }

    private void notifyNumberOfFramesChanged() {
        for (IFrameDataModelListener listener : listeners)
            listener.onNumberOfFramesChanged();
    }
}