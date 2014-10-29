/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentSensor;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.views.plotview.*;
import nz.ac.auckland.lablet.views.table.CSVWriter;
import nz.ac.auckland.lablet.views.table.ColumnDataTableAdapter;
import nz.ac.auckland.lablet.views.table.DataTableColumn;

import java.io.*;


public class AccelerometerExperimentSensor extends AbstractExperimentSensor {
    private XYDataAdapter xData = new XYDataAdapter();
    private XYDataAdapter yData = new XYDataAdapter();
    private XYDataAdapter zData = new XYDataAdapter();
    private XYDataAdapter totalData = new XYDataAdapter();

    private SensorManager sensorManager;
    private long startTime = 0;

    @Override
    public String getSensorName() {
        return getClass().getSimpleName();
    }

    @Override
    public View createExperimentView(Context context) {
        PlotView view = new PlotView(context);
        view.getTitleView().setTitle("Accelerometer");
        view.setXRange(0, 20000);
        view.setYRange(-0.5f, 0.5f);
        view.getBackgroundPainter().setShowXGrid(true);
        view.getBackgroundPainter().setShowYGrid(true);

        XYPainter xPainter = new XYPainter();
        xPainter.setDataAdapter(xData);
        view.addPlotPainter(xPainter);

        XYPainter yPainter = new XYPainter();
        yPainter.setPointRenderer(new CircleRenderer());
        Paint yMarkerPaint = new Paint();
        yMarkerPaint.setColor(Color.BLUE);
        yPainter.getDrawConfig().setMarkerPaint(yMarkerPaint);
        yPainter.setDataAdapter(yData);
        view.addPlotPainter(yPainter);

        XYPainter zPainter = new XYPainter();
        Paint zMarkerPaint = new Paint();
        zMarkerPaint.setColor(Color.RED);
        zPainter.getDrawConfig().setMarkerPaint(zMarkerPaint);
        zPainter.setPointRenderer(new BottomTriangleRenderer());
        zPainter.setDataAdapter(zData);
        view.addPlotPainter(zPainter);

        XYPainter totalPainter = new XYPainter();
        Paint totalMarkerPaint = new Paint();
        totalMarkerPaint.setColor(Color.WHITE);
        totalPainter.getDrawConfig().setMarkerPaint(totalMarkerPaint);
        totalPainter.setPointRenderer(new CircleRenderer());
        totalPainter.setDataAdapter(totalData);
        view.addPlotPainter(totalPainter);

        LegendPainter legend = new LegendPainter();
        legend.addEntry(xPainter, "x-acceleration");
        legend.addEntry(yPainter, "y-acceleration");
        legend.addEntry(zPainter, "z-acceleration");
        legend.addEntry(totalPainter, "total-acceleration");
        view.addForegroundPainter(legend);

        view.setAutoRange(PlotView.AUTO_RANGE_SCROLL, PlotView.AUTO_RANGE_ZOOM_EXTENDING);
        //setListener(view);
        return view;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem) {
        return false;
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                final float[] values = sensorEvent.values;
                final float x = values[0];
                final float y = values[1];
                final float z = values[2];
                if (startTime == 0)
                    startTime = sensorEvent.timestamp;
                final long time = (sensorEvent.timestamp - startTime) / 1000000;
                xData.addData((float)time, x);
                yData.addData((float)time, y);
                zData.addData((float)time, z);

                totalData.addData((float)time, (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void init(Activity activity) {
        sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void destroy() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    abstract class DataColunm extends DataTableColumn {
        final protected XYDataAdapter data;
        final private String header;

        public DataColunm(XYDataAdapter data, String header) {
            this.data = data;
            this.header = header;
        }

        @Override
        public int size() {
            return data.getSize();
        }

        @Override
        public String getHeader() {
            return header;
        }
    }

    class XDataColumn extends DataColunm {
        public XDataColumn(XYDataAdapter data, String header) {
            super(data, header);
        }

        @Override
        public Number getValue(int index) {
            return data.getX(index);
        }
    }

    class YDataColumn extends DataColunm {
        public YDataColumn(XYDataAdapter data, String header) {
            super(data, header);
        }

        @Override
        public Number getValue(int index) {
            return data.getY(index);
        }
    }

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

        if (!saveData) {
            resetData();
            return;
        }

        ColumnDataTableAdapter dataTableAdapter = new ColumnDataTableAdapter();
        dataTableAdapter.addColumn(new XDataColumn(xData, "time [ms]"));
        dataTableAdapter.addColumn(new YDataColumn(xData, "x-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new YDataColumn(yData, "y-acceleration [m/s^2]"));
        dataTableAdapter.addColumn(new YDataColumn(zData, "z-acceleration [m/s^2]"));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(storageDir, "data.csv"))));
        CSVWriter.writeTable(dataTableAdapter, writer, ',');
    }

    @Override
    public void startPreview() {
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        super.startPreview();
    }

    @Override
    public void stopPreview() {
        super.stopPreview();
    }

    @Override
    public void startRecording() throws Exception {
        super.startRecording();

        resetData();
    }

    private void resetData() {
        xData.clear();
        yData.clear();
        zData.clear();
        totalData.clear();

        startTime = 0;
    }

    @Override
    public boolean stopRecording() {
        sensorManager.unregisterListener(sensorEventListener);

        super.stopRecording();
        return true;
    }

    @Override
    public void startPlayback() {
        super.startPlayback();
    }

    @Override
    public void stopPlayback() {
        resetData();

        super.stopPlayback();
    }

    @Override
    public ISensorData getExperimentData() {
        return null;
    }
}
