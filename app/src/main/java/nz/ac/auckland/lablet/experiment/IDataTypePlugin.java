/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;


/**
 * Plugin interface for a data type.
 */
public interface IDataTypePlugin {
    String getDataType();

    /**
     * Instantiates a new instance of the associated {@link ISensorData}.
     *
     * @return pointer to the data type sensor data
     */
    ISensorData instantiateData();
}
