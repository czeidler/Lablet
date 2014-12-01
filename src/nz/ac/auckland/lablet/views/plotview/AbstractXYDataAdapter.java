/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


public abstract class AbstractXYDataAdapter extends CloneablePlotDataAdapter {
    abstract public Number getX(int index);
    abstract public Number getY(int index);

    abstract public Range getRange(Number leftReal, Number rightReal);

    @Override
    public DataStatistics createDataStatistics() {
        return new XYDataStatistics(this, false);
    }
}
