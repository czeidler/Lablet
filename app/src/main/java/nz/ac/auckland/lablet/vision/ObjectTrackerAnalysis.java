/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import android.os.Bundle;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.vision.data.RectDataList;
import nz.ac.auckland.lablet.vision.data.RoiDataList;


public class ObjectTrackerAnalysis {
    final private String RECT_DATA_LIST = "rectDataList";
    final private String ROI_DATA_LIST = "roiDataList";
    final private String DEBUGGING_ENABLED = "debuggingEnabled";

    final private RoiDataList roiDataList = new RoiDataList();
    final private RectDataList rectDataList = new RectDataList();

    final private CamShiftTracker tracker;

    public ObjectTrackerAnalysis(MotionAnalysis motionAnalysis, FrameDataModel frameDataModel) {
        tracker = new CamShiftTracker(motionAnalysis);

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
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        if(rectDataList.size() > 0)
            bundle.putBundle(RECT_DATA_LIST, rectDataList.toBundle());

        if(roiDataList.size() > 0)
            bundle.putBundle(ROI_DATA_LIST, roiDataList.toBundle());

        bundle.putBoolean(DEBUGGING_ENABLED, tracker.isDebuggingEnabled());
        return bundle;
    }
}
