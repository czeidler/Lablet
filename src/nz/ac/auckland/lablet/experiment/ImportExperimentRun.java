/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.io.File;
import java.io.IOException;


public class ImportExperimentRun extends ExperimentRun {
    final IImportPlugin importPlugin;
    File storageDir;

    public ImportExperimentRun(IImportPlugin importPlugin) {
        this.importPlugin = importPlugin;
    }

    public File getStorageDir() {
        return storageDir;
    }

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        super.finishExperiment(saveData, storageDir);

        this.storageDir = new File(storageDir, importPlugin.getName());
        this.storageDir.mkdirs();
    }
}
