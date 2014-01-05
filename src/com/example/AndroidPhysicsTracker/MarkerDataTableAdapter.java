package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


class MarkerData {
    public int runId;
    public PointF positionReal;

    public MarkerData() {
        positionReal = new PointF();
    }
}


public class MarkerDataTableAdapter implements  ITableAdapter<MarkerData> {
    private List<MarkerData> markerDataList;
    private List<ITableAdapterListener> listeners;
    private int selectedRow;

    public MarkerDataTableAdapter() {
        markerDataList = new ArrayList<MarkerData>();
        listeners = new ArrayList<ITableAdapterListener>();
        selectedRow = -1;
    }

    @Override
    public int getRowCount() {
        return markerDataList.size();
    }

    @Override
    public int getColumnWeight(int column) {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public MarkerData getRow(int index) throws IndexOutOfBoundsException {
        return markerDataList.get(index);
    }

    @Override
    public View getView(Context context, int row, int column) throws IndexOutOfBoundsException {
        MarkerData data = getRow(row);
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);

        String text = new String();
        if (column == 0)
            text += data.runId;
        else if (column == 1)
            text += data.positionReal.x;
        else if (column == 2)
            text += data.positionReal.y;
        else
            throw new IndexOutOfBoundsException();

        textView.setText(text);
        return textView;
    }

    public void setMarkerPosition(PointF position, int row) {
        MarkerData data = getRow(row);
        data.positionReal = position;
        notifyRowUpdated(row);
    }

    @Override
    public int addRow(MarkerData data) {
        for (int i = 0; i < markerDataList.size(); i++) {
            MarkerData current = markerDataList.get(i);
            if (current.runId > data.runId) {
                markerDataList.add(i, data);
                notifyRowAdded(i);
                return i;
            }
        }
        markerDataList.add(data);
        notifyRowAdded(markerDataList.size() - 1);
        return markerDataList.size() - 1;
    }

    @Override
    public void removeRow(int row) throws IndexOutOfBoundsException {
        markerDataList.remove(row);
        notifyRowRemoved(row);
    }

    @Override
    public void selectRow(int row) {
        selectedRow = row;
        notifyRowSelected(row);
    }

    @Override
    public int getSelectedRow() {
        return selectedRow;
    }

    @Override
    public void addListener(ITableAdapterListener listener) {
        listeners.add(listener);
    }

    private void notifyRowAdded(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowAdded(this, row);
    }

    private void notifyRowRemoved(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowRemoved(this, row);
    }

    private void notifyRowUpdated(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowUpdated(this, row);
    }

    private void notifyRowSelected(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowSelected(this, row);
    }
}
