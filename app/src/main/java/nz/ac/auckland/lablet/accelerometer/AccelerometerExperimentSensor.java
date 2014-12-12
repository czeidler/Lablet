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
import nz.ac.auckland.lablet.experiment.IExperimentData;

import java.io.*;


public class AccelerometerExperimentSensor extends AbstractExperimentSensor {
    private AccelerometerExperimentData data;

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
        data = new AccelerometerExperimentData(activity, this);

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
    public IExperimentData getExperimentData() {
        return data;
    }
}
