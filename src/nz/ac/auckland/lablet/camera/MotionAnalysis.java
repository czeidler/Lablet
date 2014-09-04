/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.graphics.PointF;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Base class for everything that is related to analysing an experiment.
 */
public class MotionAnalysis implements ISensorAnalysis {
    public interface IListener {
        void onShowCoordinateSystem(boolean show);
    }

    private CameraSensorData sensorData;

    private FrameDataModel frameDataModel;
    private CalibrationXY calibrationXY;
    private CalibrationVideoFrame calibrationVideoFrame;

    private Unit xUnit = new Unit("m");
    private Unit yUnit = new Unit("m");
    private Unit tUnit = new Unit("s");

    private CalibratedMarkerDataModel tagMarkers;
    private MarkerDataModel lengthCalibrationMarkers;
    private MarkerDataModel originMarkers;

    private LengthCalibrationSetter lengthCalibrationSetter;
    private OriginCalibrationSetter originCalibrationSetter;
    private boolean showCoordinateSystem = true;

    private Bundle experimentSpecificData = null;

    private List<IListener> listenerList = new ArrayList<IListener>();

    public MotionAnalysis(CameraSensorData sensorData) {
        this.sensorData = sensorData;

        tUnit.setPrefix("m");

        calibrationXY = new CalibrationXY(xUnit, yUnit);
        calibrationVideoFrame = new CalibrationVideoFrame(sensorData.getVideoDuration(), tUnit);

        frameDataModel = new FrameDataModel();
        frameDataModel.setNumberOfFrames(calibrationVideoFrame.getNumberOfFrames());

        tagMarkers = new CalibratedMarkerDataModel(calibrationXY);
        tagMarkers.setCalibrationXY(calibrationXY);
        tagMarkers.setMaxRangeRaw(sensorData.getMaxRawX(), sensorData.getMaxRawY());

        float maxXValue = sensorData.getMaxRawX();
        float maxYValue = sensorData.getMaxRawY();
        lengthCalibrationMarkers = new MarkerDataModel();
        MarkerData point1 = new MarkerData(-1);
        point1.setPosition(new PointF(maxXValue * 0.1f, maxYValue * 0.9f));
        lengthCalibrationMarkers.addMarkerData(point1);
        MarkerData point2 = new MarkerData(-2);
        point2.setPosition(new PointF(maxXValue * 0.3f, maxYValue * 0.9f));
        lengthCalibrationMarkers.addMarkerData(point2);
        lengthCalibrationSetter = new LengthCalibrationSetter(lengthCalibrationMarkers, calibrationXY);

        PointF origin = calibrationXY.getOrigin();
        PointF axis1 = calibrationXY.getAxis1();
        originMarkers = new MarkerDataModel();
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
        originCalibrationSetter = new OriginCalibrationSetter(calibrationXY, originMarkers);

        updateOriginFromVideoRotation();
    }

    protected void setOrigin(PointF origin, PointF axis1) {
        originCalibrationSetter.setOrigin(origin, axis1);
    }

    @Override
    public String getIdentifier() {
        return "MotionAnalysis";
    }

    public ISensorData getData() { return sensorData; }
    public FrameDataModel getFrameDataModel() {
        return frameDataModel;
    }
    public LengthCalibrationSetter getLengthCalibrationSetter() {
        return lengthCalibrationSetter;
    }
    public CalibrationXY getCalibrationXY() {
        return calibrationXY;
    }
    public CalibrationVideoFrame getCalibrationVideoFrame() {
        return calibrationVideoFrame;
    }
    public CalibratedMarkerDataModel getTagMarkers() {
        return tagMarkers;
    }
    public MarkerDataModel getXYCalibrationMarkers() { return lengthCalibrationMarkers; }
    public MarkerDataModel getOriginMarkers(){
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
        return xUnit.getPrefix();
    }

    public String getYUnitPrefix() {
        return yUnit.getPrefix();
    }

    public void addListener(IListener listener) {
        listenerList.add(listener);
    }

    public boolean removeListener(IListener listener) {
        return listenerList.remove(listener);
    }

    public void setXUnitPrefix(String xUnitPrefix) {
        this.xUnit.setPrefix(xUnitPrefix);
    }
    public void setYUnitPrefix(String yUnitPrefix) {
        this.yUnit.setPrefix(yUnitPrefix);
    }

    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        tagMarkers.clear();

        setExperimentSpecificData(bundle.getBundle("experiment_specific_data"));
        frameDataModel.setCurrentFrame(bundle.getInt("currentRun"));

        Bundle tagMarkerBundle = bundle.getBundle("tagMarkers");
        if (tagMarkerBundle != null)
            tagMarkers.fromBundle(tagMarkerBundle);

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

        PointF origin = calibrationXY.getOrigin();
        PointF axis1 = calibrationXY.getAxis1();
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
        calibrationXY.setSwapAxis(swapAxis);

        if (bundle.containsKey("xUnitPrefix"))
            setXUnitPrefix(bundle.getString("xUnitPrefix"));
        if (bundle.containsKey("yUnitPrefix"))
            setYUnitPrefix(bundle.getString("yUnitPrefix"));

        return true;
    }

    @Override
    public Bundle exportAnalysisData(File storageDir) throws IOException {
        Bundle analysisDataBundle = new Bundle();

        analysisDataBundle.putInt("currentRun", frameDataModel.getCurrentFrame());

        if (tagMarkers.getMarkerCount() > 0) {
            Bundle tagMarkerBundle = tagMarkers.toBundle();
            analysisDataBundle.putBundle("tagMarkers", tagMarkerBundle);
        }

        PointF point1 = lengthCalibrationMarkers.getMarkerDataAt(0).getPosition();
        PointF point2 = lengthCalibrationMarkers.getMarkerDataAt(1).getPosition();
        analysisDataBundle.putFloat("lengthCalibrationPoint1x", point1.x);
        analysisDataBundle.putFloat("lengthCalibrationPoint1y", point1.y);
        analysisDataBundle.putFloat("lengthCalibrationPoint2x", point2.x);
        analysisDataBundle.putFloat("lengthCalibrationPoint2y", point2.y);
        analysisDataBundle.putFloat("lengthCalibrationValue", lengthCalibrationSetter.getCalibrationValue());

        analysisDataBundle.putFloat("originX", calibrationXY.getOrigin().x);
        analysisDataBundle.putFloat("originY", calibrationXY.getOrigin().y);
        analysisDataBundle.putFloat("originAxis1x", calibrationXY.getAxis1().x);
        analysisDataBundle.putFloat("originAxis1y", calibrationXY.getAxis1().y);
        analysisDataBundle.putBoolean("originSwapAxis", calibrationXY.getSwapAxis());
        analysisDataBundle.putBoolean("showCoordinateSystem", showCoordinateSystem);

        analysisDataBundle.putString("xUnitPrefix", getXUnitPrefix());
        analysisDataBundle.putString("yUnitPrefix", getYUnitPrefix());

        if (experimentSpecificData != null)
            analysisDataBundle.putBundle("experiment_specific_data", experimentSpecificData);
        return analysisDataBundle;
    }

    public void exportTagMarkerCSVData(OutputStream outputStream) {
        try {
            String header = "id, x [" + xUnit.getUnit()+ "], y [" + yUnit.getUnit() + "], time [" + tUnit.getUnit()
                    + "]\n";
            outputStream.write(header.getBytes());
            for (int i = 0; i < tagMarkers.getMarkerCount(); i++) {
                MarkerData markerData = tagMarkers.getMarkerDataAt(i);
                String string = "";
                string += markerData.getRunId();
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                PointF position = tagMarkers.getRealMarkerPositionAt(i);
                string = "";
                string += position.x;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += position.y;
                outputStream.write(string.getBytes());
                outputStream.write(",".getBytes());

                string = "";
                string += calibrationVideoFrame.getTimeFromRaw(i);
                outputStream.write(string.getBytes());

                outputStream.write("\n".getBytes());
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyShowCoordinateSystem(boolean show) {
        for (IListener listener : listenerList)
            listener.onShowCoordinateSystem(show);
    }

    private void updateOriginFromVideoRotation() {
        CameraSensorData cameraExperiment = (CameraSensorData)getData();
        CalibrationXY calibrationXY = getCalibrationXY();

        // read rotation from video
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(cameraExperiment.getVideoFile().getPath());
        String rotationString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        PointF origin = new PointF();
        origin.set(calibrationXY.getOrigin());
        float xOffset = origin.x;
        float yOffset = origin.y;
        PointF axis1 = new PointF();
        axis1.set(calibrationXY.getAxis1());
        switch (rotationString) {
            case "90":
                origin.x = cameraExperiment.getMaxRawX() - xOffset;
                origin.y = yOffset;
                axis1.x = origin.x;
                axis1.y = origin.y + 10;
                break;
            case "180":
                origin.x = cameraExperiment.getMaxRawX() - xOffset;
                origin.y = cameraExperiment.getMaxRawY() - yOffset;
                axis1.x = origin.x - 10;
                axis1.y = origin.y;
                break;
            case "270":
                origin.x = xOffset;
                origin.y = cameraExperiment.getMaxRawY() - yOffset;
                axis1.x = origin.x;
                axis1.y = origin.y - 10;
                break;
        }

        setOrigin(origin, axis1);
    }

    protected void onRunSpecificDataChanged() {
        Bundle experimentSpecificData = getExperimentSpecificData();
        if (experimentSpecificData == null)
            return;
        Bundle runSettings = experimentSpecificData.getBundle("run_settings");
        if (runSettings == null)
            return;

        calibrationVideoFrame.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
        calibrationVideoFrame.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        calibrationVideoFrame.setAnalysisFrameRate(runSettings.getInt("analysis_frame_rate"));

        int numberOfRuns = calibrationVideoFrame.getNumberOfFrames();
        getFrameDataModel().setNumberOfFrames(numberOfRuns);
        if (numberOfRuns <= getFrameDataModel().getCurrentFrame())
            getFrameDataModel().setCurrentFrame(numberOfRuns - 1);
    }
}
