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
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Abstract base class for experiments.
 */
abstract public class AbstractSensorData implements ISensorData {
    private String uid;
    protected Context context;

    private File storageDir;

    private IExperimentSensor sourceSensor;

    /**
     * Constructor to load an existing experiment.
     *
     * @param experimentContext the context of the experiment
     * @param bundle the experiment data that fits into a bundle
     * @param storageDir the storage directory of the experiment
     */
    public AbstractSensorData(Context experimentContext, Bundle bundle, File storageDir) {
        init(experimentContext);

        this.storageDir = storageDir;
        loadExperimentData(bundle, storageDir);
    }

    /**
     * Create a new experiment.
     *
     * @param experimentContext the experiment context
     */
    public AbstractSensorData(Context experimentContext, IExperimentSensor sourceSensor) {
        init(experimentContext);

        uid = generateNewUid();
        this.sourceSensor = sourceSensor;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public File getStorageDir() {
        return storageDir;
    }

    @Override
    public boolean loadExperimentData(Bundle bundle, File storageDir) {
        this.storageDir = storageDir;
        uid = bundle.getString("uid");
        return true;
    }

    @Override
    public void saveExperimentDataToFile(File storageDir) throws IOException {
        this.storageDir = storageDir;

        Bundle bundle = new Bundle();
        bundle.putString("sensor_name", sourceSensor.getIdentifier());
        Bundle experimentData = experimentDataToBundle();
        bundle.putBundle("data", experimentData);

        if (!storageDir.exists())
            storageDir.mkdir();
        onSaveAdditionalData(storageDir);

        // save the bundle
        File projectFile = new File(storageDir, EXPERIMENT_DATA_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    /**
     * Sub classes can override this method to store data that does not fit into a Bundle.
     *
     * This method is called from {@see saveExperimentDataToFile}.
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
        bundle.putString("uid", uid);

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

    private void init(Context experimentContext) {
        context = experimentContext;
    }
}
