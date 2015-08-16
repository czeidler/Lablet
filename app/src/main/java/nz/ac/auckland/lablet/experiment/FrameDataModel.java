/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;


/**
 * Data model for the set of frames of an experiment.
 */
public class FrameDataModel extends WeakListenable<FrameDataModel.IListener>{
    public interface IListener {
        public void onFrameChanged(int newFrame);
        public void onNumberOfFramesChanged();
    }

    private int currentFrame;
    private int numberOfFrames;
    private Integer roiFrame = null;
    private HashMap<Integer, Boolean> frames = new HashMap<Integer, Boolean>();

    public Integer getROIFrame()
    {
        return this.roiFrame;
    }

    public void setROIFrame(int roiFrame)
    {
        this.roiFrame = roiFrame;
    }

    public void setObjectPicked(int frame, boolean isObjectPicked)
    {
        this.frames.put(frame, isObjectPicked);
    }

    public Boolean isObjectPicked(int frame)
    {
        Boolean isPicked = this.frames.get(frame);

        if(isPicked == null)
        {
            return false;
        }
        else
        {
            return isPicked;
        }
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
        for (IListener listener : getListeners())
            listener.onFrameChanged(currentFrame);
    }

    private void notifyNumberOfFramesChanged() {
        for (IListener listener : getListeners())
            listener.onNumberOfFramesChanged();
    }
}