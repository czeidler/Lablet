/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;

import java.io.File;
import java.io.IOException;


/**
 * The ISensorData interface allows to save and load experiment data.
 */
public interface ISensorData {
    /**
     * The default file name where the experiment data is stored.
     * <p>
     * The data is first stored into a bundle and then transformed to xml using a
     * {@link nz.ac.auckland.lablet.misc.PersistentBundle}.
     * </p>
     */
    String EXPERIMENT_DATA_FILE_NAME = "experiment_data.xml";

    /**
     * Only returns a valid file if {load, save}ExperimentData has been called.
     *
     * @return the storage directory of the sensor data.
     */
    File getStorageDir();

    /**
     * The data type is a unique string describing the data.
     *
     * @return the data type string.
     */
    String getDataType();

    /**
     * Load a previously conducted experiment from a Bundle and sets the storage directory.
     * <p>
     * The storage directory contains, for example, the video file from a camera experiment.
     * </p>
     * @param bundle the where all experiment information is stored
     * @param storageDir the storage directory of the experiment
     * @return
     */
    boolean loadExperimentData(Bundle bundle, File storageDir) throws IOException;

    /**
     * Saves the experiment to the path specified. The default file name is EXPERIMENT_DATA_FILE_NAME.
     *
     * @param storageDir
     * @throws IOException
     */
    void saveExperimentData(File storageDir) throws IOException;
}
