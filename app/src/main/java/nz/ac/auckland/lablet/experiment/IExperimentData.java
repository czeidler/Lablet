package nz.ac.auckland.lablet.experiment;


import android.os.Bundle;

import java.io.File;
import java.io.IOException;

public interface IExperimentData {
    /**
     * The default file name where the experiment data is stored.
     * <p>
     * The data is first stored into a bundle and then transformed to xml using a
     * {@link nz.ac.auckland.lablet.misc.PersistentBundle}.
     * </p>
     */
    final static public String EXPERIMENT_DATA_FILE_NAME = "experiment_data.xml";

    public String getUid();
    public File getStorageDir();
    public String getDataType();

    /**
     * Load a previously conducted experiment from a Bundle and sets the storage directory.
     * <p>
     * The storage directory contains, for example, the video file from a camera experiment.
     * </p>
     * @param bundle the where all experiment information is stored
     * @param storageDir the storage directory of the experiment
     * @return
     */
    public boolean loadExperimentData(Bundle bundle, File storageDir) throws IOException;

    /**
     * /**
     * Saves the experiment to the path specified in {@see setStorageDir}.
     *
     * @param storageDir
     * @throws IOException
     */
    public void saveExperimentDataToFile(File storageDir) throws IOException;
}
