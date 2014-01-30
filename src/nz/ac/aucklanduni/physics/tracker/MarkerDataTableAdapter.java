/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MarkerDataTableAdapter implements ITableAdapter<MarkerData>, MarkersDataModel.IMarkersDataModelListener,
        ExperimentAnalysis.IExperimentAnalysisListener {
    private MarkersDataModel model;
    private List<ITableAdapterListener> listeners;
    private ExperimentAnalysis experimentAnalysis;

    public MarkerDataTableAdapter(MarkersDataModel model, ExperimentAnalysis experimentAnalysis) {
        this.model = model;
        model.addListener(this);
        listeners = new ArrayList<ITableAdapterListener>();
        this.experimentAnalysis = experimentAnalysis;
        this.experimentAnalysis.addListener(this);
    }

    public void release() {
        model.removeListener(this);
        listeners.clear();
    }

    @Override
    public int getRowCount() {
        // markers plus header
        return model.getMarkerCount() + 1;
    }

    @Override
    public int getColumnWeight(int column) {
        return 1;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public MarkerData getRow(int index) throws IndexOutOfBoundsException {
        return model.getMarkerDataAt(index);
    }

    @Override
    public View getView(Context context, int row, int column) throws IndexOutOfBoundsException {
        if (row == 0)
            return makeHeaderCell(context, column);

        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.WHITE);

        populateTextView(textView, row, column);

        return textView;
    }

    @Override
    public void updateView(View view, int row, int column) {
        if (row == 0) {
            populateHeaderView((TextView)view, column);
            return;
        }
        populateTextView((TextView)view, row, column);
    }

    private void populateTextView(TextView textView, int row, int column) {
        MarkerData data = getRow(row - 1);

        PointF position = model.getCalibratedMarkerPositionAt(row - 1);
        String text = "";
        if (column == 0)
            text += data.getRunId();
        else if (column == 1)
            text += String.format("%.2f", position.x);
        else if (column == 2)
            text += String.format("%.2f", position.y);
        else if (column == 3)
            text += String.format("%.1f", experimentAnalysis.getExperiment().getRunValueAt(data.getRunId()));
        else
            throw new IndexOutOfBoundsException();

        textView.setText(text);
    }

    private View makeHeaderCell(Context context, int column) {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);

        populateHeaderView(textView, column);
        return textView;
    }

    private void populateHeaderView(TextView textView, int column) {
        String text;
        if (column == 0)
            text = "id";
        else if (column == 1)
            text = "x [" + experimentAnalysis.getXUnit() + "]";
        else if (column == 2)
            text = "y [" + experimentAnalysis.getYUnit() + "]";
        else if (column == 3)
            text = "run value";
        else
            throw new IndexOutOfBoundsException();

        textView.setText(text);
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
    public void onDataChanged(MarkersDataModel model, int index, int number) {
        notifyRowChanged(index, number);
    }

    @Override
    public void onAllDataChanged(MarkersDataModel model) {
        notifyAllRowsChanged();
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

    private void notifyRowChanged(int row, int number) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowUpdated(this, row + 1, number);
    }

    private void notifyAllRowsChanged() {
        for (ITableAdapterListener listener : listeners)
            listener.onAllRowsUpdated(this);
    }

    private void notifyRowSelected(int row) {
        for (ITableAdapterListener listener : listeners)
            listener.onRowSelected(this, row);
    }

    @Override
    public void onUnitPrefixChanged() {
        for (ITableAdapterListener listener : listeners)
            listener.onRowUpdated(this, 0, 1);
    }
}
