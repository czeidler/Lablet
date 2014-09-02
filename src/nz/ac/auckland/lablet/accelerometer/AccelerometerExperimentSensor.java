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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import nz.ac.auckland.lablet.experiment.AbstractExperimentSensor;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.views.plotview.PlotView;
import nz.ac.auckland.lablet.views.plotview.XYDataAdapter;
import nz.ac.auckland.lablet.views.plotview.XYPainter;

import java.io.File;
import java.io.IOException;


public class AccelerometerExperimentSensor extends AbstractExperimentSensor {
    private XYDataAdapter xData = new XYDataAdapter();

    private SensorManager sensorManager;


    @Override
    public String getIdentifier() {
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

        XYPainter painter = new XYPainter();
        painter.setDataAdapter(xData);
        view.addPlotPainter(painter);

        view.setAutoRange(PlotView.AUTO_RANGE_SCROLL, PlotView.AUTO_RANGE_ZOOM_EXTENDING);
        //setListener(view);
        return view;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem) {
        return false;
    }

    final private long startTime = System.currentTimeMillis();

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = sensorEvent.values;
                // Movement
                float x = values[0];
                float y = values[1];
                float z = values[2];
                long time = System.currentTimeMillis() - startTime;
                xData.addData((float)time, x);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void init(Activity activity) {
        sensorManager = (SensorManager)activity.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
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

    }

    @Override
    public void startPreview() {
        super.startPreview();
    }

    @Override
    public void stopPreview() {
        super.stopPreview();
    }

    @Override
    public void startRecording() throws Exception {
        super.startRecording();
    }

    @Override
    public boolean stopRecording() {
        super.stopRecording();
        return true;
    }

    @Override
    public void startPlayback() {
        super.startPlayback();
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
    }

    @Override
    public SensorData getExperimentData() {
        return null;
    }
}
