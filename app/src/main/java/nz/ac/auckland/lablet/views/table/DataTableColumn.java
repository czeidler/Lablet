/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Abstract base class for table columns.
 */
public abstract class DataTableColumn {
    protected MarkerDataModel dataModel;

    abstract public int size();
    abstract public Number getValue(int index);
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format("%.2f", number.floatValue());
    }
    abstract public String getHeader();

    public void setDataModel(MarkerDataModel dataModel) {
        this.dataModel = dataModel;
    }
}
