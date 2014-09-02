/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import nz.ac.auckland.lablet.experiment.Unit;


public interface ITimeCalibration {
    public Unit getUnit();
    public float getTimeFromRaw(float raw);
}
