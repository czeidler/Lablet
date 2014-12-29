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
}

