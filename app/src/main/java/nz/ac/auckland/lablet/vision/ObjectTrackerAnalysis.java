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
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.vision.data.RectData;
import nz.ac.auckland.lablet.vision.data.RectDataList;
import nz.ac.auckland.lablet.vision.data.RoiData;
import nz.ac.auckland.lablet.vision.data.RoiDataList;
import org.opencv.core.Rect;

import java.io.IOException;


public class ObjectTrackerAnalysis {
    public interface IListener {
        void onTrackingFinished(SparseArray<Rect> results);
        void onTrackingUpdate(Double percentDone);
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

    public ObjectTrackerAnalysis(MotionAnalysis motionAnalysis, FrameDataModel frameDataModel) {
        this.motionAnalysis = motionAnalysis;
        tracker = new CamShiftTracker();

        roiDataList.addFrameDataList(frameDataModel);
        rectDataList.addFrameDataList(frameDataModel);
        rectDataList.setVisibility(false);
    }

    public CamShiftTracker getObjectTracker() {
        return tracker;
    }

    public RectDataList getRectDataList(){
        return rectDataList;
    }
    public RoiDataList getRoiDataList() {return roiDataList;}

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

    public boolean isDebuggingEnabled() {
        return debuggingEnabled;
    }

    public void setDebuggingEnabled(boolean debuggingEnabled) {
        this.debuggingEnabled = debuggingEnabled;
        getRectDataList().setVisibility(debuggingEnabled);
    }

    public void updateMarkers(SparseArray<Rect> results) {
        //Delete all items from arrays
        MarkerDataModel pointDataList = motionAnalysis.getTagMarkers();
        RectDataList rectDataList = motionAnalysis.getObjectTrackerAnalysis().getRectDataList();
        rectDataList.clear();

        VideoData videoData = motionAnalysis.getVideoData();

        for (int i = 0; i < results.size(); i++) {
            int frameId = results.keyAt(i);
            Rect result = results.get(frameId);

            float centreX = result.x + result.width / 2;
            float centreY = result.y + result.height / 2;

            //Add point marker
            PointF centre = videoData.toMarkerPoint(new PointF(centreX, centreY));

            int index = pointDataList.findMarkerDataByRun(frameId);
            if (index >= 0)
                pointDataList.setMarkerPosition(centre, index);
            else {
                MarkerData markerData = new MarkerData(frameId);
                markerData.setPosition(centre);
                pointDataList.addMarkerData(markerData);
            }

            //Add debugging rectangle
            RectData data = new RectData(frameId);
            data.setCentre(centre);
            data.setAngle(0);
            PointF size = videoData.toMarkerPoint(new PointF(result.width, result.height));
            data.setWidth(size.x);
            data.setHeight(size.y);
            rectDataList.addData(data);
        }
    }

    public void addRegionOfInterest(int frameId) {
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

    private class BackgroundTask extends AsyncTask<Void, Double, SparseArray<Rect>> {
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

            for (int i = startFrame; i < endFrame && isTracking; i++) {
                RoiData last = getLastRoi(roiDataList, i);

                if (last != null) {
                    if (last != currentRoi) {
                        currentRoi = last;
                        long frameTimeMicroseconds = (long) motionAnalysis.getTimeData().getTimeAt(i) * 1000;
                        Bitmap roiBmp = getFrame(frameTimeMicroseconds);
                        //saveFrame(roiBmp, "roi");

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
                            return null;
                        }
                    }

                    if (currentRoi.getFrameId() != i) {
                        long frameTimeMicroseconds = (long) motionAnalysis.getTimeData().getTimeAt(i) * 1000;
                        Bitmap curFrameBmp = getFrame(frameTimeMicroseconds);
                        //saveFrame(curFrameBmp, "frame" + i);

                        if (curFrameBmp != null && curFrameBmp.getConfig() != null) {
                            Rect result = tracker.getObjectLocation(curFrameBmp);

                            if (result != null) {
                                results.put(i, result);
                            }
                        } else {
                            Log.d(TAG, "Current frame BMP is null: " + i);
                        }
                    }
                }

                publishProgress(((double) i + 1) / endFrame);
            }

            return results;
        }

        /**
         * Gets Bitmap of video frame
         *
         * @param time: time in microseconds
         * @return
         */
        private Bitmap getFrame(long time) {
            extractor.seekToFrameSync(time);
            outputSurface.awaitNewImage();
            outputSurface.drawImage(true);
            return outputSurface.getBitmap();
        }

        /**
         * Sets the region of interest for the CamShiftTracker.
         *
         * @param roiDataList
         * @param currentFrame
         * @return
         */
        private RoiData getLastRoi(RoiDataList roiDataList, int currentFrame) {
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

        @Override
        protected void onPostExecute(SparseArray<Rect> results) {
            super.onPostExecute(results);

            updateMarkers(results);

            listener.onTrackingFinished(results);
        }

        @Override
        protected void onProgressUpdate(Double... values) {
            super.onProgressUpdate(values);

            listener.onTrackingUpdate(values[0]);
        }
    }

    private BackgroundTask task;

    public void trackObjects(int startFrame, int endFrame, IListener listener) {
        if (motionAnalysis.getObjectTrackerAnalysis().getRoiDataList().size() > 0) {
            // TODO: don't allow to start to background threads!!
            isTracking = true;
            task = new BackgroundTask(startFrame, endFrame, motionAnalysis.getVideoData(), getRoiDataList(),
                    listener);
            task.execute();
            Log.e(TAG, "Please add a region of interest before calling trackObjects");
        }
    }

    public void stopTracking() {
        isTracking = false;
    }
}
