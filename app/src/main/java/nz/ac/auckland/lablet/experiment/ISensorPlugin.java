/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.content.Context;
import android.os.Bundle;

import java.io.File;


/**
 * Plugin interface for a sensor.
 */
public interface ISensorPlugin {
    /**
     * The identifier of the sensor.
     *
     * @return the identifier of the sensor
     */
    public String getSensorName();

    /**
     * Creates an experiment run object.
     * <p>
     * The activity intent can hold the following options:
     * <ul>
     * <li>bundle field "options" -> options</li>
     * </ul>
     * </p>
     *
     * @return the experiment run
     */
    public IExperimentSensor createExperimentSensor();

    /**
     * Load an old experiment from a data bundle.
     *
     * @param context of the parent
     * @param data saved state of the experiment
     * @param storageDir directory where additional data may have been stored
     * @return the loaded experiment or null on failure
     */
    public ISensorData loadSensorData(Context context, Bundle data, File storageDir);
}

