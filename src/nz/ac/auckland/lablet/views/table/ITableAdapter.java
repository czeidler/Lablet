/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import android.content.Context;
import android.view.View;


public interface ITableAdapter {
    public int getRowCount();

    public int getColumnWeight(int column);
    public int getColumnCount();

    public View getView(Context context, int row, int column);
    public void updateView(View view, int row, int column);

    public void selectRow(int row);
    public int getSelectedRow();

    public void addListener(IListener listener);
    public boolean removeListener(IListener listener);

    public interface IListener {
        public void onRowAdded(ITableAdapter table, int row);
        public void onRowRemoved(ITableAdapter table, int row);
        public void onRowUpdated(ITableAdapter table, int row, int number);
        public void onAllRowsUpdated(ITableAdapter table);
        public void onRowSelected(ITableAdapter table, int row);
    }
}

