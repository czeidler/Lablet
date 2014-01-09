package com.example.AndroidPhysicsTracker;


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
        this.experimentRunView = runView;

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

        onRunChanged(runDataModel.getCurrentRun());
    }

    public void addMarkerData(MarkersDataModel data) {
        markerView.addTagMarkers(data);
    }

    public boolean removeMarkerData(MarkersDataModel data) {
        return markerView.removeMarkers(data);
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
