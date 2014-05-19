/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.experiment;

import android.graphics.PointF;
import android.os.Bundle;
import nz.ac.aucklanduni.physics.tracker.misc.PersistentBundle;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ExperimentAnalysis {
    public interface IExperimentAnalysisListener {
        void onUnitPrefixChanged();
        void onShowCoordinateSystem(boolean show);
    }

    final public static String EXPERIMENT_ANALYSIS_FILE_NAME = "experiment_analysis.xml";

    private Experiment experiment;

    private RunDataModel runDataModel;
    private Calibration calibration;

    private String xUnitPrefix = "";
    private String yUnitPrefix = "";

    private MarkersDataModel tagMarkers;
    private MarkersDataModel lengthCalibrationMarkers;
    private MarkersDataModel originMarkers;

    private LengthCalibrationSetter lengthCalibrationSetter;
    private OriginCalibrationSetter originCalibrationSetter;
    private boolean showCoordinateSystem = true;

    private Bundle experimentSpecificData = null;

    private List<IExperimentAnalysisListener> listenerList = new ArrayList<IExperimentAnalysisListener>();

    public ExperimentAnalysis(Experiment experiment) {
        this.experiment = experiment;

        runDataModel = new RunDataModel();
        runDataModel.setNumberOfRuns(experiment.getNumberOfRuns());

        calibration = new Calibration();

        tagMarkers = new MarkersDataModel();
        tagMarkers.setCalibration(calibration);

        float maxXValue = experiment.getMaxRawX();
        float maxYValue = experiment.getMaxRawY();
        lengthCalibrationMarkers = new MarkersDataModel();
        MarkerData point1 = new MarkerData(-1);
        point1.setPosition(new PointF(maxXValue * 0.1f, maxYValue * 0.9f));
        lengthCalibrationMarkers.addMarkerData(point1);
        MarkerData point2 = new MarkerData(-2);
        point2.setPosition(new PointF(maxXValue * 0.3f, maxYValue * 0.9f));
        lengthCalibrationMarkers.addMarkerData(point2);
        lengthCalibrationSetter = new LengthCalibrationSetter(calibration, lengthCalibrationMarkers);

        PointF origin = calibration.getOrigin();
        PointF axis1 = calibration.getAxis1();
        originMarkers = new MarkersDataModel();
        // y-axis
        point1 = new MarkerData(-1);
        point1.setPosition(new PointF(10, 10));
        originMarkers.addMarkerData(point1);
        // x-axis
        point2 = new MarkerData(-2);
        point2.setPosition(new PointF(axis1.x, axis1.y));
        originMarkers.addMarkerData(point2);
        // origin
        MarkerData point3 = new MarkerData(-3);
        point3.setPosition(origin);
        originMarkers.addMarkerData(point3);
        originCalibrationSetter = new OriginCalibrationSetter(calibration, originMarkers);
    }

    protected void setOrigin(PointF origin, PointF axis1) {
        originCalibrationSetter.setOrigin(origin, axis1);
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
    public MarkersDataModel getOriginMarkers(){
        return originMarkers;
    }
    public Bundle getExperimentSpecificData() { return experimentSpecificData; }
    public void setShowCoordinateSystem(boolean show) {
        showCoordinateSystem = show;
        notifyShowCoordinateSystem(show);
    }
    public boolean getShowCoordinateSystem() {
        return showCoordinateSystem;
    }
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

    public boolean removeListener(IExperimentAnalysisListener listener) {
        return listenerList.remove(listener);
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

        analysisDataBundle.putFloat("originX", calibration.getOrigin().x);
        analysisDataBundle.putFloat("originY", calibration.getOrigin().y);
        analysisDataBundle.putFloat("originAxis1x", calibration.getAxis1().x);
        analysisDataBundle.putFloat("originAxis1y", calibration.getAxis1().y);
        analysisDataBundle.putBoolean("originSwapAxis", calibration.getSwapAxis());
        analysisDataBundle.putBoolean("showCoordinateSystem", showCoordinateSystem);

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

        PointF origin = calibration.getOrigin();
        PointF axis1 = calibration.getAxis1();
        boolean swapAxis = false;
        if (bundle.containsKey("originX"))
            origin.x = bundle.getFloat("originX");
        if (bundle.containsKey("originY"))
            origin.y = bundle.getFloat("originY");
        if (bundle.containsKey("originAxis1x"))
            axis1.x = bundle.getFloat("originAxis1x");
        if (bundle.containsKey("originAxis1y"))
            axis1.y = bundle.getFloat("originAxis1y");
        if (bundle.containsKey("originSwapAxis"))
            swapAxis = bundle.getBoolean("originSwapAxis");
        if (bundle.containsKey("showCoordinateSystem"))
            showCoordinateSystem = bundle.getBoolean("showCoordinateSystem");
        originCalibrationSetter.setOrigin(origin, axis1);
        calibration.setSwapAxis(swapAxis);

        if (bundle.containsKey("xUnitPrefix"))
            setXUnitPrefix(bundle.getString("xUnitPrefix"));
        if (bundle.containsKey("yUnitPrefix"))
            setYUnitPrefix(bundle.getString("yUnitPrefix"));

        return true;
    }

    public void saveAnalysisDataToFile() throws IOException {
        Bundle bundle = new Bundle();
        Bundle experimentData = analysisDataToBundle();
        bundle.putBundle("analysis_data", experimentData);

        // save the bundle
        File projectFile = new File(experiment.getStorageDir(), EXPERIMENT_ANALYSIS_FILE_NAME);
        FileWriter fileWriter = new FileWriter(projectFile);
        PersistentBundle persistentBundle = new PersistentBundle();
        persistentBundle.flattenBundle(bundle, fileWriter);
    }

    public void exportTagMarkerCSVData(OutputStream outputStream) {
        try {
            String header = "id, x [" + getXUnit() + "], y [" + getYUnit()
                    + "], " + experiment.getRunValueLabel() + "\n";
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

    private void notifyShowCoordinateSystem(boolean show) {
        for (IExperimentAnalysisListener listener : listenerList)
            listener.onShowCoordinateSystem(show);
    }
}
