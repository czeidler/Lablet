/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;


import nz.ac.auckland.lablet.experiment.Unit;

public class CalibrationVideoFrame implements ITimeCalibration {
    private int numberOfFrames;
    final private int videoDuration;

    private int analysisFrameRate;
    // milli seconds
    private int analysisVideoStart;
    private int analysisVideoEnd;

    final private Unit tUnit;

    public CalibrationVideoFrame(int videoDuration, Unit tUnit) {
        this.videoDuration = videoDuration;
        this.tUnit = tUnit;

        setAnalysisVideoStart(0);
        setAnalysisVideoEnd(0);
        // must be called here (needs video start and end time as well as the duration)
        setAnalysisFrameRate(0);
    }

    @Override
    public Unit getUnit() {
        return tUnit;
    }

    @Override
    public float getTimeFromRaw(float raw) {
        return getFrameTime(Math.round(raw));
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    private float getFrameTime(int frame) {
        return analysisVideoStart + (float)1000 / analysisFrameRate * frame;
    }

    /**
     * Set the frame rate at which the video should be analysed.
     * <p>
     * This method should only be called from the {@link CameraExperimentAnalysis}. The
     * frame rate determined how many sensorDataList are returned by the experiments, i.e., a higher frame rate results in more
     * sensorDataList.
     * </p>
     * <p>
     * The analysis frame rate should be smaller or equal to the frame rate of the recorded video. Furthermore, the
     * video frame rate should be dividable byte the analysis frame rate. For example, if the video is recorded with
     * 30fps allowed analysis frame rates are 30, 15, 10, 5, 3, 2, 1.
     * </p>
     * @param frameRate
     */
    public void setAnalysisFrameRate(int frameRate) {
        analysisFrameRate = frameRate;
        if (analysisFrameRate <= 0)
            analysisFrameRate = 10;

        int runTime = analysisVideoEnd - analysisVideoStart;
        numberOfFrames = runTime * analysisFrameRate / 1000 + 1;
    }

    /**
     * Gets the frame rate at which the video should be analysed.
     *
     * @return the analysis frame rate
     */
    public int getAnalysisFrameRate() {
        return analysisFrameRate;
    }



    /**
     * Gets the start time at which the video should be analysed.
     *
     * @return the time when the analysis starts
     */
    public int getAnalysisVideoStart() {
        return analysisVideoStart;
    }

    /**
     * Sets the start time of the video analysis.
     * <p>
     * This value should only be set by {@link CameraExperimentAnalysis}. The analysis
     * start point affects the number of returned sensorDataList.
     * </p>
     * @param startTime the time for the analysis start
     */
    public void setAnalysisVideoStart(int startTime) {
        this.analysisVideoStart = startTime;
    }

    /**
     * Gets the end time for the video analysed.
     *
     * @return the video analysis end time
     */
    public int getAnalysisVideoEnd() {
        return analysisVideoEnd;
    }

    /**
     * Sets the end time for the video analysis.
     * <p>
     * This value should only be set by {@link CameraExperimentAnalysis}. The analysis
     * end point affects the number of returned sensorDataList.
     * </p>
     * @param endTime the time for the analysis end
     */
    public void setAnalysisVideoEnd(int endTime) {
        analysisVideoEnd = endTime;

        if (analysisVideoEnd <= 0)
            analysisVideoEnd = videoDuration;
    }
}
