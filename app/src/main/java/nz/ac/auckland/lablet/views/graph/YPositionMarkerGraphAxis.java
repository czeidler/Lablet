/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.misc.Unit;


/**
 * Graph axis for the marker data graph adapter. Provides the y-position.
 */
public class YPositionMarkerGraphAxis extends MarkerGraphAxis {
    final private Unit unit;
    final private IMinRangeGetter minRangeGetter;

    public YPositionMarkerGraphAxis(Unit yUnit, IMinRangeGetter minRangeGetter) {
        this.unit = yUnit;
        this.minRangeGetter = minRangeGetter;
    }

    @Override
    public int size() {
        return getData().size();
    }

    @Override
    public Number getValue(int index) {
        return getData().getRealMarkerPositionAt(index).y;
    }

    @Override
    public String getTitle() {
        return unit.getName();
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public Number getMinRange() {
        if (minRangeGetter == null)
            return -1;
        return minRangeGetter.getMinRange();
    }
}
