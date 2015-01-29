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
public class TableView extends ListView {
    private TableListAdapter adapter = null;
    private float[] columnWeights = null;
    private float weightsSum = 0;

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
        setColumnWeights(columnWeights);
        setAdapter(adapter);
    }

    public void setColumnWeights(float ... weights) {
        if (adapter == null) {
            this.columnWeights = weights;
            // weightsSum has no meaning without the adapter
            weightsSum = 0;
            return;
        }

        ITableAdapter tableAdapter = adapter.getAdapter();
        int columnCount = tableAdapter.getColumnCount();
        if (weights == null) {
            weights = new float[columnCount];
            for (int i = 0; i < columnCount; i++)
                weights[i] = 1;
            this.columnWeights = weights;
            weightsSum = columnCount;
            return;
        }

        this.columnWeights = weights;

        if (columnCount > weights.length) {
            // fill missing weights with 1
            float[] newWeights = new float[tableAdapter.getColumnCount()];
            for (int i = 0; i < weights.length; i++)
                newWeights[i] = weights[i];
            for (int i = weights.length; i < columnCount; i++)
                newWeights[i] = 1;
            this.columnWeights = newWeights;
        }

        weightsSum = 0;
        for (int i = 0; i < columnCount; i++)
            weightsSum += this.columnWeights[i];
    }

    class RowView extends ViewGroup {
        public RowView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int space = 1;
            final int childCount = getChildCount();
            final float availableSpace = getWidth() - space * (childCount - 1);
            float currentColumnPosition = 0;
            final int height = bottom - top;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                int l = (int)currentColumnPosition;
                int columnWidth = (int)(availableSpace * columnWeights[i] / weightsSum);
                int r = l + columnWidth;
                if (i + 1 == getChildCount())
                    r = right;

                int width = r - l;
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height,
                        MeasureSpec.EXACTLY));
                child.layout(l, 0, r, height);

                currentColumnPosition = r + space;
            }
        }

        private int getPreferredHeight() {
            if (getChildCount() == 0)
                return 0;
            View view = getChildAt(0);
            view.measure(MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.AT_MOST));
            return view.getMeasuredHeight();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int height = getPreferredHeight();
            super.setMeasuredDimension(widthSize, height);
        }
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
            return adapter.getRowCount();
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
        public View getView(int row, View view, ViewGroup viewGroup) {
            RowView rowView;
            if (view != null) {
                rowView = (RowView) view;
                rowView.removeAllViews();
            } else
                rowView = new RowView(viewGroup.getContext());

            for (int column = 0; column < adapter.getColumnCount(); column++) {
                View cell = adapter.getView(viewGroup.getContext(), row, column);
                if (row == 0)
                    cell.setBackgroundColor(headerRowBackgroundColor);
                else if (row == selectedRow)
                    cell.setBackgroundColor(selectedRowColor);
                else
                    cell.setBackgroundColor(rowBackgroundColor);
                rowView.addView(cell);
            }

            return rowView;
        }

        private void selectRow(int row) {
            selectedRow = row + 1;
            notifyDataSetChanged();
        }
    }
}
