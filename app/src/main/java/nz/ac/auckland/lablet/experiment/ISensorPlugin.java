/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;


/**
 * Plugin interface for a sensor.
 */
public interface ISensorPlugin {
    /**
     * The identifier of the sensor.
     *
     * @return the identifier of the sensor
     */
    String getSensorName();

    /**
     * Creates an experiment run object.
     *
     * @return the experiment run
     */
    IExperimentSensor createExperimentSensor();
}

