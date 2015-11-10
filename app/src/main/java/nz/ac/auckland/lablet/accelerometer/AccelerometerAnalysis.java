/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.IDataAnalysis;
import nz.ac.auckland.lablet.experiment.ISensorData;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;

import java.io.File;
import java.io.IOException;
import java.io.Writer;


/**
 * Contains all data relevant for the accelerometer analysis.
 */
public class AccelerometerAnalysis implements IDataAnalysis {
    final private AccelerometerSensorData sensorData;
    final private DisplaySettings displaySettings = new DisplaySettings();

    final private Calibration xCalibration = new Calibration();
    final private Calibration yCalibration = new Calibration();
    final private Calibration zCalibration = new Calibration();
    final private Calibration totalCalibration = new Calibration();

    class Calibration {
        final private MarkerDataModel baseLineMarker = new MarkerDataModel();

        public Calibration() {
            MarkerData markerData = new MarkerData(0);
            markerData.setPosition(new PointF(0f, 9.81f));
            baseLineMarker.addMarkerData(markerData);
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putBundle("baseLineMarker", baseLineMarker.toBundle());

            return bundle;
        }

        public void fromBundle(Bundle bundle) {
            Bundle baseLineMarkerBundle = bundle.getBundle("baseLineMarker");
            if (baseLineMarkerBundle != null)
                baseLineMarker.fromBundle(baseLineMarkerBundle);
        }

        public MarkerDataModel getBaseLineMarker() {
            return baseLineMarker;
        }

        public float getBaseLine() {
            return baseLineMarker.getRealMarkerPositionAt(0).y;
        }

        public void setBaseLine(float baseLine) {
            baseLineMarker.setMarkerPosition(new PointF(0, baseLine), 0);
        }
    }

    public class DisplaySettings {
        final RectF range = new RectF();

        public void setRange(RectF range) {
            this.range.set(range);
        }

        public RectF getRange() {
            return range;
        }

        public Bundle toBundle() {
            Bundle bundle = new Bundle();
            bundle.putFloat("rangeLeft", range.left);
            bundle.putFloat("rangeTop", range.top);
            bundle.putFloat("rangeRight", range.right);
            bundle.putFloat("rangeBottom", range.bottom);
            return bundle;
        }

        public void fromBundle(Bundle bundle) {
            range.left = bundle.getFloat("rangeLeft", range.left);
            range.top = bundle.getFloat("rangeTop", range.top);
            range.right = bundle.getFloat("rangeRight", range.right);
            range.bottom = bundle.getFloat("rangeBottom", range.bottom);
        }
    }

    public AccelerometerAnalysis(AccelerometerSensorData sensorData) {
        this.sensorData = sensorData;
    }

    public Calibration getXCalibration() {
        return xCalibration;
    }

    public Calibration getYCalibration() {
        return yCalibration;
    }

    public Calibration getZCalibration() {
        return zCalibration;
    }

    public Calibration getTotalCalibration() {
        return totalCalibration;
    }

    @Override
    public String getDisplayName() {
        return "Accelerometer Analysis";
    }

    @Override
    public String getIdentifier() {
        return "AccelerometerAnalysis";
    }

    public AccelerometerSensorData getAccelerometerData() {
        return sensorData;
    }

    @Override
    public ISensorData[] getData() {
        return new ISensorData[]{sensorData};
    }

    @Override
    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        Bundle displaySettingsBundle = bundle.getBundle("displaySettings");
        if (displaySettingsBundle != null)
            displaySettings.fromBundle(displaySettingsBundle);

        Bundle xCalibrationBundle = bundle.getBundle("xCalibration");
        if (xCalibrationBundle != null)
            xCalibration.fromBundle(xCalibrationBundle);
        Bundle yCalibrationBundle = bundle.getBundle("yCalibration");
        if (yCalibrationBundle != null)
            yCalibration.fromBundle(yCalibrationBundle);
        Bundle zCalibrationBundle = bundle.getBundle("zCalibration");
        if (zCalibrationBundle != null)
            zCalibration.fromBundle(zCalibrationBundle);
        Bundle totalCalibrationBundle = bundle.getBundle("totalCalibration");
        if (totalCalibrationBundle != null)
            totalCalibration.fromBundle(totalCalibrationBundle);

        return true;
    }

    @Override
    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException {
        Bundle bundle = new Bundle();
        bundle.putBundle("displaySettings", getDisplaySettings().toBundle());

        bundle.putBundle("xCalibration", xCalibration.toBundle());
        bundle.putBundle("yCalibration", yCalibration.toBundle());
        bundle.putBundle("zCalibration", zCalibration.toBundle());
        bundle.putBundle("totalCalibration", totalCalibration.toBundle());

        return bundle;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) throws IOException {

    }

    public DisplaySettings getDisplaySettings() {
        return displaySettings;
    }
}
