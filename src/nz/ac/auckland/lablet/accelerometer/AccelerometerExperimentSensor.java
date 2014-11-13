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
import nz.ac.auckland.lablet.experiment.AbstractExperimentSensorView;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.views.plotview.*;

import java.io.*;


class AccelerometerExperimentView extends AbstractExperimentSensorView {
    final private AccelerometerExperimentSensor sensor;

    final private XYDataAdapter xData;
    final private XYDataAdapter yData;
    final private XYDataAdapter zData;
    final private XYDataAdapter totalData = new XYDataAdapter();
    final private AccelerometerSensorData.IListener dataListenerStrongRef;

    final private PlotView plotView;

    public AccelerometerExperimentView(Context context, AccelerometerExperimentSensor sensor) {
        super(context);

        this.sensor = sensor;

        final AccelerometerSensorData data = (AccelerometerSensorData)sensor.getExperimentData();
        dataListenerStrongRef = new AccelerometerSensorData.IListener() {
            @Override
            public void onDataAdded() {
                int index = data.size() - 1;
                final Number time = data.getTimeValues().get(index);
                final Number x = data.getXValues().get(index);
                final Number y = data.getYValues().get(index);
                final Number z = data.getZValues().get(index);

                totalData.addData(time.floatValue(), (float)Math.sqrt(Math.pow((float)x, 2) + Math.pow((float)y, 2)
                        + Math.pow((float)z, 2)));

                xData.notifyDataAdded(index, 1);
                yData.notifyDataAdded(index, 1);
                zData.notifyDataAdded(index, 1);
            }

            @Override
            public void onDataCleared() {
                // don't clear the x, y and z data because we don't own them
                xData.notifyAllDataChanged();
                yData.notifyAllDataChanged();
                zData.notifyAllDataChanged();
                totalData.clear();
            }
        };
        data.addListener(dataListenerStrongRef);

        plotView = new PlotView(context);
        plotView.getTitleView().setTitle("Accelerometer");
        plotView.getBackgroundPainter().setShowXGrid(true);
        plotView.getBackgroundPainter().setShowYGrid(true);
        resetView();
        StrategyPainter strategyPainter = new ThreadStrategyPainter();

        xData = new XYDataAdapter(data.getTimeValues(), data.getXValues());
        XYConcurrentPainter xPainter = new XYConcurrentPainter(xData);
        strategyPainter.addChild(xPainter);

        yData = new XYDataAdapter(data.getTimeValues(), data.getYValues());
        XYConcurrentPainter yPainter = new XYConcurrentPainter(yData);
        yPainter.setPointRenderer(new CircleRenderer());
        Paint yMarkerPaint = new Paint();
        yMarkerPaint.setColor(Color.BLUE);
        yPainter.getDrawConfig().setMarkerPaint(yMarkerPaint);
        strategyPainter.addChild(yPainter);

        zData = new XYDataAdapter(data.getTimeValues(), data.getZValues());
        XYConcurrentPainter zPainter = new XYConcurrentPainter(zData);
        Paint zMarkerPaint = new Paint();
        zMarkerPaint.setColor(Color.RED);
        zPainter.getDrawConfig().setMarkerPaint(zMarkerPaint);
        zPainter.setPointRenderer(new BottomTriangleRenderer());
        strategyPainter.addChild(zPainter);

        XYConcurrentPainter totalPainter = new XYConcurrentPainter(totalData);
        Paint totalMarkerPaint = new Paint();
        totalMarkerPaint.setColor(Color.WHITE);
        totalPainter.getDrawConfig().setMarkerPaint(totalMarkerPaint);
        totalPainter.setPointRenderer(new CircleRenderer());
        strategyPainter.addChild(totalPainter);

        plotView.addPlotPainter(strategyPainter);

        LegendPainter legend = new LegendPainter();
        legend.addEntry(xPainter, "x-acceleration");
        legend.addEntry(yPainter, "y-acceleration");
        legend.addEntry(zPainter, "z-acceleration");
        legend.addEntry(totalPainter, "total-acceleration");
        plotView.addForegroundPainter(legend);

        plotView.setAutoRange(PlotView.AUTO_RANGE_SCROLL, PlotView.AUTO_RANGE_ZOOM_EXTENDING);
        addView(plotView);
    }

    @Override
    public void onSettingsChanged() {

    }

    @Override
    public void onStartRecording() {
        super.onStartRecording();

        resetView();
    }

    private void resetView() {
        plotView.setXRange(0, 20000);
        plotView.setYRange(-0.5f, 0.5f);
    }
}

public class AccelerometerExperimentSensor extends AbstractExperimentSensor {
    private AccelerometerSensorData data;

    private SensorManager sensorManager;
    private long startTime = 0;

    final public static String SENSOR_NAME = "Accelerometer";

    @Override
    public String getSensorName() {
        return SENSOR_NAME;
    }

    @Override
    public View createExperimentView(Context context) {
        AccelerometerExperimentView view = new AccelerometerExperimentView(context, this);
        setListener(view);
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
                if (startTime == 0)
                    startTime = sensorEvent.timestamp;
                final long time = (sensorEvent.timestamp - startTime) / 1000000;

                data.addData(time, values);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void init(Activity activity) {
        data = new AccelerometerSensorData(activity, this);

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

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

        if (!saveData) {
            resetData();
            return;
        }

        data.saveExperimentDataToFile(storageDir);
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
        data.clear();

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
        return data;
    }
}
