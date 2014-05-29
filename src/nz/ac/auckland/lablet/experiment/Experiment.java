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
import android.text.format.Time;
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Abstract base class for experiments.
 */
abstract public class Experiment {
    private String uid;
    private File storageDir;
    protected Context context;

    final static public String EXPERIMENT_DATA_FILE_NAME = "experiment_data.xml";

    public Experiment(Context experimentContext, Bundle bundle, File storageDir) {
        init(experimentContext);

        loadExperiment(bundle, storageDir);
    }

    public Experiment(Context experimentContext) {
        init(experimentContext);

        uid = generateNewUid();
    }

    public String getXUnit() {
        return "";
    }

    public String getYUnit() {
        return "";
    }

    /** The raw data is stored in internal units starting at (0,0). These methods return the max values.
     * The max values ratio should be the same as the screen ratio.
     */
    abstract public float getMaxRawX();
    abstract public float getMaxRawY();

    protected boolean loadExperiment(Bundle bundle, File storageDir) {
        uid = bundle.getString("uid");
        this.storageDir = storageDir;
        return true;
    }

    /**
     * Saves the experiment to the path specified in {@see setStorageDir}.
     *
     * @throws IOException
     */
    public void saveExperimentDataToFile() throws IOException {
        Bundle bundle = new Bundle();
        bundle.putString("experiment_identifier", getIdentifier());
        Bundle experimentData = experimentDataToBundle();
        bundle.putBundle("data", experimentData);

        File dir = getStorageDir();
        if (!dir.exists())
            dir.mkdir();
        onSaveAdditionalData(dir);

        // save the bundle
        File projectFile = new File(getStorageDir(), EXPERIMENT_DATA_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    public void setStorageDir(File dir) {
        storageDir = dir;
    }
    public File getStorageDir() {
        return storageDir;
    }

    /**
     * Sub classes can override this method to store data that does not fit into a Bundle.
     *
     * This method is called from {@see saveExperimentDataToFile}.
     *
     * * @param dir directory where data can be stored
     */
    public void onSaveAdditionalData(File dir) {}

    /**
     * Here, derived classes can make their data persistent.
     *
     * If an experiment has data that does not fit into an Bundle use {@see onSaveAdditionalData} instead.
     *
     * @return a bundle containing the experiment data
     */
    public Bundle experimentDataToBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);

        return bundle;
    }

    /**
     * Gets the unique experiment identifier.
     *
     * @return the unique id of this experiment
     */
    public String getUid() {
        return uid;
    }

    protected String getIdentifier() {
        return this.getClass().getSimpleName();
    }

    abstract public int getNumberOfRuns();
    abstract public Bundle getRunAt(int i);
    abstract public float getRunValueAt(int i);
    abstract public String getRunValueBaseUnit();
    abstract public String getRunValueUnitPrefix();
    public String getRunValueUnit() {
        return getRunValueUnitPrefix() + getRunValueBaseUnit();
    }
    abstract public String getRunValueLabel();

    protected String generateNewUid() {
        String identifier = getIdentifier();

        Time now = new Time(Time.getCurrentTimezone());
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        now.setToNow();
        String newUid = "";
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
