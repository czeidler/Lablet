/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.app.Activity;
import nz.ac.auckland.lablet.experiment.AbstractFileImportPlugin;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.File;
import java.io.IOException;


public class WavImportPlugin extends AbstractFileImportPlugin {
    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public String getFileFilter() {
        return ".*wav|.*WAV";
    }

    @Override
    protected String createUid(String importFileName) {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_HH-mm-ss", new java.util.Date());

        return dateString + "_Imported_Audio_" + importFileName;
    }

    @Override
    protected boolean importFile(File importFile, File dataStorageDir) {
        String fileName = importFile.getName();
        try {
            StorageLib.copyFile(importFile, new File(dataStorageDir, fileName), new StreamHelper.IProgressListener() {
                @Override
                public void onNewProgress(long totalProgress) {

                }
            }, 32 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        final AudioData sensorData = new AudioData();

        sensorData.setAudioFileName(fileName);
        try {
            sensorData.saveExperimentData(dataStorageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
