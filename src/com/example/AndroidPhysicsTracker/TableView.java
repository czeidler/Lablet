package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;


public class TableView extends TableLayout implements ITableAdapter.ITableAdapterListener {
    protected ITableAdapter<?> adapter = null;

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
    }

    @Override
    public void onRowAdded(ITableAdapter<?> table, int row) {
        TableRow tableRow = createRow(row);
        addView(tableRow, row);
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

    private TableRow createRow(int row) {
        TableRow tableRow = new TableRow(getContext());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        tableRow.setLayoutParams(params);

        for (int column = 0; column < adapter.getColumnCount(); column++) {
            View cell = adapter.getView(getContext(), row, column);
            tableRow.addView(cell);
            TableRow.LayoutParams cellParams = (TableRow.LayoutParams)cell.getLayoutParams();
            cellParams.weight = adapter.getColumnWeight(column);
        }

        return tableRow;
    }
}
