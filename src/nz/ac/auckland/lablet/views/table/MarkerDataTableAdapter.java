/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.experiment.*;

import java.util.ArrayList;
import java.util.List;


public class MarkerDataTableAdapter extends ColumnDataTableAdapter implements MarkerDataModel.IListener,
        Unit.IListener {
    protected MarkerDataModel model;

    public MarkerDataTableAdapter(MarkerDataModel model) {
        this.model = model;
        model.addListener(this);
    }

    @Override
    public void addColumn(DataTableColumn column) {
        column.setDataModel(model);
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


abstract class UnitDataTableColumn extends DataTableColumn implements Unit.IListener {
    private List<Unit> units = new ArrayList<>();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        for (Unit unit : units)
            unit.removeListener(this);
    }

    protected void listenTo(Unit unit) {
        unit.addListener(this);
        units.add(unit);
    }

    @Override
    public void onPrefixChanged() {
        dataModel.notifyAllDataChanged();
    }
}