/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.graphics.PointF;
import android.os.Bundle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ExperimentAnalysis {
    interface IExperimentAnalysisListener {
        void onUnitPrefixChanged();
    }

    private Experiment experiment;

    private RunDataModel runDataModel;
    private Calibration calibration;

    private String xUnitPrefix = "";
    private String yUnitPrefix = "";

    private MarkersDataModel tagMarkers;
    private MarkersDataModel lengthCalibrationMarkers;

    private LengthCalibrationSetter lengthCalibrationSetter;

    private Bundle experimentSpecificData = null;

    private List<IExperimentAnalysisListener> listenerList = new ArrayList<IExperimentAnalysisListener>();

    public ExperimentAnalysis(Experiment experiment) {
        this.experiment = experiment;

        runDataModel = new RunDataModel();
        runDataModel.setNumberOfRuns(experiment.getNumberOfRuns());

        calibration = new Calibration();

        tagMarkers = new MarkersDataModel();
        tagMarkers.setCalibration(calibration);

        lengthCalibrationMarkers = new MarkersDataModel();
        MarkerData point1 = new MarkerData(-1);
        point1.setPosition(new PointF(10, 90));
        lengthCalibrationMarkers.addMarkerData(point1);
        MarkerData point2 = new MarkerData(-2);
        point2.setPosition(new PointF(30, 90));
        lengthCalibrationMarkers.addMarkerData(point2);
        lengthCalibrationSetter = new LengthCalibrationSetter(calibration, lengthCalibrationMarkers);
    }

    public Experiment getExperiment() { return  experiment; }
    public RunDataModel getRunDataModel() {
        return runDataModel;
    }
    public Calibration getCalibration() {
        return calibration;
    }
    public LengthCalibrationSetter getLengthCalibrationSetter() {
        return lengthCalibrationSetter;
    }
    public MarkersDataModel getTagMarkers() {
        return tagMarkers;
    }
    public MarkersDataModel getXYCalibrationMarkers() { return lengthCalibrationMarkers; }
    public Bundle getExperimentSpecificData() { return experimentSpecificData; }
    public void setExperimentSpecificData(Bundle data) {
        experimentSpecificData = data;
        onRunSpecificDataChanged();
    }
    public String getXUnitPrefix() {
        return xUnitPrefix;
    }

    public String getYUnitPrefix() {
        return yUnitPrefix;
    }

    public void addListener(IExperimentAnalysisListener listener) {
        listenerList.add(listener);
    }

    public void setXUnitPrefix(String xUnitPrefix) {
        this.xUnitPrefix = xUnitPrefix;
        notifyUnitPrefixChanged();
    }
    public void setYUnitPrefix(String yUnitPrefix) {
        this.yUnitPrefix = yUnitPrefix;
        notifyUnitPrefixChanged();
    }

    public String getXUnit() {
        return getXUnitPrefix() + experiment.getXUnit();
    }

    public String getYUnit() {
        return getYUnitPrefix() + experiment.getYUnit();
    }

    public Bundle analysisDataToBundle() {
        Bundle analysisDataBundle = new Bundle();

        analysisDataBundle.putInt("currentRun", runDataModel.getCurrentRun());

        if (tagMarkers.getMarkerCount() > 0) {
            Bundle tagMarkerBundle = new Bundle();
            int[] runIds = new int[tagMarkers.getMarkerCount()];
            float[] xPositions = new float[tagMarkers.getMarkerCount()];
            float[] yPositions = new float[tagMarkers.getMarkerCount()];
            for (int i = 0; i < tagMarkers.getMarkerCount(); i++) {
                MarkerData data = tagMarkers.getMarkerDataAt(i);
                runIds[i] = data.getRunId();
                xPositions[i] = data.getPosition().x;
                yPositions[i] = data.getPosition().y;
            }
            tagMarkerBundle.putIntArray("runIds", runIds);
            tagMarkerBundle.putFloatArray("xPositions", xPositions);
            tagMarkerBundle.putFloatArray("yPositions", yPositions);

            analysisDataBundle.putBundle("tagMarkers", tagMarkerBundle);
        }


        PointF point1 = lengthCalibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF point2 = lengthCalibrationMarkers.getMarkerDataAt(1).getPosition();
        analysisDataBundle.putFloat("lengthCalibrationPoint1x", point1.x);
        analysisDataBundle.putFloat("lengthCalibrationPoint1y", point1.y);
        analysisDataBundle.putFloat("lengthCalibrationPoint2x", point2.x);
        analysisDataBundle.putFloat("lengthCalibrationPoint2y", point2.y);
        analysisDataBundle.putFloat("lengthCalibrationValue", lengthCalibrationSetter.getCalibrationValue());

        analysisDataBundle.putString("xUnitPrefix", getXUnitPrefix());
        analysisDataBundle.putString("yUnitPrefix", getYUnitPrefix());

        if (experimentSpecificData != null)
            analysisDataBundle.putBundle("experiment_specific_data", experimentSpecificData);
        return analysisDataBundle;
    }

    protected boolean loadAnalysisData(Bundle bundle, File storageDir) {
        tagMarkers.clear();

        setExperimentSpecificData(bundle.getBundle("experiment_specific_data"));
        runDataModel.setCurrentRun(bundle.getInt("currentRun"));

        Bundle tagMarkerBundle = bundle.getBundle("tagMarkers");
        if (tagMarkerBundle != null) {
            tagMarkers.clear();
            int[] runIds = tagMarkerBundle.getIntArray("runIds");
            float[] xPositions = tagMarkerBundle.getFloatArray("xPositions");
            float[] yPositions = tagMarkerBundle.getFloatArray("yPositions");

            if (runIds != null && xPositions != null && yPositions != null && runIds.length == xPositions.length
                && xPositions.length == yPositions.length) {
                for (int i = 0; i < runIds.length; i++) {
                    MarkerData data = new MarkerData(runIds[i]);
                    data.getPosition().set(xPositions[i], yPositions[i]);
                    tagMarkers.addMarkerData(data);
                }
            }
        }

        PointF point1 = lengthCalibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF point2 = lengthCalibrationMarkers.getMarkerDataAt(1).getPosition();
        if (bundle.containsKey("lengthCalibrationPoint1x"))
            point1.x = bundle.getFloat("lengthCalibrationPoint1x");
        if (bundle.containsKey("lengthCalibrationPoint1y"))
            point1.y = bundle.getFloat("lengthCalibrationPoint1y");
        if (bundle.containsKey("lengthCalibrationPoint2x"))
            point2.x = bundle.getFloat("lengthCalibrationPoint2x");
        if (bundle.containsKey("lengthCalibrationPoint2y"))
            point2.y = bundle.getFloat("lengthCalibrationPoint2y");
        if (bundle.containsKey("lengthCalibrationValue"))
            lengthCalibrationSetter.setCalibrationValue(bundle.getFloat("lengthCalibrationValue"));

        if (bundle.containsKey("xUnitPrefix"))
            setXUnitPrefix(bundle.getString("xUnitPrefix"));
        if (bundle.containsKey("yUnitPrefix"))
            setYUnitPrefix(bundle.getString("yUnitPrefix"));

        return true;
    }

    public void exportTagMarkerCSVData(OutputStream outputStream) {
        try {
            String header = "id, x [" + getXUnit() + "], y [" + getYUnit()
                    + "], runValue\n";
            outputStream.write(header.getBytes());
            for (int i = 0; i < tagMarkers.getMarkerCount(); i++) {
                MarkerData markerData = tagMarkers.getMarkerDataAt(i);
                String string = "";
                string += markerData.getRunId();
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                PointF position = tagMarkers.getCalibratedMarkerPositionAt(i);
                string = "";
                string += position.x;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += position.y;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += experiment.getRunValueAt(i);
                outputStream.write(string.getBytes());

                outputStream.write("\n".getBytes());
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onRunSpecificDataChanged() {}

    private void notifyUnitPrefixChanged() {
        for (IExperimentAnalysisListener listener : listenerList)
            listener.onUnitPrefixChanged();
    }
}
