/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;


public interface ITimeData {
    public int getSize();

    /**
     * Get time value.
     *
     * @param index of the frame number
     * @return time in milli seconds
     */
    public float getTimeAt(float index);
}
