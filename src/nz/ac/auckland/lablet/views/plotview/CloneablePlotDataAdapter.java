/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


public abstract class CloneablePlotDataAdapter extends AbstractPlotDataAdapter {
    abstract public CloneablePlotDataAdapter clone(Region1D region);
}
