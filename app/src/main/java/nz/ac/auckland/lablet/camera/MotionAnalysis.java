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
import android.util.SparseArray;

import org.opencv.core.Rect;

import nz.ac.auckland.lablet.data.CalibratedDataList;
import nz.ac.auckland.lablet.data.FrameDataList;
import nz.ac.auckland.lablet.data.PointData;
import nz.ac.auckland.lablet.data.PointDataList;
import nz.ac.auckland.lablet.data.RectData;
import nz.ac.auckland.lablet.data.RectDataList;
import nz.ac.auckland.lablet.data.RoiData;
import nz.ac.auckland.lablet.data.RoiDataList;
import nz.ac.auckland.lablet.experiment.*;
import nz.ac.auckland.lablet.misc.Unit;
import nz.ac.auckland.lablet.views.graph.IMinRangeGetter;
import nz.ac.auckland.lablet.views.table.*;
import nz.ac.auckland.lablet.vision.CamShiftTracker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains all data relevant for a motion analysis of {@link VideoData}.
 */
public class MotionAnalysis implements IDataAnalysis {
    public interface IListener {
        void onShowCoordinateSystem(boolean show);
    }

    final static public String ANALYSIS_FRAME_RATE_KEY = "analysis_frame_rate";
    final static public String ANALYSIS_VIDEO_START_KEY = "analysis_video_start";
    final static public String ANALYSIS_VIDEO_END_KEY = "analysis_video_end";

    final private String X_UNIT_BASE_EXPONENT_KEY = "xUnitBaseExponent";
    final private String Y_UNIT_BASE_EXPONENT_KEY = "yUnitBaseExponent";
    final private String LENGTH_CALIBRATION_KEY = "lengthCalibration";
    final private String CALIBRATION_XY_KEY = "calibrationXY";
    final private String SHOW_COORDINATE_SYSTEM_KEY = "showCoordinateSystem";

    final private String POINT_DATA_LIST = "pointDataList";
    final private String RECT_DATA_LIST = "rectDataList";
    final private String ROI_DATA_LIST = "roiDataList";
    final private String DEBUGGING_ENABLED = "debuggingEnabled";

    final private VideoData sensorData;

    final private FrameDataList frameDataList;
    final private CalibrationXY calibrationXY;
    final private CalibrationVideoTimeData calibrationVideoTimeData;
    final private ITimeData timeData;

    final private Unit xUnit = new Unit("m");
    final private Unit yUnit = new Unit("m");
    final private Unit tUnit = new Unit("s");

    final private CalibratedDataList pointDataList;
    final private PointDataList lenCalibDataList;
    final private PointDataList originDataList;
    final private RoiDataList roiDataList;
    final private RectDataList rectDataList;

    final private LengthCalibrationSetter lengthCalibrationSetter;
    final private OriginCalibrationSetter originCalibrationSetter;
    private boolean showCoordinateSystem = true;

    private Bundle videoAnalysisSettings = null;

    private int videoRotation = 0;
    private  boolean debuggingEnabled = false;

    private CamShiftTracker tracker;


    final private List<IListener> listenerList = new ArrayList<>();

    public MotionAnalysis(VideoData sensorData)
    {
        this.sensorData = sensorData;

        xUnit.setName("x");
        yUnit.setName("y");
        tUnit.setName("time");
        tUnit.setBaseExponent(-3); // milli seconds

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

        frameDataList = new FrameDataList();
        frameDataList.setNumberOfFrames(calibrationVideoTimeData.getNumberOfFrames());

        pointDataList = new CalibratedDataList(calibrationXY);
        pointDataList.setCalibrationXY(calibrationXY);

        float maxXValue = sensorData.getMaxRawX();
        float maxYValue = sensorData.getMaxRawY();

        lenCalibDataList = new PointDataList();
        PointData point1 = new PointData(-1);
        point1.setPosition(new PointF(maxXValue * 0.1f, maxYValue * 0.9f));
        lenCalibDataList.addData(point1);
        PointData point2 = new PointData(-2);
        point2.setPosition(new PointF(maxXValue * 0.3f, maxYValue * 0.9f));
        lenCalibDataList.addData(point2);
        lengthCalibrationSetter = new LengthCalibrationSetter(lenCalibDataList, calibrationXY);

        PointF origin = calibrationXY.getOrigin();
        PointF axis1 = calibrationXY.getAxis1();
        originDataList = new PointDataList();
        // y-axis
        point1 = new PointData(-1);
        point1.setPosition(new PointF(10, 10));
        originDataList.addData(point1);
        // x-axis
        point2 = new PointData(-2);
        point2.setPosition(new PointF(axis1.x, axis1.y));
        originDataList.addData(point2);
        // origin
        PointData point3 = new PointData(-3);
        point3.setPosition(origin);
        originDataList.addData(point3);
        originCalibrationSetter = new OriginCalibrationSetter(calibrationXY, originDataList);

        roiDataList = new RoiDataList();
        roiDataList.addFrameDataList(frameDataList);
        rectDataList = new RectDataList();
        rectDataList.addFrameDataList(frameDataList);
        rectDataList.setVisibility(false);
        tracker = new CamShiftTracker(this);

        updateOriginFromVideoRotation();
    }

    public CamShiftTracker getObjectTracker()
    {
        return tracker;
    }

    protected void setOrigin(PointF origin, PointF axis1) {
        originCalibrationSetter.setOrigin(origin, axis1);
    }

    public int getVideoRotation() {
        return videoRotation;
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
    public FrameDataList getFrameDataList() {
        return frameDataList;
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
    public PointDataList getPointDataList() {
        return pointDataList;
    }
    public PointDataList getXYCalibrationDataList() { return lenCalibDataList; }
    public RoiDataList getRoiDataList() {return roiDataList;}
    public PointDataList getOriginDataList(){
        return originDataList;
    }
    public RectDataList getRectDataList(){
        return rectDataList;
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
        pointDataList.clear();
        rectDataList.clear();
        roiDataList.clear();

        setVideoAnalysisSettings(bundle.getBundle("video_analysis_settings"));

        if(bundle.containsKey(DEBUGGING_ENABLED))
            tracker.setDebuggingEnabled(bundle.getBoolean(DEBUGGING_ENABLED));

        frameDataList.setCurrentFrame(bundle.getInt("currentRun"));

        if (bundle.containsKey(POINT_DATA_LIST))
            pointDataList.fromBundle(bundle.getBundle(POINT_DATA_LIST));

        if (bundle.containsKey(RECT_DATA_LIST))
            rectDataList.fromBundle(bundle.getBundle(RECT_DATA_LIST));

        if (bundle.containsKey(ROI_DATA_LIST))
            roiDataList.fromBundle(bundle.getBundle(ROI_DATA_LIST));

        if (bundle.containsKey(LENGTH_CALIBRATION_KEY))
            lengthCalibrationSetter.fromBundle(bundle.getBundle(LENGTH_CALIBRATION_KEY));

        if (bundle.containsKey(CALIBRATION_XY_KEY))
            calibrationXY.fromBundle(bundle.getBundle(CALIBRATION_XY_KEY));
        originCalibrationSetter.setOrigin(calibrationXY.getOrigin(), calibrationXY.getAxis1());

        if (bundle.containsKey(SHOW_COORDINATE_SYSTEM_KEY))
            showCoordinateSystem = bundle.getBoolean(SHOW_COORDINATE_SYSTEM_KEY);

        if (bundle.containsKey(X_UNIT_BASE_EXPONENT_KEY))
            xUnit.setBaseExponent(bundle.getInt(X_UNIT_BASE_EXPONENT_KEY));

        if (bundle.containsKey(Y_UNIT_BASE_EXPONENT_KEY))
            yUnit.setBaseExponent(bundle.getInt(Y_UNIT_BASE_EXPONENT_KEY));

        return true;
    }

    @Override
    public Bundle exportAnalysisData(File storageDir) throws IOException {
        Bundle analysisDataBundle = new Bundle();

        analysisDataBundle.putInt("currentRun", frameDataList.getCurrentFrame());

        if (pointDataList.size() > 0)
            analysisDataBundle.putBundle(POINT_DATA_LIST, pointDataList.toBundle());

        if(rectDataList.size() > 0)
            analysisDataBundle.putBundle(RECT_DATA_LIST, rectDataList.toBundle());

        if(roiDataList.size() > 0)
            analysisDataBundle.putBundle(ROI_DATA_LIST, roiDataList.toBundle());

        analysisDataBundle.putBundle(LENGTH_CALIBRATION_KEY, lengthCalibrationSetter.toBundle());

        analysisDataBundle.putBundle(CALIBRATION_XY_KEY, calibrationXY.toBundle());
        analysisDataBundle.putBoolean(SHOW_COORDINATE_SYSTEM_KEY, showCoordinateSystem);

        analysisDataBundle.putInt(X_UNIT_BASE_EXPONENT_KEY, getXUnit().getBaseExponent());
        analysisDataBundle.putInt(Y_UNIT_BASE_EXPONENT_KEY, getYUnit().getBaseExponent());

        analysisDataBundle.putBoolean(DEBUGGING_ENABLED, tracker.isDebuggingEnabled());

        if (videoAnalysisSettings != null)
            analysisDataBundle.putBundle("video_analysis_settings", videoAnalysisSettings);
        return analysisDataBundle;
    }

    @Override
    public void exportTagMarkerCSVData(Writer writer) {
        MarkerDataTableAdapter tableAdapter = new MarkerDataTableAdapter(pointDataList);
        tableAdapter.addColumn(new RunIdDataTableColumn("frame"));
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
        VideoData cameraExperiment = getVideoData();

        // read rotation from video
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(cameraExperiment.getVideoFile().getPath());
        String rotationString = mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        switch (rotationString) {
            case "90":
                videoRotation = 90;
                break;
            case "180":
                videoRotation = 180;
                break;
            case "270":
                videoRotation = 270;
                break;
        }
    }

    protected void onVideoAnalysisSettingsChanged() {
        Bundle runSettings = getVideoAnalysisSettings();
        if (runSettings == null)
            return;

        calibrationVideoTimeData.setAnalysisVideoStart(runSettings.getFloat(ANALYSIS_VIDEO_START_KEY));
        calibrationVideoTimeData.setAnalysisVideoEnd(runSettings.getFloat(ANALYSIS_VIDEO_END_KEY));
        calibrationVideoTimeData.setAnalysisFrameRate(runSettings.getFloat(ANALYSIS_FRAME_RATE_KEY));

        int numberOfFrames = calibrationVideoTimeData.getNumberOfFrames();
        getFrameDataList().setNumberOfFrames(numberOfFrames);
        if (numberOfFrames <= getFrameDataList().getCurrentFrame())
            getFrameDataList().setCurrentFrame(numberOfFrames - 1);
    }
}
