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
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import nz.ac.auckland.lablet.experiment.AbstractExperimentRun;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;
import nz.ac.auckland.lablet.experiment.IExperimentRun;

import java.io.File;
import java.io.IOException;


public class AccelerometerExperimentRun extends AbstractExperimentRun {
    private XYPlot graphView2D;
    private SimpleXYSeries xData;

    private SensorManager sensorManager;

    @Override
    public View createExperimentView(Context context) {
        graphView2D = new XYPlot(context, "Accelerometer");

        xData = new SimpleXYSeries("x");

        graphView2D.addSeries(xData, new LineAndPointFormatter());
        return graphView2D;
    }

    @Override
    public boolean onPrepareOptionsMenu(MenuItem menuItem, IExperimentParent parent) {
        return false;
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = sensorEvent.values;
                // Movement
                float x = values[0];
                float y = values[1];
                float z = values[2];
                xData.addLast(System.currentTimeMillis(), x);
                graphView2D.invalidate();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    public void init(Activity activity, File experimentBaseDir) {
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
    public void finish(boolean discardExperiment) throws IOException {

    }

    @Override
    public void startPreview() {

    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void startRecording() throws Exception {

    }

    @Override
    public boolean stopRecording() {
        return true;
    }

    @Override
    public void startPlayback() {

    }

    @Override
    public void stopPlayback() {

    }

    @Override
    public ExperimentRunData getExperimentData() {
        return null;
    }
}
