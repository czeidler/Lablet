/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.*;


/**
 * Holds information about a run.
 */
class ExperimentRunInfo {
    private String description = "";
    private Bundle runInformation = new Bundle();

    final static private String DESCRIPTION_KEY = "description";
    final static private String RUN_INFO_KEY = "run_info";

    public String getDescription() {
        return description;
    }

    /**
     * Set a basic description of the run.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The returned bundle can be used to put some run specific information.
     *
     * @return a bundle, ready to store or read run information
     */
    public Bundle getRunInformation() {
        return runInformation;
    }

    /**
     * Save the runtime state.
     *
     * @param outState
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(DESCRIPTION_KEY, getDescription());
        outState.putBundle(RUN_INFO_KEY, getRunInformation());
    }

    /**
     * Restore a runtime state.
     *
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        description = savedInstanceState.getString(DESCRIPTION_KEY, "");
        Bundle bundle = savedInstanceState.getBundle(RUN_INFO_KEY);
        if (bundle != null)
            runInformation = bundle;
    }

    /**
     * Save run information (as a PersistentBundle) to file.
     *
     * @param file target file
     * @throws IOException
     */
    public void saveToFile(File file) throws IOException {
        Bundle bundle = new Bundle();
        onSaveInstanceState(bundle);

        FileWriter fileWriter = new FileWriter(file);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
        fileWriter.close();
    }

    /**
     * Loads a PersistentBundle from file and restores the run info.
     *
     * @param file where the run information is stored
     * @throws IOException
     */
    public void loadFromFile(File file) throws IOException {
        Bundle bundle = null;
        PersistentBundle persistentBundle = new PersistentBundle();
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            if (inStream!= null)
                inStream.close();
        }
        onRestoreInstanceState(bundle);
    }
}
