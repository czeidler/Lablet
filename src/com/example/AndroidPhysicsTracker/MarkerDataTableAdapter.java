package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
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

public class MarkerDataTableAdapter implements ITableAdapter<MarkerData>, MarkersDataModel.IMarkersDataModelListener {
    private MarkersDataModel model;
    private List<ITableAdapterListener> listeners;

    public MarkerDataTableAdapter(MarkersDataModel model) {
        this.model = model;
        model.addListener(this);
        listeners = new ArrayList<ITableAdapterListener>();
    }

    @Override
    public int getRowCount() {
        return model.getMarkerCount();
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
        return model.getMarkerDataAt(index);
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

    @Override
    public void selectRow(int row) {
        model.selectMarkerData(row);
    }

    @Override
    public int getSelectedRow() {
        return model.getSelectedMarkerData();
    }

    @Override
    public void addListener(ITableAdapterListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onDataAdded(MarkersDataModel model, int index) {
        notifyRowAdded(index);
    }

    @Override
    public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {
        notifyRowRemoved(index);
    }

    @Override
    public void onDataChanged(MarkersDataModel model, int index) {
        notifyRowChanged(index);
    }

    @Override
    public void onDataSelected(MarkersDataModel model, int index) {
        notifyRowSelected(index);
    }

    private void notifyRowAdded(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowAdded(this, row);
    }

    private void notifyRowRemoved(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowRemoved(this, row);
    }

    private void notifyRowChanged(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowUpdated(this, row);
    }

    private void notifyRowSelected(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowSelected(this, row);
    }
}
