/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.Activity;
import nz.ac.auckland.lablet.experiment.AbstractFileImportPlugin;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.File;
import java.io.IOException;


public class VideoImportPlugin extends AbstractFileImportPlugin {
    @Override
    public String getName() {
        return "Video";
    }

    @Override
    public String getFileFilter() {
        return ".*mp4|.*MP4";
    }

    @Override
    protected String createUid(String importFileName) {
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        return dateString + "_Imported_Video_" + importFileName;
    }

    @Override
    protected boolean importFile(Activity activity, File importFile, String importUid, File dataStorageDir) {
        String fileName = importFile.getName();
        try {
            StorageLib.copyFile(importFile, new File(dataStorageDir, fileName), new StreamHelper.IProgressListener() {
                @Override
                public void onNewProgress(int totalProgress) {

                }
            }, 32 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        final VideoData videoData = new VideoData();
        videoData.setUid(importUid);

        videoData.setVideoFileName(dataStorageDir, fileName);
        try {
            videoData.saveExperimentDataToFile(dataStorageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
