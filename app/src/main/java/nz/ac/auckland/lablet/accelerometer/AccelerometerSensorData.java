/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.os.Bundle;
import au.com.bytecode.opencsv.CSV;
import au.com.bytecode.opencsv.CSVReadProc;
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
        void onDataAdded(long time, float[] data);
        void onDataCleared();
    }

    static final public String DATA_TYPE = "Vector4D/Accelerometer";

    @Override
    public String getDataType() {
        return DATA_TYPE;
    }

    public AccelerometerSensorData() {
        super();
    }

    public AccelerometerSensorData(IExperimentSensor sourceSensor) {
        super(sourceSensor);
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

    class LongDataColumn<T> extends DataColumn<T> {
        public LongDataColumn(List<T> data, String header) {
            super(data, header);
        }

        @Override
        public String getStringValue(int index) {
            return Long.toString(getValue(index).longValue());
        }
    }

    @Override
    public void saveExperimentDataToFile(File storageDir) throws IOException {
        super.saveExperimentDataToFile(storageDir);

        ColumnDataTableAdapter dataTableAdapter = new ColumnDataTableAdapter();
        dataTableAdapter.addColumn(new LongDataColumn(timeValues, "time [ms]"));
        dataTableAdapter.addColumn(new DataColumn(xValues, "x-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new DataColumn(yValues, "y-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new DataColumn(zValues, "z-acceleration [m/s^2]"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(storageDir, "data.csv"))));
        CSVWriter.writeTable(dataTableAdapter, writer, ',');
        writer.close();
    }

    @Override
    public boolean loadExperimentData(Bundle bundle, File storageDir) {
        clear();
        final float[] dataBuffer = new float[3];
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(storageDir, "data.csv")));
            CSV csv = CSV.separator(',').quote('"').create();
            csv.read(inputStream, new CSVReadProc() {
                @Override
                public void procRow(int rowIndex, String... strings) {
                    // ignore header
                    if (rowIndex ==  0)
                        return;
                    if (strings.length != 4)
                        return;
                    long time = Long.parseLong(strings[0]);
                    dataBuffer[0] = Float.parseFloat(strings[1]);
                    dataBuffer[1] = Float.parseFloat(strings[2]);
                    dataBuffer[2] = Float.parseFloat(strings[3]);
                    addData(time, dataBuffer);
                }
            });
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return super.loadExperimentData(bundle, storageDir);
    }

    public void addListener(IListener listener) {
        weakListenable.addListener(listener);
    }

    public void removeListener(IListener listener) {
        weakListenable.removeListener(listener);
    }

    private void notifyDataAdded(long time, float[] data) {
        for (IListener listener : weakListenable.getListeners())
            listener.onDataAdded(time, data);
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
        notifyDataAdded(time, data);
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
