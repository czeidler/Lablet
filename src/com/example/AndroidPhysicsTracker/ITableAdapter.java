package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.view.View;

public interface ITableAdapter<T> {
    public int getRowCount();

    public int getColumnWeight(int column);
    public int getColumnCount();

    public T getRow(int row);
    public View getView(Context context, int row, int column);

    public void selectRow(int row);
    public int getSelectedRow();

    public void addListener(ITableAdapterListener listener);

    public void release();

    public interface ITableAdapterListener {
        public void onRowAdded(ITableAdapter<?> table, int row);
        public void onRowRemoved(ITableAdapter<?> table, int row);
        public void onRowUpdated(ITableAdapter<?> table, int row);
        public void onAllRowsUpdated(ITableAdapter<?> table);
        public void onRowSelected(ITableAdapter<?> table, int row);
    }
}

