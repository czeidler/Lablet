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
import android.view.View;
import android.widget.TextView;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.List;


/**
 * MarkerDataTableAdapter that can holds a set of {@link nz.ac.auckland.lablet.views.table.DataTableColumn}s.
 */
public class ColumnDataTableAdapter extends WeakListenable<ITableAdapter.IListener> implements ITableAdapter {

    private List<DataTableColumn> columns = new ArrayList<>();

    public void addColumn(DataTableColumn column) {
        columns.add(column);
    }

    @Override
    public int getRowCount() {
        if (columns.size() == 0)
            return 0;

        int rowCount = Integer.MAX_VALUE;
        for (DataTableColumn column : columns) {
            int currentRowCount = column.size();
            if (currentRowCount < rowCount)
                rowCount = currentRowCount;

        }

        return rowCount + 1;
    }

    @Override
    public int getColumnWeight(int column) {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    public DataTableColumn getColumn(int i) {
        return columns.get(i);
    }

    protected void populateTextView(TextView textView, int index, int columnNumber) {
        DataTableColumn column = columns.get(columnNumber);
        if (column == null)
            throw new IndexOutOfBoundsException();

        String text = column.getStringValue(index);
        textView.setText(text);
    }

    protected void populateHeaderView(TextView textView, int columnNumber) {
        DataTableColumn column = columns.get(columnNumber);
        if (column == null)
            throw new IndexOutOfBoundsException();

        textView.setText(column.getHeader());
    }

    @Override
    public void updateView(View view, int row, int column) {
        if (row == 0) {
            populateHeaderView((TextView)view, column);
            return;
        }
        populateTextView((TextView)view, row - 1, column);
    }

    @Override
    public void selectRow(int row) {

    }

    @Override
    public int getSelectedRow() {
        return -1;
    }


    private View makeHeaderCell(Context context, int column) {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);

        populateHeaderView(textView, column);
        return textView;
    }

    @Override
    public View getView(Context context, int row, int column) throws IndexOutOfBoundsException {
        if (row == 0)
            return makeHeaderCell(context, column);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);

        populateTextView(textView, row - 1, column);

        return textView;
    }
}
