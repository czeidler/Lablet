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
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import nz.ac.auckland.lablet.experiment.AbstractExperimentRun;
import nz.ac.auckland.lablet.experiment.ExperimentRunData;

import java.io.IOException;
import java.lang.ref.WeakReference;


class SensorView extends XYPlot implements AccelerometerExperimentRun.ISensorDataListener {

    public SensorView(Context context, String s) {
        super(context, s);
    }

    @Override
    public void onDataUpdated() {
        invalidate();
    }

    public void setParent(AccelerometerExperimentRun parent) {
        parent.setDataListener(this);
    }
}

public class AccelerometerExperimentRun extends AbstractExperimentRun {
    private XYPlot graphView2D;
    private WeakReference<ISensorDataListener> softDataListener;

    private SimpleXYSeries xData = new SimpleXYSeries("x");;

    private SensorManager sensorManager;

    public void setDataListener(SensorView dataListener) {
        this.softDataListener = new WeakReference<ISensorDataListener>(dataListener);
    }

    public interface ISensorDataListener {
        public void onDataUpdated();
    }

    @Override
    public View createExperimentView(Context context) {
        SensorView view = new SensorView(context, "Accelerometer");
        view.setParent(this);
        view.addSeries(xData, new LineAndPointFormatter());
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
                float[] values = sensorEvent.values;
                // Movement
                float x = values[0];
                float y = values[1];
                float z = values[2];
                xData.addLast(System.currentTimeMillis(), x);

                if (softDataListener != null) {
                    ISensorDataListener dataListener = softDataListener.get();
                    if (dataListener != null)
                        dataListener.onDataUpdated();
                }
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
    public void finish(boolean discardExperiment) throws IOException {

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
    public ExperimentRunData getExperimentData() {
        return null;
    }
}
