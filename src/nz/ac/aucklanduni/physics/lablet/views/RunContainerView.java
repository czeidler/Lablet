/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import nz.ac.aucklanduni.physics.lablet.experiment.Calibration;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.experiment.MarkersDataModel;
import nz.ac.aucklanduni.physics.lablet.experiment.RunDataModel;


public class RunContainerView extends RelativeLayout implements RunDataModel.IRunDataModelListener,
        ExperimentAnalysis.IExperimentAnalysisListener {
    private View experimentRunView = null;
    private MarkerView markerView = null;
    private RunDataModel runDataModel = null;
    private ExperimentAnalysis experimentAnalysis = null;
    private OriginMarkerPainter originMarkerPainter = null;

    public RunContainerView(Context context) {
        super(context);
    }

    public RunContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void finalize() {
        experimentAnalysis.removeListener(this);
        runDataModel.removeListener(this);

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void setTo(View runView, ExperimentAnalysis analysis) {
        if (experimentAnalysis != null)
            experimentAnalysis.removeListener(this);
        experimentAnalysis = analysis;
        experimentAnalysis.addListener(this);

        if (runDataModel != null)
            runDataModel.removeListener(this);
        runDataModel = experimentAnalysis.getRunDataModel();
        runDataModel.addListener(this);

        experimentRunView = runView;

        experimentRunView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int parentWidth = experimentRunView.getMeasuredWidth();
                int parentHeight = experimentRunView.getMeasuredHeight();
                markerView.setSize(parentWidth, parentHeight);
            }
        });

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

        markerView = new MarkerView(getContext());
        addView(markerView, makerViewParams);
    }

    public void addTagMarkerData(MarkersDataModel data) {
        IMarkerDataModelPainter painter = new TagMarkerDataModelPainter(markerView,
                (IExperimentRunView)experimentRunView, data);
        markerView.addMarkerPainter(painter);

        onRunChanged(runDataModel.getCurrentRun());
    }

    public void addXYCalibrationData(MarkersDataModel data) {
        IMarkerDataModelPainter painter = new CalibrationMarkerPainter(markerView,
                (IExperimentRunView)experimentRunView, data);
        markerView.addMarkerPainter(painter);
    }

    public void removeOriginData() {
        markerView.removeMarkerPainter(originMarkerPainter);
    }

    public void addOriginData(MarkersDataModel data, Calibration calibration) {
        originMarkerPainter = new OriginMarkerPainter(markerView, (IExperimentRunView)experimentRunView, data,
                calibration);
        if (experimentAnalysis.getShowCoordinateSystem())
            markerView.addMarkerPainter(originMarkerPainter);
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

    @Override
    public void onUnitPrefixChanged() {

    }

    @Override
    public void onShowCoordinateSystem(boolean show) {
        if (show)
            markerView.addMarkerPainter(originMarkerPainter);
        else
            markerView.removeMarkerPainter(originMarkerPainter);
    }

    /**
     * Copy resize behaviour of the experimentRunView.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        experimentRunView.measure(widthMeasureSpec, heightMeasureSpec);

        int width = experimentRunView.getMeasuredWidth();
        int height = experimentRunView.getMeasuredHeight();

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, specWidthMode),
                MeasureSpec.makeMeasureSpec(height, specHeightMode));
    }
}
