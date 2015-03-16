/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;


public class CalibrationVideoTimeData implements ITimeData {
    protected int numberOfFrames;
    final protected long videoDuration;

    protected float analysisFrameRate;
    // milli seconds
    protected float analysisVideoStart;
    protected float analysisVideoEnd;

    public CalibrationVideoTimeData(long videoDuration) {
        this.videoDuration = videoDuration;

        setAnalysisVideoStart(0);
        setAnalysisVideoEnd(0);
        // must be called here (needs video start and end time as well as the duration)
        setAnalysisFrameRate(0);
    }

    @Override
    public int getSize() {
        return getNumberOfFrames();
    }

    @Override
    public float getTimeAt(float index) {
        return getFrameTime(Math.round(index));
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    protected float getFrameTime(int frame) {
        return analysisVideoStart + (float)1000 / analysisFrameRate * frame;
    }

    public int getClosestFrame(float time) {
        return Math.round((time - analysisVideoStart) * analysisFrameRate / 1000);
    }

    /**
     * Set the frame rate at which the video should be analysed.
     * <p>
     * This method should only be called from the {@link nz.ac.auckland.lablet.camera.MotionAnalysis}. The
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
    public void setAnalysisFrameRate(float frameRate) {
        analysisFrameRate = frameRate;
        if (analysisFrameRate <= 0)
            analysisFrameRate = 10;

        float runTime = analysisVideoEnd - analysisVideoStart;
        numberOfFrames = (int)(runTime * analysisFrameRate / 1000 + 1);
    }

    /**
     * Gets the frame rate at which the video should be analysed.
     *
     * @return the analysis frame rate
     */
    public float getAnalysisFrameRate() {
        return analysisFrameRate;
    }



    /**
     * Gets the start time at which the video should be analysed.
     *
     * @return the time when the analysis starts
     */
    public float getAnalysisVideoStart() {
        return analysisVideoStart;
    }

    /**
     * Sets the start time of the video analysis.
     * <p>
     * This value should only be set by {@link nz.ac.auckland.lablet.camera.MotionAnalysis}. The analysis
     * start point affects the number of returned sensorDataList.
     * </p>
     * @param startTime the time for the analysis start
     */
    public void setAnalysisVideoStart(float startTime) {
        this.analysisVideoStart = startTime;
        setAnalysisFrameRate(analysisFrameRate);
    }

    /**
     * Gets the end time for the video analysed.
     *
     * @return the video analysis end time
     */
    public float getAnalysisVideoEnd() {
        return analysisVideoEnd;
    }

    /**
     * Sets the end time for the video analysis.
     * <p>
     * This value should only be set by {@link nz.ac.auckland.lablet.camera.MotionAnalysis}. The analysis
     * end point affects the number of returned sensorDataList.
     * </p>
     * @param endTime the time for the analysis end
     */
    public void setAnalysisVideoEnd(float endTime) {
        analysisVideoEnd = endTime;

        if (analysisVideoEnd <= 0)
            analysisVideoEnd = videoDuration;

        setAnalysisFrameRate(analysisFrameRate);
    }
}
