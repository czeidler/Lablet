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

    final private MarkerDataModel hCursorMarkerModel;
    final private MarkerDataModel vCursorMarkerModel;
    final private Unit xUnit = new Unit("s");
    final private Unit yUnit = new Unit("Hz");

    public FrequencyAnalysis(MicrophoneSensorData sensorData) {
        this.sensorData = sensorData;

        xUnit.setName("time");
        yUnit.setName("frequency");
        hCursorMarkerModel = new MarkerDataModel();
        vCursorMarkerModel = new MarkerDataModel();
    }

    public Unit getXUnit() {
        return xUnit;
    }

    public Unit getYUnit() {
        return yUnit;
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
        Bundle hCursorsBundle = bundle.getBundle("hCursors");
        if (hCursorsBundle != null)
            hCursorMarkerModel.fromBundle(hCursorsBundle);
        Bundle vCursorsBundle = bundle.getBundle("vCursors");
        if (vCursorsBundle != null)
            vCursorMarkerModel.fromBundle(vCursorsBundle);
        return true;
    }

    @Override
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
        Bundle analysisDataBundle = new Bundle();

        if (hCursorMarkerModel.getMarkerCount() > 0)
            analysisDataBundle.putBundle("hCursors", hCursorMarkerModel.toBundle());
        if (vCursorMarkerModel.getMarkerCount() > 0)
            analysisDataBundle.putBundle("vCursors", vCursorMarkerModel.toBundle());

        return analysisDataBundle;
    }

    @Override
    public void exportTagMarkerCSVData(OutputStream outputStream) throws IOException {

    }

    public MarkerDataModel getHCursorMarkerModel() {
        return hCursorMarkerModel;
    }

    public MarkerDataModel getVCursorMarkerModel() {
        return vCursorMarkerModel;
    }
}
