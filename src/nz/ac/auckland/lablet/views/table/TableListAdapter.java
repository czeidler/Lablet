/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


class RowView extends ViewGroup {
    public RowView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int space = 1;
        final int childCount = getChildCount();
        final float columnWidth = (getWidth() - space * (childCount - 1)) / childCount;
        final int height = bottom - top;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int l = (int)(i * (columnWidth + space));
            int r = l + (int)columnWidth - space;
            if (i + 1 == getChildCount())
                r = right;

            int width = r - l;
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height,
                    MeasureSpec.EXACTLY));
            child.layout(l, 0, r, height);
        }
    }

    private int getPreferredHeight() {
        if (getChildCount() == 0)
            return 0;
        View view = getChildAt(0);
        view.measure(MeasureSpec.makeMeasureSpec(50, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(500, MeasureSpec.AT_MOST));
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

public class TableListAdapter extends BaseAdapter {
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
