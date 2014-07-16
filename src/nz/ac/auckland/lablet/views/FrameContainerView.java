/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import nz.ac.auckland.lablet.experiment.Calibration;
import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;


/**
 * Container for the {@link IExperimentFrameView} and a marker view overlay.
 * <p>
 * The resize behaviour of the run view is copied and the marker view is put exactly on top of the run view. In this way
 * the screen coordinates of the run view and the marker view are the same.
 * </p>
 */
public class FrameContainerView extends RelativeLayout implements FrameDataModel.IFrameDataModelListener,
        SensorAnalysis.IExperimentAnalysisListener {
    private View sensorAnalysisView = null;
    private MarkerView markerView = null;
    private FrameDataModel frameDataModel = null;
    private SensorAnalysis sensorAnalysis = null;
    private OriginMarkerPainter originMarkerPainter = null;

    public FrameContainerView(Context context) {
        super(context);
    }

    public FrameContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void finalize() {
        if (sensorAnalysis != null)
            sensorAnalysis.removeListener(this);
        if (frameDataModel != null)
            frameDataModel.removeListener(this);

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void setTo(View runView, SensorAnalysis analysis) {
        if (sensorAnalysis != null)
            sensorAnalysis.removeListener(this);
        sensorAnalysis = analysis;
        sensorAnalysis.addListener(this);

        if (frameDataModel != null)
            frameDataModel.removeListener(this);
        frameDataModel = sensorAnalysis.getFrameDataModel();
        frameDataModel.addListener(this);

        sensorAnalysisView = runView;

        sensorAnalysisView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int parentWidth = sensorAnalysisView.getMeasuredWidth();
                int parentHeight = sensorAnalysisView.getMeasuredHeight();
                markerView.setSize(parentWidth, parentHeight);
            }
        });

        // run view
        RelativeLayout.LayoutParams runViewParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(sensorAnalysisView, runViewParams);

        // marker view
        RelativeLayout.LayoutParams makerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        makerViewParams.addRule(RelativeLayout.ALIGN_LEFT, sensorAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_TOP, sensorAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_RIGHT, sensorAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, sensorAnalysisView.getId());

        markerView = new MarkerView(getContext());
        addView(markerView, makerViewParams);
    }

    public void addTagMarkerData(MarkerDataModel data) {
        IMarkerDataModelPainter painter = new TagMarkerDataModelPainter(markerView,
                (IExperimentFrameView) sensorAnalysisView, data);
        markerView.addMarkerPainter(painter);

        onFrameChanged(frameDataModel.getCurrentFrame());
    }

    public void addXYCalibrationData(MarkerDataModel data) {
        IMarkerDataModelPainter painter = new CalibrationMarkerPainter(markerView,
                (IExperimentFrameView) sensorAnalysisView, data);
        markerView.addMarkerPainter(painter);
    }

    public void removeOriginData() {
        markerView.removeMarkerPainter(originMarkerPainter);
    }

    public void addOriginData(MarkerDataModel data, Calibration calibration) {
        originMarkerPainter = new OriginMarkerPainter(markerView, (IExperimentFrameView) sensorAnalysisView, data,
                calibration);
        if (sensorAnalysis.getShowCoordinateSystem())
            markerView.addMarkerPainter(originMarkerPainter);
    }

    public void release() {
        if (markerView != null)
            markerView.release();
    }

    @Override
    public void onFrameChanged(int newFrame) {
        ((IExperimentFrameView) sensorAnalysisView).setCurrentFrame(newFrame);
        markerView.setCurrentRun(newFrame);
        markerView.invalidate();
    }

    @Override
    public void onNumberOfFramesChanged() {

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
     * Copy resize behaviour of the sensorAnalysisView.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (sensorAnalysis == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        sensorAnalysisView.measure(widthMeasureSpec, heightMeasureSpec);

        int width = sensorAnalysisView.getMeasuredWidth();
        int height = sensorAnalysisView.getMeasuredHeight();

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, specWidthMode),
                MeasureSpec.makeMeasureSpec(height, specHeightMode));
    }
}
