package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;


public class TableView extends TableLayout implements ITableAdapter.ITableAdapterListener {
    protected ITableAdapter<?> adapter = null;
    TableRow selectedRow = null;
    final int rowBackgroundColor = Color.WHITE;
    final int selectedRowColor = Color.rgb(200, 200, 200);

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(ITableAdapter<?> tableAdapter) {
        adapter = tableAdapter;
        adapter.addListener(this);

        removeAllViews();

        for (int row = 0; row < adapter.getRowCount(); row++) {
            TableRow tableRow = createRow(row);
            addView(tableRow);
        }
        selectRow(adapter.getSelectedRow());
    }

    @Override
    public void onRowAdded(ITableAdapter<?> table, int row) {
        TableRow tableRow = createRow(row);
        addView(tableRow, row);
        if (adapter.getSelectedRow() == row)
            selectRow(row);
    }

    @Override
    public void onRowRemoved(ITableAdapter<?> table, int row) {
        // TODO check if that selects the right row
        removeViewAt(row);
    }

    @Override
    public void onRowUpdated(ITableAdapter<?> table, int row) {
        // TODO reuse the existing TableRow?
        onRowRemoved(table, row);
        onRowAdded(table, row);
    }

    @Override
    public void onRowSelected(ITableAdapter<?> table, int row) {
        selectRow(row);
    }

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
            cell.setBackgroundColor(rowBackgroundColor);
            tableRow.addView(cell);
            TableRow.LayoutParams cellParams = (TableRow.LayoutParams)cell.getLayoutParams();
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

        if (row < 0 || row >= adapter.getRowCount())
            return;

        selectedRow = (TableRow)getChildAt(row);
        if (selectedRow != null)
            setRowBackgroundColor(selectedRow, selectedRowColor);
    }
}
