/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.camera.VideoData;
import nz.ac.auckland.lablet.camera.decoder.CodecOutputSurface;
import nz.ac.auckland.lablet.camera.decoder.SeekToFrameExtractor;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.vision.data.*;
import org.opencv.core.Rect;

import java.io.IOException;


public class ObjectTrackerAnalysis {
    public interface IListener {
        void onTrackingFinished(SparseArray<Rect> results);
        void onTrackingUpdate(int frameNumber, int totalNumberOfFrames);
    }

    final private static String TAG = ObjectTrackerAnalysis.class.getName();

    final private String RECT_DATA_LIST = "rectDataList";
    final private String ROI_DATA_LIST = "roiDataList";
    final private String DEBUGGING_ENABLED = "debuggingEnabled";

    final private RoiDataList roiDataList = new RoiDataList();
    final private RectDataList rectDataList = new RectDataList();

    private boolean debuggingEnabled = false;

    private MotionAnalysis motionAnalysis;
    final private CamShiftTracker tracker;

    private boolean isTracking = false;
    private BackgroundTask task;

    RoiDataList.IListener roiDataListener = new DataList.IListener() {
        @Override
        public void onDataAdded(DataList dataList, int index) {

        }

        @Override
        public void onDataRemoved(DataList dataList, int index, Data data) {

        }

        @Override
        public void onDataChanged(DataList dataList, int index, int number) {

        }

        @Override
        public void onAllDataChanged(DataList dataList) {

        }

        @Override
        public void onDataSelected(DataList dataList, int index) {
            if (index < 0)
                return;
            motionAnalysis.getFrameDataModel().setCurrentFrame(dataList.getDataAt(index).getFrameId());
        }
    };

    public ObjectTrackerAnalysis(MotionAnalysis motionAnalysis, FrameDataModel frameDataModel) {
        this.motionAnalysis = motionAnalysis;
        tracker = new CamShiftTracker();

        roiDataList.addFrameDataList(frameDataModel);
        rectDataList.addFrameDataList(frameDataModel);
        rectDataList.setVisibility(false);

        roiDataList.addListener(roiDataListener);
    }

    public RectDataList getRectDataList(){
        return rectDataList;
    }
    public RoiDataList getRoiDataList() {return roiDataList;}

    public void stopTracking() {
        isTracking = false;
    }

    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }

    public void setDebuggingEnabled(boolean debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
        getRectDataList().setVisibility(debuggingEnabled);
    }

    public boolean isTracking() {
        return isTracking;
    }

    public RoiData getRoiForFrame(int frameId) {
        for (int i = 0; i < roiDataList.size(); i++) {
            RoiData roiData = roiDataList.getDataAt(i);
            if (roiData.getFrameId() == frameId)
                return roiData;
        }
        return null;
    }

    /**
     * Adds a region of interest marker.
     *
     * @param frameId The id of the frame to add the ROI marker to.
     */
    public void addRegionOfInterestMarker(int frameId) {
        RoiDataList roiDataList = motionAnalysis.getObjectTrackerAnalysis().getRoiDataList();

        RoiData data = new RoiData(frameId);
        PointF centre = new PointF(motionAnalysis.getVideoData().getMaxRawX() / 2,
                motionAnalysis.getVideoData().getMaxRawY() / 2);
        int width = 5;
        int height = 5;
        data.setTopLeft(new PointF(centre.x - width, centre.y + height));
        data.setTopRight(new PointF(centre.x + width, centre.y + height));
        data.setBtmRight(new PointF(centre.x + width, centre.y - height));
        data.setBtmLeft(new PointF(centre.x - width, centre.y - height));
        data.setCentre(centre);
        roiDataList.addData(data);
    }

    public void removeRegionOfInterest(RoiData roiData) {
        roiDataList.removeData(roiData);
    }

    /**
     * Tracks objects between a start and end frame. Region of interest markers need to be added
     * before this method is called so that the algorithm knows what objects to track. Use
     * addRegionOfInterestMarker to add a region of interest marker.
     *
     * @param startFrame Frame to begin tracking objects from.
     * @param endFrame Frame to stop tracking objects at.
     * @param listener
     */
    public void trackObjects(int startFrame, int endFrame, IListener listener) {
        if (motionAnalysis.getObjectTrackerAnalysis().getRoiDataList().size() > 0) {
            // TODO: don't allow to start to background threads!!
            isTracking = true;
            task = new BackgroundTask(startFrame, endFrame, motionAnalysis.getVideoData(), getRoiDataList(),
                    listener);
            task.execute();
        }
        else
        {
            Log.e(TAG, "Please add a region of interest before calling trackObjects");
        }
    }

    public void addPointMarker(int frameId, PointF point) {
        MarkerDataModel pointDataList = motionAnalysis.getTagMarkers();
        VideoData videoData = motionAnalysis.getVideoData();

        //Add point marker
        PointF centre = videoData.toMarkerPoint(point);

        int index = pointDataList.findMarkerDataByRun(frameId);
        if (index >= 0)
            pointDataList.setMarkerPosition(centre, index);
        else {
            MarkerData markerData = new MarkerData(frameId);
            markerData.setPosition(centre);
            pointDataList.addMarkerData(markerData);
        }
    }

    /**
     *
     * @param rect The object tracking results Rect.
     */
    public void addRectMarker(int frameId, Rect rect) {
        VideoData videoData = motionAnalysis.getVideoData();
        float centreX = rect.x + rect.width / 2;
        float centreY = rect.y + rect.height / 2;

        PointF centre = videoData.toMarkerPoint(new PointF(centreX, centreY));

        //Add debugging rectangle
        RectData data = new RectData(frameId);
        data.setCentre(centre);
        data.setAngle(0);
        PointF size = videoData.toMarkerPoint(new PointF(rect.width, rect.height));
        data.setWidth(size.x);
        data.setHeight(size.y);
        rectDataList.addData(data);

    }

    public void fromBundle(Bundle bundle) {
        rectDataList.clear();
        roiDataList.clear();
        if (bundle.containsKey(RECT_DATA_LIST))
            rectDataList.fromBundle(bundle.getBundle(RECT_DATA_LIST));

        if (bundle.containsKey(ROI_DATA_LIST))
            roiDataList.fromBundle(bundle.getBundle(ROI_DATA_LIST));

        if (bundle.containsKey(DEBUGGING_ENABLED))
            debuggingEnabled = bundle.getBoolean(DEBUGGING_ENABLED);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        if(rectDataList.size() > 0)
            bundle.putBundle(RECT_DATA_LIST, rectDataList.toBundle());

        if(roiDataList.size() > 0)
            bundle.putBundle(ROI_DATA_LIST, roiDataList.toBundle());

        bundle.putBoolean(DEBUGGING_ENABLED, isDebuggingEnabled());
        return bundle;
    }

    private class BackgroundTask extends AsyncTask<Void, Float, SparseArray<Rect>> {
        final int startFrame;
        final int endFrame;
        final private IListener listener;
        final private VideoData videodata;
        final private RoiDataList roiDataList;

        private CodecOutputSurface outputSurface;
        private SeekToFrameExtractor extractor;

        public BackgroundTask(int startFrame, int endFrame, VideoData videodata, RoiDataList roiDataList,
                              IListener listener) {
            this.startFrame = startFrame;
            this.endFrame = endFrame;
            this.listener = listener;
            this.videodata = videodata;
            this.roiDataList = roiDataList;
        }


        /**
         * Called by AsyncTask when the AsyncTask execute method is called.
         *
         * @param objects
         * @return
         */
        @Override
        protected SparseArray<Rect> doInBackground(Void[] objects) {
            isTracking = true;

            RoiData currentRoi = null;
            SparseArray<Rect> results = new SparseArray<>();

            outputSurface = new CodecOutputSurface(videodata.getVideoWidth(), videodata.getVideoHeight());
            try {
                extractor = new SeekToFrameExtractor(videodata.getVideoFile(), outputSurface.getSurface());
            } catch (IOException e) {
                return results;
            }

            //TODO: check if endFrame exclusive is what you want or i < endFrame + 1?
            for (int i = startFrame; i <= endFrame && isTracking; i++) {
                RoiData last = getClosestRoi(roiDataList, i);

                if (last != null) {
                    if (last != currentRoi) {
                        currentRoi = last;
                        long frameTimeMicroseconds = (long) motionAnalysis.getTimeData().getTimeAt(i) * 1000;
                        Bitmap roiBmp = getFrame(frameTimeMicroseconds);


                        if (roiBmp != null) {
                            PointF topLeft = videodata.toVideoPoint(currentRoi.getTopLeft().getPosition());
                            PointF btmRight = videodata.toVideoPoint(currentRoi.getBtmRight().getPosition());

                            int x = (int) topLeft.x;
                            int y = (int) topLeft.y;
                            int width = (int) (btmRight.x - topLeft.x);
                            int height = (int) (btmRight.y - topLeft.y);
                            tracker.setRegionOfInterest(roiBmp, x, y, width, height);
                        } else {
                            Log.d(TAG, "Region of interest BMP is null");
                            break;
                        }
                    }

                    if (currentRoi.getFrameId() != i) {
                        long frameTimeMicroseconds = (long) motionAnalysis.getTimeData().getTimeAt(i) * 1000;
                        Bitmap curFrameBmp = getFrame(frameTimeMicroseconds);
                        //tracker.saveFrame(curFrameBmp, "frame" + i);

                        if (curFrameBmp != null) {
                            Rect result = tracker.getObjectLocation(curFrameBmp);

                            if (result != null) {
                                results.put(i, result);
                            }
                        } else {
                            Log.d(TAG, "Current frame BMP is null: " + i);
                        }
                    }
                }

                Rect result = results.get(i);
                if(result != null)
                {
                    publishProgress((float)i - startFrame, (float)result.x, (float)result.y, (float)result.width, (float)result.height);
                }
                else
                {
                    publishProgress((float)i - startFrame, null, null, null, null);
                }
            }

            extractor.release();
            return results;
        }


        /**
         * Gets Bitmap of video frame
         *
         * @param time: time in microseconds.
         * @return The Bitmap of the video frame.
         */
        private Bitmap getFrame(long time) {
            extractor.seekToFrame(time);

            try
            {
                outputSurface.awaitNewImage();
                outputSurface.drawImage(true);
                return outputSurface.getBitmap();
            } catch (RuntimeException e) {
                return getFrame(time);
            }
        }


        /**
         * Gets the closest region of interest.
         *
         * @param roiDataList
         * @param currentFrame
         * @return
         */
        private RoiData getClosestRoi(RoiDataList roiDataList, int currentFrame) {
            RoiData data = null;

            for (int i = currentFrame; i >= 0; i--) {
                int roiIndex = roiDataList.getIndexByFrameId(i);

                if (roiIndex != -1) {
                    data = roiDataList.getDataAt(roiIndex);
                    break;
                }
            }

            return data;
        }


        /**
         * Called after the object tracking thread has finished.
         *
         * @param results The object tracking results.
         */
        @Override
        protected void onPostExecute(SparseArray<Rect> results) {
            super.onPostExecute(results);

            isTracking = false;

            if(listener != null) {
                listener.onTrackingFinished(results);
            }
        }


        /**
         * Called when each frame is processed by the object tracker.
         *
         * @param values The progress of the object tracker values[0] (from 0.0-1.0)
         */
        @Override
        protected void onProgressUpdate(Float... values) {
            super.onProgressUpdate(values);

            int currentFrame = values[0].intValue();
            Float x = values[1];
            Float y = values[2];
            Float width = values[3];
            Float height = values[4];

            if(x != null && y != null)
            {
                float centreX = x + width / 2;
                float centreY = y + height / 2;

                addPointMarker(currentFrame, new PointF(centreX, centreY));
                addRectMarker(currentFrame, new Rect(x.intValue(), y.intValue(), width.intValue(), height.intValue()));

                motionAnalysis.getFrameDataModel().setCurrentFrame(currentFrame);
            }

            if(listener != null)
            {
                listener.onTrackingUpdate(currentFrame, endFrame - startFrame);
            }
        }
    }
}
