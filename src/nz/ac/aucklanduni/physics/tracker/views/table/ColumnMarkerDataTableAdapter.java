/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.views.table;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.TextView;
import nz.ac.aucklanduni.physics.tracker.Experiment;
import nz.ac.aucklanduni.physics.tracker.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.MarkerData;
import nz.ac.aucklanduni.physics.tracker.MarkersDataModel;

import java.util.ArrayList;
import java.util.List;


abstract class MarkerDataTableAdapter implements ITableAdapter<MarkerData>, MarkersDataModel.IMarkersDataModelListener,
        ExperimentAnalysis.IExperimentAnalysisListener {
    protected MarkersDataModel model;
    protected ExperimentAnalysis experimentAnalysis;
    private List<ITableAdapterListener> listeners;

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
    public int getColumnWeight(int column) {
        return 1;
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

        populateTextView(textView, row - 1, column);

        return textView;
    }

    @Override
    public void updateView(View view, int row, int column) {
        if (row == 0) {
            populateHeaderView((TextView)view, column);
            return;
        }
        populateTextView((TextView)view, row - 1, column);
    }

    abstract protected void populateTextView(TextView textView, int index, int column);

    private View makeHeaderCell(Context context, int column) {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.WHITE);

        populateHeaderView(textView, column);
        return textView;
    }

    abstract protected void populateHeaderView(TextView textView, int column);

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

    @Override
    public void onShowCoordinateSystem(boolean show) {

    }
}

abstract class DataTableColumn {
    protected MarkersDataModel markersDataModel;
    protected ExperimentAnalysis experimentAnalysis;

    public void setMarkersDataModel(MarkersDataModel model) {
        this.markersDataModel = model;
    }

    public void setExperimentAnalysis(ExperimentAnalysis experimentAnalysis) {
        this.experimentAnalysis = experimentAnalysis;
    }

    abstract public int size();
    abstract public Number getValue(int index);
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format("%.2f", number.floatValue());
    }
    abstract public String getHeader();
}

public class ColumnMarkerDataTableAdapter extends MarkerDataTableAdapter {
    private List<DataTableColumn> columns = new ArrayList<DataTableColumn>();

    public ColumnMarkerDataTableAdapter(MarkersDataModel model, ExperimentAnalysis experimentAnalysis) {
        super(model, experimentAnalysis);
    }

    public void addColumn(DataTableColumn column) {
        column.setMarkersDataModel(model);
        column.setExperimentAnalysis(experimentAnalysis);
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
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    protected void populateTextView(TextView textView, int index, int columnNumber) {
        DataTableColumn column = columns.get(columnNumber);
        if (column == null)
            throw new IndexOutOfBoundsException();

        String text = column.getStringValue(index);
        textView.setText(text);
    }

    @Override
    protected void populateHeaderView(TextView textView, int columnNumber) {
        DataTableColumn column = columns.get(columnNumber);
        if (column == null)
            throw new IndexOutOfBoundsException();

        textView.setText(column.getHeader());
    }
}


