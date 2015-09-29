/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.io.File;


/**
 * Data import plugin interface. Imports data from a data file.
 */
public interface IImportPlugin {
    /**
     * Listener interface for the import plugin.
     */
    interface IListener {
        void onImportFinished(boolean successful);
    }

    /**
     * Gets a human readable name of the plugin.
     *
     * @return name of the plugin
     */
    String getName();

    /**
     * Returns a filter of what files can be imported.
     *
     * @return a filter string
     */
    String getFileFilter();

    /**
     * Imports file from a source file.
     *
     * @param importFile the file that should be imported.
     * @param storageDir the base storage dir where the data should be imported
     * @param listener for the import
     */
    void importData(File importFile, File storageDir, IListener listener);
}
