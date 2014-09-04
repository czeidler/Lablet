/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;


import nz.ac.auckland.lablet.experiment.Unit;

/**
 * Table column for the marker data table adapter. Provides the y-position.
 */
public class YPositionDataTableColumn extends UnitDataTableColumn {
    final private Unit yUnit;

    public YPositionDataTableColumn(Unit yUnit) {
        this.yUnit = yUnit;

        listenTo(yUnit);
    }

    @Override
    public int size() {
        return dataModel.getMarkerCount();
    }

    @Override
    public Number getValue(int index) {
        return dataModel.getRealMarkerPositionAt(index).y;
    }

    @Override
    public String getHeader() {
        return yUnit.getName() + " [" + yUnit.getUnit() + "]";
    }
}
