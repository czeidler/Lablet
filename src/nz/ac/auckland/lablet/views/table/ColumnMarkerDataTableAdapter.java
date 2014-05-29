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
import nz.ac.auckland.lablet.experiment.ExperimentAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


/**
 * Abstract base class for marker data table adapters.
 * <p>
 * See {@link nz.ac.auckland.lablet.views.table.ColumnMarkerDataTableAdapter} for an example.
 * </p>
 */
abstract class MarkerDataTableAdapter implements ITableAdapter<MarkerData>, MarkerDataModel.IMarkerDataModelListener,
        ExperimentAnalysis.IExperimentAnalysisListener {
    protected MarkerDataModel model;
    protected ExperimentAnalysis experimentAnalysis;
    private List<WeakReference<ITableAdapterListener>> listeners;

    public MarkerDataTableAdapter(MarkerDataModel model, ExperimentAnalysis experimentAnalysis) {
        this.model = model;
        model.addListener(this);
        listeners = new ArrayList<WeakReference<ITableAdapterListener>>();
        this.experimentAnalysis = experimentAnalysis;
        this.experimentAnalysis.addListener(this);
    }

    @Override
    protected void finalize() {
        model.removeListener(this);
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
        listeners.add(new WeakReference<ITableAdapterListener>(listener));
    }

    @Override
    public boolean removeListener(ITableAdapterListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void onDataAdded(MarkerDataModel model, int index) {
        notifyRowAdded(index);
    }

    @Override
    public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {
        notifyRowRemoved(index);
    }

    @Override
    public void onDataChanged(MarkerDataModel model, int index, int number) {
        notifyRowChanged(index, number);
    }

    @Override
    public void onAllDataChanged(MarkerDataModel model) {
        notifyAllRowsChanged();
    }

    @Override
    public void onDataSelected(MarkerDataModel model, int index) {
        notifyRowSelected(index);
    }

    private void notifyRowAdded(int row) {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onRowAdded(this, row);
            else
                it.remove();
        }
    }

    private void notifyRowRemoved(int row) {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onRowRemoved(this, row);
            else
                it.remove();
        }
    }

    private void notifyRowChanged(int row, int number) {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onRowUpdated(this, row + 1, number);
            else
                it.remove();
        }
    }

    private void notifyAllRowsChanged() {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onAllRowsUpdated(this);
            else
                it.remove();
        }
    }

    private void notifyRowSelected(int row) {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onRowSelected(this, row);
            else
                it.remove();
        }
    }

    @Override
    public void onUnitPrefixChanged() {
        for (ListIterator<WeakReference<ITableAdapterListener>> it = listeners.listIterator(); it.hasNext(); ) {
            ITableAdapterListener listener = it.next().get();
            if (listener != null)
                listener.onRowUpdated(this, 0, 1);
            else
                it.remove();
        }
    }

    @Override
    public void onShowCoordinateSystem(boolean show) {

    }
}

/**
 * Abstract base class for table columns.
 */
abstract class DataTableColumn {
    protected MarkerDataModel markerDataModel;
    protected ExperimentAnalysis experimentAnalysis;

    public void setMarkerDataModel(MarkerDataModel model) {
        this.markerDataModel = model;
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


/**
 * MarkerDataTableAdapter that can holds a set of {@link DataTableColumn}s.
 */
public class ColumnMarkerDataTableAdapter extends MarkerDataTableAdapter {
    private List<DataTableColumn> columns = new ArrayList<DataTableColumn>();

    public ColumnMarkerDataTableAdapter(MarkerDataModel model, ExperimentAnalysis experimentAnalysis) {
        super(model, experimentAnalysis);
    }

    public void addColumn(DataTableColumn column) {
        column.setMarkerDataModel(model);
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


