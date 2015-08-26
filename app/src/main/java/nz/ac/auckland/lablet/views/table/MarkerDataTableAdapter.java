/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.table;

import nz.ac.auckland.lablet.data.Data;
import nz.ac.auckland.lablet.data.PointDataList;
import nz.ac.auckland.lablet.misc.Unit;

import java.util.ArrayList;
import java.util.List;


public class MarkerDataTableAdapter extends ColumnDataTableAdapter {
    protected PointDataList model;

    public MarkerDataTableAdapter(PointDataList model) {
        this.model = model;
        model.addListener(markerListener);
    }

    public void release() {
        model.removeListener(markerListener);
    }

    @Override
    public void addColumn(DataTableColumn column) {
        column.setDataModel(model);
        super.addColumn(column);
    }

    @Override
    protected void finalize() {
        model.removeListener(markerListener);
    }

    @Override
    public int getColumnWeight(int column) {
        return 1;
    }

    @Override
    public void selectRow(int row) {
        model.selectData(row);
    }

    @Override
    public int getSelectedRow() {
        return model.getSelectedData();
    }

    private PointDataList.IListener markerListener = new PointDataList.IListener<PointDataList>() {
        @Override
        public void onDataAdded (PointDataList model,int index){
            notifyRowAdded(index + 1);
        }

        @Override
        public void onDataRemoved (PointDataList model,int index, Data data){
            notifyRowRemoved(index + 1);
        }

        @Override
        public void onDataChanged (PointDataList model,int index, int number){
            notifyRowChanged(index + 1, number);
        }

        @Override
        public void onAllDataChanged (PointDataList model){
            notifyAllRowsChanged();
        }

        @Override
        public void onDataSelected (PointDataList model,int index){
            notifyRowSelected(index);
        }
    };

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
            listener.onRowUpdated(this, row, number);
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
    public void onBaseExponentChanged() {
        dataModel.notifyAllDataChanged();
    }
}