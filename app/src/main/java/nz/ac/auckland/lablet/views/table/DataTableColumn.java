/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.data.PointDataList;

import java.util.Locale;


/**
 * Abstract base class for table columns.
 */
public abstract class DataTableColumn {
    protected PointDataList dataModel;

    abstract public int size();
    abstract public Number getValue(int index);
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format(Locale.US, "%.2f", number.floatValue());
    }
    abstract public String getHeader();

    public void setDataModel(PointDataList dataModel) {
        this.dataModel = dataModel;
    }
}
