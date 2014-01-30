/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;


public class RunContainerView extends RelativeLayout implements RunDataModel.IRunDataModelListener{
    private View experimentRunView = null;
    private MarkerView markerView = null;
    private RunDataModel runDataModel = null;

    public RunContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setTo(View runView, RunDataModel model) {
        experimentRunView = runView;

        experimentRunView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int parentWidth = experimentRunView.getMeasuredWidth();
                int parentHeight = experimentRunView.getMeasuredHeight();
                markerView.setSize(parentWidth, parentHeight);
            }
        });

        if (runDataModel != null)
            runDataModel.removeListener(this);
        runDataModel = model;
        runDataModel.addListener(this);

        // run view
        RelativeLayout.LayoutParams runViewParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(experimentRunView, runViewParams);

        // marker view
        RelativeLayout.LayoutParams makerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        makerViewParams.addRule(RelativeLayout.ALIGN_LEFT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_TOP, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_RIGHT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, experimentRunView.getId());

        markerView = new MarkerView(getContext(), experimentRunView);
        addView(markerView, makerViewParams);
    }

    public void addTagMarkerData(MarkersDataModel data) {
        markerView.addTagMarkers(data);
        onRunChanged(runDataModel.getCurrentRun());
    }

    public void addXYCalibrationData(MarkersDataModel data) {
        markerView.addXYCalibrationMarkers(data);
    }

    public void release() {
        markerView.release();
    }

    @Override
    public void onRunChanged(int newRun) {
        ((IExperimentRunView)experimentRunView).setCurrentRun(newRun);
        markerView.setCurrentRun(newRun);
        markerView.invalidate();
    }

    @Override
    public void onNumberOfRunsChanged() {

    }
}
