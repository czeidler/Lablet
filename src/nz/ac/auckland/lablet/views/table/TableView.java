/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of a table view. Uses the {@link nz.ac.auckland.lablet.views.table.ITableAdapter}.
 */
public class TableView extends TableLayout {
    protected ITableAdapter adapter = null;
    TableRow selectedRow = null;
    final int rowBackgroundColor = Color.WHITE;
    final int headerRowBackgroundColor = Color.rgb(100, 100, 100);
    final int selectedRowColor = Color.rgb(200, 200, 200);
    final List<TableRow> tableRowOrder = new ArrayList<>();

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlwaysDrawnWithCacheEnabled(true);
    }

    public void setAdapter(ITableAdapter tableAdapter) {
        removeAllViews();
        if (adapter != null)
            adapter.removeListener(tableAdapterListener);
        adapter = tableAdapter;

        if (adapter == null)
            return;

        adapter.addListener(tableAdapterListener);

        reload();
    }

    private void reload() {
        tableRowOrder.clear();
        removeAllViews();
        for (int row = 0; row < adapter.getRowCount(); row++) {
            TableRow tableRow = createRow(row);
            addView(tableRow);
            tableRowOrder.add(tableRow);
        }
        selectRow(adapter.getSelectedRow() + 1);
    }

    private ITableAdapter.IListener tableAdapterListener = new ITableAdapter.IListener() {
        @Override
        public void onRowAdded(ITableAdapter table, int row) {
            TableRow tableRow = createRow(row);
            addView(tableRow, row);
            tableRowOrder.add(row, tableRow);
            if (adapter.getSelectedRow() == row - 1)
                selectRow(row);
        }

        @Override
        public void onRowRemoved(ITableAdapter table, int row) {
            TableRow tableRow = tableRowOrder.get(row);
            tableRowOrder.remove(row);
            removeView(tableRow);
        }

        @Override
        public void onRowUpdated(ITableAdapter table, int row, int number) {
            for (int i = row; i < row + number; i++) {
                TableRow tableRow = (TableRow) getChildAt(i);
                if (tableRow == null)
                    break;
                for (int column = 0; column < adapter.getColumnCount(); column++) {
                    adapter.updateView(tableRow.getChildAt(column), i, column);
                }
            }
        }

        @Override
        public void onAllRowsUpdated(ITableAdapter table) {
            reload();
        }

        @Override
        public void onRowSelected(ITableAdapter table, int row) {
            selectRow(row);
        }
    };

    private void setRowBackgroundColor(TableRow row, int color) {
        for (int i = 0; i < row.getVirtualChildCount(); i++)
            row.getVirtualChildAt(i).setBackgroundColor(color);
    }

    private TableRow createRow(int row) {
        TableRow tableRow = new TableRow(getContext());

        TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(params);

        for (int column = 0; column < adapter.getColumnCount(); column++) {
            View cell = adapter.getView(getContext(), row, column);
            if (row == 0)
                cell.setBackgroundColor(headerRowBackgroundColor);
            else
                cell.setBackgroundColor(rowBackgroundColor);
            tableRow.addView(cell);
            TableRow.LayoutParams cellParams = (TableRow.LayoutParams)cell.getLayoutParams();
            assert cellParams != null;
            cellParams.setMargins(1, 1, 1, 1);
            cellParams.weight = adapter.getColumnWeight(column);
        }

        return tableRow;
    }

    private void selectRow(int row) {
        if (selectedRow != null) {
            setRowBackgroundColor(selectedRow, rowBackgroundColor);
            selectedRow = null;
        }

        if (row <= 0 || row >= adapter.getRowCount())
            return;

        selectedRow = (TableRow)getChildAt(row);
        if (selectedRow != null)
            setRowBackgroundColor(selectedRow, selectedRowColor);
    }
}
