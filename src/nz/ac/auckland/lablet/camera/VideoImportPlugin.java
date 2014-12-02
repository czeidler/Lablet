/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import nz.ac.auckland.lablet.experiment.Experiment;
import nz.ac.auckland.lablet.experiment.ExperimentRun;
import nz.ac.auckland.lablet.experiment.IImportPlugin;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.File;
import java.io.IOException;


class ImportExperimentRun extends ExperimentRun {
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

public class VideoImportPlugin implements IImportPlugin {
    @Override
    public String getName() {
        return "Video";
    }

    @Override
    public String getFileFilter() {
        return ".*mp4|.*MP4";
    }

    @Override
    public void importData(Activity activity, File importFile, File storageDir, IListener listener) {
        boolean result = importInternal(activity, importFile, storageDir);

        if (listener != null)
            listener.onImportFinished(result);
    }

    private String createUid(String importFileName) {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        return dateString + "_Imported_Video_" + importFileName;
    }

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

        File storageDir = experimentRun.getStorageDir();
        try {
            StorageLib.copyFile(importFile, new File(storageDir, fileName), new StreamHelper.IProgressListener() {
                @Override
                public void onNewProgress(int totalProgress) {

                }
            }, 32 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        final CameraSensorData cameraSensorData = new CameraSensorData(activity);
        cameraSensorData.setUid(uid);

        cameraSensorData.setVideoFileName(storageDir, fileName);
        try {
            cameraSensorData.saveExperimentDataToFile(storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
