/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Abstract base class for experiments.
 */
abstract public class AbstractSensorData implements ISensorData {
    private File storageDir;

    private IExperimentSensor sourceSensor;

    static public String DATA_KEY = "data";
    static public String DATA_TYPE_KEY = "data_type";
    static public String SENSOR_NAME_KEY = "sensor_name";

    /**
     * Constructor to create an existing experiment that is loaded with loadExperimentData.
     *
     * loadExperimentData is not called directly here in the constructor because its not a good idea to call virtual
     * methods, i.e., the derived is constructor is not finished before the derived method is called.
     */
    public AbstractSensorData() {
    }

    /**
     * Create data to be filled by an experiment sensor.
     */
    public AbstractSensorData(IExperimentSensor sourceSensor) {
        this.sourceSensor = sourceSensor;
    }

    @Override
    public File getStorageDir() {
        return storageDir;
    }

    @Override
    public boolean loadExperimentData(Bundle bundle, File storageDir) {
        this.storageDir = storageDir;
        return true;
    }

    @Override
    public void saveExperimentData(File storageDir) throws IOException {
        this.storageDir = storageDir;

        Bundle bundle = new Bundle();
        if (sourceSensor != null)
            bundle.putString(SENSOR_NAME_KEY, sourceSensor.getSensorName());
        Bundle experimentData = experimentDataToBundle();
        bundle.putBundle(DATA_KEY, experimentData);

        if (!storageDir.exists())
            storageDir.mkdir();
        onSaveAdditionalData(storageDir);

        // save the bundle
        File projectFile = new File(storageDir, EXPERIMENT_DATA_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
        fileWriter.close();
    }

    /**
     * Sub classes can override this method to store data that does not fit into a Bundle.
     *
     * This method is called from {@see saveExperimentData}.
     *
     * * @param dir directory where data can be stored
     */
    protected void onSaveAdditionalData(File dir) {}

    /**
     * Here, derived classes can make their data persistent.
     *
     * If an experiment has data that does not fit into an Bundle use {@see onSaveAdditionalData} instead.
     *
     * @return a bundle containing the experiment data
     */
    protected Bundle experimentDataToBundle() {
        Bundle bundle = new Bundle();
        bundle.putString(DATA_TYPE_KEY, getDataType());

        return bundle;
    }

    protected String getIdentifier() {
        return this.getClass().getSimpleName();
    }

    protected String generateNewUid() {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        String newUid = "";
        String identifier = getIdentifier();
        if (!identifier.equals("")) {
            newUid += identifier;
            newUid += "_";
        }
        newUid += dateString;
        return newUid;
    }
}
