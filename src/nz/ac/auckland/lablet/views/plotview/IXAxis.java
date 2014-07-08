/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


public interface IXAxis {
    public float getAxisLeftOffset();
    public float getAxisRightOffset();

    public void setDataRange(float left, float right);

    public float optimalHeight();
    public void setLabel(String label);
    public void setUnit(String unit);
}
