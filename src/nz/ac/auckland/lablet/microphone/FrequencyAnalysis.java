/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.graphics.PointF;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.experiment.SensorData;
import nz.ac.auckland.lablet.misc.PersistentBundle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;


public class FrequencyAnalysis implements ISensorAnalysis {
    final private MicrophoneSensorData sensorData;

    final private MarkerDataModel tagMarkerModel = new MarkerDataModel();

    public FrequencyAnalysis(MicrophoneSensorData sensorData) {
        this.sensorData = sensorData;
    }

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public SensorData getData() {
        return sensorData;
    }

    @Override
    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        Bundle tagMarkerBundle = bundle.getBundle("tagMarkers");
        if (tagMarkerBundle != null)
            tagMarkerModel.fromBundle(tagMarkerBundle);
        return true;
    }

    @Override
    public void saveAnalysisData(File directory) throws IOException {
        Bundle bundle = new Bundle();
        Bundle experimentData = analysisDataToBundle();
        bundle.putBundle("analysis_data", experimentData);

        // save the bundle
        File projectFile = new File(directory, EXPERIMENT_ANALYSIS_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    private Bundle analysisDataToBundle() {
        Bundle analysisDataBundle = new Bundle();

        if (tagMarkerModel.getMarkerCount() > 0) {
            Bundle tagMarkerBundle = tagMarkerModel.toBundle();
            analysisDataBundle.putBundle("tagMarkers", tagMarkerBundle);
        }

        return analysisDataBundle;
    }

    @Override
    public void exportTagMarkerCSVData(OutputStream outputStream) throws IOException {

    }

    public MarkerDataModel getTagMarkerModel() {
        return tagMarkerModel;
    }
}
