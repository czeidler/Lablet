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
import nz.ac.auckland.lablet.camera.ITimeCalibration;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.misc.WeakListenable;

import java.util.ArrayList;
import java.util.List;


/**
 * MarkerDataTableAdapter that can holds a set of {@link DataTableColumn}s.
 */
abstract class ColumnDataTableAdapter extends WeakListenable<ITableAdapter.IListener>
        implements ITableAdapter<MarkerData> {

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
    public int getColumnCount() {
        return columns.size();
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


public class MarkerDataTableAdapter extends ColumnDataTableAdapter implements CalibratedMarkerDataModel.IListener,
        Unit.IListener {
    protected CalibratedMarkerDataModel model;
    protected ITimeCalibration timeCalibration;

    public MarkerDataTableAdapter(CalibratedMarkerDataModel model, ITimeCalibration timeCalibration) {
        this.model = model;
        this.timeCalibration = timeCalibration;
        model.addListener(this);
        CalibrationXY calibrationXY = model.getCalibrationXY();
        calibrationXY.getXUnit().addListener(this);
        calibrationXY.getYUnit().addListener(this);
        timeCalibration.getUnit().addListener(this);
    }

    @Override
    public void addColumn(DataTableColumn column) {
        column.setDataModel(model);
        column.setTimeCalibration(timeCalibration);
        super.addColumn(column);
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
    public void selectRow(int row) {
        model.selectMarkerData(row);
    }

    @Override
    public int getSelectedRow() {
        return model.getSelectedMarkerData();
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

    @Override
    public void onPrefixChanged() {
        for (IListener listener : getListeners())
            listener.onRowUpdated(this, 0, 1);
    }

    private void notifyRowAdded(int row) {
        for (IListener listener : getListeners())
            listener.onRowAdded(this, row);
    }

    private void notifyRowRemoved(int row) {
        for (IListener listener : getListeners())
            listener.onRowRemoved(this, row);
    }

    private void notifyRowChanged(int row, int number) {
        for (IListener listener : getListeners())
            listener.onRowUpdated(this, row + 1, number);
    }

    private void notifyAllRowsChanged() {
        for (IListener listener : getListeners())
            listener.onAllRowsUpdated(this);
    }

    private void notifyRowSelected(int row) {
        for (IListener listener : getListeners())
            listener.onRowSelected(this, row);
    }
}

/**
 * Abstract base class for table columns.
 */
abstract class DataTableColumn {
    protected CalibratedMarkerDataModel dataModel;
    protected ITimeCalibration timeCalibration;

    abstract public int size();
    abstract public Number getValue(int index);
    public String getStringValue(int index) {
        Number number = getValue(index);
        return String.format("%.2f", number.floatValue());
    }
    abstract public String getHeader();

    public void setDataModel(CalibratedMarkerDataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void setTimeCalibration(ITimeCalibration timeCalibration) {
        this.timeCalibration = timeCalibration;
    }
}



