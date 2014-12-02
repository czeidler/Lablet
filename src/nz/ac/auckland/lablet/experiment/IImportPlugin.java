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


public interface IImportPlugin {
    public interface IListener {
        void onImportFinished(boolean successful);
    }

    String getName();
    String getFileFilter();
    void importData(Activity activity, File importFile, File storageDir, IListener listener);
}
