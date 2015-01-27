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
import nz.ac.auckland.lablet.views.graph.IMinRangeGetter;
import nz.ac.auckland.lablet.views.table.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for everything that is related to analysing an experiment.
 */
public class MotionAnalysis implements IDataAnalysis {
    public interface IListener {
        void onShowCoordinateSystem(boolean show);
    }

    final private VideoData sensorData;

    final private FrameDataModel frameDataModel;
    final private CalibrationXY calibrationXY;
    final private CalibrationVideoTimeData calibrationVideoTimeData;
    final private ITimeData timeData;

    final private Unit xUnit = new Unit("m");
    final private Unit yUnit = new Unit("m");
    final private Unit tUnit = new Unit("s");

    final private CalibratedMarkerDataModel tagMarkers;
    final private MarkerDataModel lengthCalibrationMarkers;
    final private MarkerDataModel originMarkers;

    final private LengthCalibrationSetter lengthCalibrationSetter;
    final private OriginCalibrationSetter originCalibrationSetter;
    private boolean showCoordinateSystem = true;

    private Bundle videoAnalysisSettings = null;

    final private List<IListener> listenerList = new ArrayList<>();

    public MotionAnalysis(VideoData sensorData) {
        this.sensorData = sensorData;

        xUnit.setName("x");
        yUnit.setName("y");
        tUnit.setName("time");
        tUnit.setPrefix("m");

        calibrationXY = new CalibrationXY();
        calibrationVideoTimeData = new CalibrationVideoTimeData(sensorData.getVideoDuration());
        // if recorded with reduced frame rate use the recording frame rate in this case, e.g., for recording frame
        // rates < 1 the video frame rate is not valid
        if (sensorData.isRecordedAtReducedFrameRate())
            calibrationVideoTimeData.setAnalysisFrameRate(sensorData.getRecordingFrameRate());
        else {
            calibrationVideoTimeData.setAnalysisFrameRate(FrameRateHelper.getBestPossibleAnalysisFrameRate(
                    sensorData.getVideoFrameRate(), 10));
        }
        timeData = calibrationVideoTimeData;

        frameDataModel = new FrameDataModel();
        frameDataModel.setNumberOfFrames(calibrationVideoTimeData.getNumberOfFrames());

        tagMarkers = new CalibratedMarkerDataModel(calibrationXY);
        tagMarkers.setCalibrationXY(calibrationXY);

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
    public String getDisplayName() {
        return "Motion Analysis";
    }

    @Override
    public String getIdentifier() {
        return "MotionAnalysis";
    }

    public VideoData getVideoData() { return sensorData; }
    public FrameDataModel getFrameDataModel() {
        return frameDataModel;
    }
    public LengthCalibrationSetter getLengthCalibrationSetter() {
        return lengthCalibrationSetter;
    }
    public CalibrationXY getCalibrationXY() {
        return calibrationXY;
    }
    public ITimeData getTimeData() {
        return timeData;
    }
    public CalibrationVideoTimeData getCalibrationVideoTimeData() {
        return calibrationVideoTimeData;
    }
    public MarkerDataModel getTagMarkers() {
        return tagMarkers;
    }
    public MarkerDataModel getXYCalibrationMarkers() { return lengthCalibrationMarkers; }
    public MarkerDataModel getOriginMarkers(){
        return originMarkers;
    }
    public Bundle getVideoAnalysisSettings() { return videoAnalysisSettings; }
    public void setShowCoordinateSystem(boolean show) {
        showCoordinateSystem = show;
        notifyShowCoordinateSystem(show);
    }
    public boolean getShowCoordinateSystem() {
        return showCoordinateSystem;
    }
    public void setVideoAnalysisSettings(Bundle data) {
        videoAnalysisSettings = data;
        onVideoAnalysisSettingsChanged();
    }

    @Override
    public ISensorData[] getData() {
        return new ISensorData[]{sensorData};
    }

    public Unit getXUnit() {
        return xUnit;
    }

    public Unit getYUnit() {
        return yUnit;
    }

    public Unit getTUnit() {
        return tUnit;
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

    public IMinRangeGetter getXMinRangeGetter() {
        return new IMinRangeGetter() {
            @Override
            public Number getMinRange() {
                PointF point = new PointF();
                point.x = sensorData.getMaxRawX();
                point = calibrationXY.fromRawLength(point);
                return point.x * 0.2f;
            }
        };
    }

    public IMinRangeGetter getYMinRangeGetter() {
        return new IMinRangeGetter() {
            @Override
            public Number getMinRange() {
                PointF point = new PointF();
                point.y = sensorData.getMaxRawY();
                point = calibrationXY.fromRawLength(point);
                return point.y * 0.2f;
            }
        };
    }

    public boolean loadAnalysisData(Bundle bundle, File storageDir) {
        tagMarkers.clear();

        setVideoAnalysisSettings(bundle.getBundle("video_analysis_settings"));
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

        analysisDataBundle.putString("xUnitPrefix", getXUnit().getPrefix());
        analysisDataBundle.putString("yUnitPrefix", getYUnit().getPrefix());

        if (videoAnalysisSettings != null)
            analysisDataBundle.putBundle("video_analysis_settings", videoAnalysisSettings);
        return analysisDataBundle;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) {
        MarkerDataTableAdapter tableAdapter = new MarkerDataTableAdapter(tagMarkers);
        tableAdapter.addColumn(new RunIdDataTableColumn());
        tableAdapter.addColumn(new XPositionDataTableColumn(xUnit));
        tableAdapter.addColumn(new YPositionDataTableColumn(yUnit));
        tableAdapter.addColumn(new TimeDataTableColumn(tUnit, timeData));
        CSVWriter.writeTable(tableAdapter, writer, ',');
    }

    private void notifyShowCoordinateSystem(boolean show) {
        for (IListener listener : listenerList)
            listener.onShowCoordinateSystem(show);
    }

    private void updateOriginFromVideoRotation() {
        VideoData cameraExperiment = (VideoData) getVideoData();

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

    protected void onVideoAnalysisSettingsChanged() {
        Bundle runSettings = getVideoAnalysisSettings();
        if (runSettings == null)
            return;

        calibrationVideoTimeData.setAnalysisVideoStart(runSettings.getFloat("analysis_video_start"));
        calibrationVideoTimeData.setAnalysisVideoEnd(runSettings.getFloat("analysis_video_end"));
        calibrationVideoTimeData.setAnalysisFrameRate(runSettings.getFloat("analysis_frame_rate"));

        int numberOfRuns = calibrationVideoTimeData.getNumberOfFrames();
        getFrameDataModel().setNumberOfFrames(numberOfRuns);
        if (numberOfRuns <= getFrameDataModel().getCurrentFrame())
            getFrameDataModel().setCurrentFrame(numberOfRuns - 1);
    }
}
