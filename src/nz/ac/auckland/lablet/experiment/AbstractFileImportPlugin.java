/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.app.Activity;

import java.io.File;
import java.io.IOException;


public abstract class AbstractFileImportPlugin implements IImportPlugin {
    @Override
    public void importData(Activity activity, File importFile, File storageDir, IListener listener) {
        boolean result = importInternal(activity, importFile, storageDir);

        if (listener != null)
            listener.onImportFinished(result);
    }

    abstract protected String createUid(String fileName);

    abstract protected boolean importFile(Activity activity, File importFile, String importUid, File dataStorageDir);

    private boolean importInternal(final Activity activity, final File importFile, File storageBaseDir) {
        final String fileName = importFile.getName();
        final String uid = createUid(fileName);
        File mainDir = new File(storageBaseDir, uid);
        mainDir.mkdirs();

        Experiment experiment = new Experiment(activity);
        ImportExperimentRun experimentRun = new ImportExperimentRun(this);
        experiment.addExperimentRun(experimentRun);
        try {
            experiment.finishExperiment(true, mainDir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File dataStorageDir = experimentRun.getStorageDir();
        return importFile(activity, importFile, uid, dataStorageDir);
    }
}
