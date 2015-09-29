/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;


/**
 * Interface to map a time stamp to a certain frame number.
 *
 * Since a video can be analysed at different frame rates, a frame number does not has a fix time stamp.
 */
public interface ITimeData {
    /**
     * Get the number of frames.
     *
     * @return number of frames
     */
    int getSize();

    /**
     * Get time value.
     *
     * @param index of the frame number
     * @return time in milli seconds
     */
    float getTimeAt(float index);
}
