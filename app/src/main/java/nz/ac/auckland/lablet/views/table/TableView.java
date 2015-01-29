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
import android.view.ViewGroup;
import android.widget.*;


/**
 * Implementation of a table view. Uses the {@link nz.ac.auckland.lablet.views.table.ITableAdapter}.
 */
public class TableView extends GridView {
    private TableListAdapter adapter = null;

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(ITableAdapter tableAdapter) {
        if (this.adapter != null) {
            this.adapter.release();
            setAdapter((ListAdapter)null);
        }
        if (tableAdapter == null) {
            this.adapter = null;
            return;
        }

        this.adapter = new TableListAdapter(tableAdapter);
        setNumColumns(tableAdapter.getColumnCount());
        setVerticalSpacing(1);
        setHorizontalSpacing(1);
        setAdapter(adapter);
    }

    class TableListAdapter extends BaseAdapter {
        final private ITableAdapter adapter;
        private int selectedRow = -1;
        final private int rowBackgroundColor = Color.WHITE;
        final private int headerRowBackgroundColor = Color.rgb(100, 100, 100);
        final private int selectedRowColor = Color.rgb(200, 200, 200);

        private ITableAdapter.IListener tableAdapterListener = new ITableAdapter.IListener() {
            @Override
            public void onRowAdded(ITableAdapter table, int row) {
                notifyDataSetChanged();
            }

            @Override
            public void onRowRemoved(ITableAdapter table, int row) {
                notifyDataSetChanged();
            }

            @Override
            public void onRowUpdated(ITableAdapter table, int row, int number) {
                notifyDataSetChanged();
            }

            @Override
            public void onAllRowsUpdated(ITableAdapter table) {
                notifyDataSetChanged();
            }

            @Override
            public void onRowSelected(ITableAdapter table, int row) {
                selectRow(row);
            }
        };

        public TableListAdapter(ITableAdapter adapter) {
            this.adapter = adapter;
            adapter.addListener(tableAdapterListener);
            selectRow(adapter.getSelectedRow());
        }

        public ITableAdapter getAdapter() {
            return adapter;
        }

        public void release() {
            adapter.removeListener(tableAdapterListener);
        }

        @Override
        public int getCount() {
            return adapter.getRowCount() * adapter.getColumnCount();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            int column = index % adapter.getColumnCount();
            int row = index / adapter.getColumnCount();

            view = adapter.getView(viewGroup.getContext(), view, row, column);

            if (row == 0)
                view.setBackgroundColor(headerRowBackgroundColor);
            else if (row == selectedRow)
                view.setBackgroundColor(selectedRowColor);
            else
                view.setBackgroundColor(rowBackgroundColor);

            return view;
        }

        private void selectRow(int row) {
            selectedRow = row + 1;
            notifyDataSetChanged();
        }
    }
}
