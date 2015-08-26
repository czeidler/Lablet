/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


import nz.ac.auckland.lablet.misc.Unit;

/**
 * Table column for the marker data table adapter. Provides the x-position.
 */
public class XPositionDataTableColumn extends UnitDataTableColumn {
    final private Unit xUnit;

    public XPositionDataTableColumn(Unit xUnit) {
        this.xUnit = xUnit;

        listenTo(xUnit);
    }

    @Override
    public int size() {
        return dataModel.getDataCount();
    }

    @Override
    public Number getValue(int index) {
        return dataModel.getRealMarkerPositionAt(index).x;
    }

    @Override
    public String getHeader() {
        return xUnit.getName() + " [" + xUnit.getTotalUnit() + "]";
    }
}
