/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


public class FrequencyAnalysis implements ISensorAnalysis {
    final private MicrophoneSensorData sensorData;

    final private CalibratedMarkerDataModel tagMarkerModel;
    final private Unit xUnit = new Unit("s");
    final private Unit yUnit = new Unit("Hz");
    final private CalibrationXY calibrationXY;

    public FrequencyAnalysis(MicrophoneSensorData sensorData) {
        this.sensorData = sensorData;

        calibrationXY = new CalibrationXY(xUnit, yUnit);
        tagMarkerModel = new CalibratedMarkerDataModel(calibrationXY);
        tagMarkerModel.setCalibrationXY(calibrationXY);
    }

    @Override
    public String getIdentifier() {
        return getClass().getSimpleName();
    }

    @Override
    public ISensorData getData() {
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
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
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

    public CalibratedMarkerDataModel getTagMarkerModel() {
        return tagMarkerModel;
    }
}
