/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.content.Context;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.AbstractSensorData;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;
import nz.ac.auckland.lablet.misc.WeakListenable;
import nz.ac.auckland.lablet.views.table.CSVWriter;
import nz.ac.auckland.lablet.views.table.ColumnDataTableAdapter;
import nz.ac.auckland.lablet.views.table.DataTableColumn;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class AccelerometerSensorData extends AbstractSensorData {
    private List<Number> timeValues = new ArrayList<>();
    private List<Number> xValues = new ArrayList<>();
    private List<Number> yValues = new ArrayList<>();
    private List<Number> zValues = new ArrayList<>();
    private WeakListenable<IListener> weakListenable = new WeakListenable<>();

    public interface IListener {
        void onDataAdded();
        void onDataCleared();
    }

    @Override
    public String getDataType() {
        return "Vector4D/accelerometer";
    }

    public AccelerometerSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);
    }

    public AccelerometerSensorData(Context experimentContext, IExperimentSensor sourceSensor) {
        super(experimentContext, sourceSensor);
    }

    class DataColumn<T> extends DataTableColumn {
        final protected List<T> data;
        final private String header;

        public DataColumn(List<T> data, String header) {
            this.data = data;
            this.header = header;
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public String getHeader() {
            return header;
        }

        @Override
        public Number getValue(int index) {
            return (Number)data.get(index);
        }
    }

    @Override
    public void saveExperimentDataToFile(File storageDir) throws IOException {
        super.saveExperimentDataToFile(storageDir);

        ColumnDataTableAdapter dataTableAdapter = new ColumnDataTableAdapter();
        dataTableAdapter.addColumn(new DataColumn(timeValues, "time [ms]"));
        dataTableAdapter.addColumn(new DataColumn(xValues, "x-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new DataColumn(yValues, "y-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new DataColumn(zValues, "z-acceleration [m/s^2]"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(storageDir, "data.csv"))));
        CSVWriter.writeTable(dataTableAdapter, writer, ',');
    }

    public void addListener(IListener listener) {
        weakListenable.addListener(listener);
    }

    public void removeListener(IListener listener) {
        weakListenable.removeListener(listener);
    }

    private void notifyDataAdded() {
        for (IListener listener : weakListenable.getListeners())
            listener.onDataAdded();
    }

    private void notifyDataCleared() {
        for (IListener listener : weakListenable.getListeners())
            listener.onDataCleared();
    }

    public int size() {
        return timeValues.size();
    }

    public void addData(long time, float[] data) {
        if (data.length != 3)
            throw new IllegalArgumentException();

        timeValues.add(time);
        xValues.add(data[0]);
        yValues.add(data[1]);
        zValues.add(data[2]);
        notifyDataAdded();
    }

    public void clear() {
        timeValues.clear();
        xValues.clear();
        yValues.clear();
        zValues.clear();
        notifyDataCleared();
    }

    public List<Number> getTimeValues() {
        return timeValues;
    }

    public List<Number> getXValues() {
        return xValues;
    }

    public List<Number> getYValues() {
        return yValues;
    }

    public List<Number> getZValues() {
        return zValues;
    }
}
