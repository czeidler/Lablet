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
import nz.ac.auckland.lablet.experiment.SensorData;

import java.io.File;


public class AccelerometerSensorData extends SensorData {
    public AccelerometerSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        super(experimentContext, bundle, storageDir);
    }

    public AccelerometerSensorData(Context experimentContext) {
        super(experimentContext);
    }

    @Override
    public float getMaxRawX() {
        return 0;
    }

    @Override
    public float getMaxRawY() {
        return 0;
    }

    @Override
    public int getNumberOfRuns() {
        return 0;
    }

    @Override
    public Bundle getRunAt(int i) {
        return null;
    }

    @Override
    public float getRunValueAt(int i) {
        return 0;
    }

    @Override
    public String getRunValueBaseUnit() {
        return null;
    }

    @Override
    public String getRunValueUnitPrefix() {
        return null;
    }

    @Override
    public String getRunValueLabel() {
        return null;
    }
}
