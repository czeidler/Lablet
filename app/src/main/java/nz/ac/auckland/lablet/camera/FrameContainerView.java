/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import nz.ac.auckland.lablet.views.marker.IMarker;
import nz.ac.auckland.lablet.views.marker.MarkerView;
import nz.ac.auckland.lablet.views.marker.TagMarkerDataModelPainter;
import nz.ac.auckland.lablet.vision.data.RectDataList;
import nz.ac.auckland.lablet.vision.data.RoiDataList;
import nz.ac.auckland.lablet.experiment.CalibrationXY;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.vision.markers.RectMarkerListPainter;
import nz.ac.auckland.lablet.vision.markers.RoiMarkerListPainter;


/**
 * Container for the {@link nz.ac.auckland.lablet.camera.CameraExperimentFrameView} and a marker view overlay.
 * <p>
 * The resize behaviour of the run view is copied and the marker view is put exactly on top of the run view. In this way
 * the screen coordinates of the run view and the marker view are the same.
 * </p>
 */
public class FrameContainerView extends RelativeLayout {
    private CameraExperimentFrameView videoAnalysisView = null;
    private FrameDataSeekBar seekBar;
    private MarkerView markerView = null;
    private TagMarkerDataModelPainter painter = null;
    private FrameDataModel frameDataModel = null;
    private MotionAnalysis motionAnalysis = null;
    private OriginMarkerPainter originMarkerPainter = null;

    private GestureDetector gestureDetector;
    final Handler handler = new Handler();

    private FrameDataModel.IListener frameDataModelListener = new FrameDataModel.IListener() {
        @Override
        public void onFrameChanged(int newFrame) {
            videoAnalysisView.setCurrentFrame(newFrame);
            markerView.setCurrentFrame(newFrame, null);
            markerView.invalidate();
            seekBarManager.open();
        }

        @Override
        public void onNumberOfFramesChanged() {

        }
    };

    public void onPause() {
        videoAnalysisView.onPause();
    }

    public void onResume() {
        videoAnalysisView.onResume();
    }

    class SeekBarManager {
        boolean open = false;
        long lastOpenRequest = 0;
        final int openTime = 4000;

        final int animationDuration = 300;
        AnimatorSet animator = null;

        public SeekBarManager() {
            seekBar.setVisibility(View.INVISIBLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    open();
                }
            });
        }

        public boolean isOpen() {
            return open;
        }

        public void open() {
            // seek bar getLastTouchEvent is SystemClock.uptimeMillis();
            lastOpenRequest = SystemClock.uptimeMillis();
            if (open)
                return;
            open = true;

            ViewGroup parent = (ViewGroup)FrameContainerView.this.getParent();
            animate(parent.getBottom(), parent.getBottom() - seekBar.getHeight(),
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animator = null;
                            scheduleClose(openTime);
                        }

                        @Override
                        public void onAnimationStart(Animator animation) {
                            seekBar.setVisibility(View.VISIBLE);
                        }
                    });
        }

        private void close() {
            if (!open)
                return;
            open = false;

            ViewGroup parent = (ViewGroup)FrameContainerView.this.getParent();
            animate(parent.getBottom() - seekBar.getHeight(), parent.getBottom(),
                    new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animator = null;
                            seekBar.setVisibility(INVISIBLE);
                        }
                    });
        }

        private void animate(int startY, int endY, AnimatorListenerAdapter listener) {
            // startY == endY == 0 can happen during init, wait for the real open event
            if (startY == 0 && endY == 0) {
                open = false;
                return;
            }
            if (animator != null)
                animator.cancel();

            animator = new AnimatorSet();
            animator.play(ObjectAnimator.ofFloat(seekBar, View.Y, startY, endY));
            animator.setDuration(animationDuration);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(listener);
            animator.start();
        }

        private void scheduleClose(long delay) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long currentTime = SystemClock.uptimeMillis();
                    if (seekBar.getLastTouchEvent() > lastOpenRequest)
                        lastOpenRequest = seekBar.getLastTouchEvent();
                    long timePast = currentTime - lastOpenRequest;
                    if (timePast >= openTime) {
                        close();
                        return;
                    }

                    // not done yet, wait the remaining time
                    scheduleClose(timePast - openTime);
                }
            }, delay);
        }
    }

    private SeekBarManager seekBarManager;

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (painter == null)
                return super.onSingleTapUp(e);

            if (markerView.isAnyMarkerSelectedForDrag())
                return super.onSingleTapUp(e);

            IMarker tappedMarker = painter.getMarkerAtScreenPosition(new PointF(e.getX(), e.getY()));
            if (tappedMarker == null)
                return super.onSingleTapUp(e);
            int tappedMarkerIndex = painter.markerIndexOf(tappedMarker);

            MarkerDataModel markerDataModel = (MarkerDataModel)painter.getMarkerModel();
            int frameId = markerDataModel.getMarkerDataAt(tappedMarkerIndex).getId();
            frameDataModel.setCurrentFrame(frameId);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            int currentFrame = frameDataModel.getCurrentFrame();
            int newFrame = currentFrame + 1;
            if (newFrame < frameDataModel.getNumberOfFrames()) {
                frameDataModel.setCurrentFrame(newFrame);
                markerView.setCurrentFrame(newFrame, new PointF(e.getX(), e.getY()));
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (markerView.isAnyMarkerSelectedForDrag())
                return false;

            // give the marker view the change to select a marker
            handler.post(new Runnable() {
                @Override
                public void run() {
                   if (!markerView.isAnyMarkerSelectedForDrag())
                        seekBarManager.open();
                }
            });

            return true;
        }
    }

    private MotionAnalysis.IListener motionAnalysisListener = new MotionAnalysis.IListener() {
        @Override
        public void onShowCoordinateSystem(boolean show) {
            if (show)
                addOriginMarkerPainter();
            else
                markerView.removePlotPainter(originMarkerPainter);
        }
    };

    public FrameContainerView(Context context) {
        super(context);
    }

    public FrameContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void finalize() {
        release();

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void setTo(CameraExperimentFrameView runView, FrameDataSeekBar seekBar, MotionAnalysis analysis) {
        this.seekBar = seekBar;
        seekBarManager = new SeekBarManager();
        seekBarManager.open();

        if (motionAnalysis != null)
            motionAnalysis.removeListener(motionAnalysisListener);
        motionAnalysis = analysis;
        motionAnalysis.addListener(motionAnalysisListener);

        if (frameDataModel != null)
            frameDataModel.removeListener(frameDataModelListener);
        frameDataModel = motionAnalysis.getFrameDataModel();
        frameDataModel.addListener(frameDataModelListener);

        videoAnalysisView = runView;

        videoAnalysisView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                int parentWidth = videoAnalysisView.getMeasuredWidth();
                int parentHeight = videoAnalysisView.getMeasuredHeight();
                markerView.setSize(parentWidth, parentHeight);
            }
        });

        // run view
        RelativeLayout.LayoutParams runViewParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(videoAnalysisView, runViewParams);

        // marker view
        RelativeLayout.LayoutParams makerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        makerViewParams.addRule(RelativeLayout.ALIGN_LEFT, videoAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_TOP, videoAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_RIGHT, videoAnalysisView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, videoAnalysisView.getId());

        markerView = new MarkerView(getContext());
        addView(markerView, makerViewParams);

        RectF range = videoAnalysisView.getDataRange();
        markerView.setRange(range);
        markerView.setMaxRange(range);

        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        markerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        initData();
    }

    private MarkerDataModel.IListener tagDataListener = new MarkerDataModel.IListener() {
        @Override
        public void onDataAdded(MarkerDataModel model, int index) {

        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {
            seekBarManager.open();
        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {

        }
    };

    public void initData() {
        // tag markers
        MarkerDataModel tagMarkers = motionAnalysis.getTagMarkers();
        tagMarkers.addListener(tagDataListener);
        painter = new TagMarkerDataModelPainter(tagMarkers);
        markerView.addPlotPainter(painter);

        frameDataModelListener.onFrameChanged(frameDataModel.getCurrentFrame());

        // calibration markers
        MarkerDataModel calibrationMarkers = motionAnalysis.getXYCalibrationMarkers();
        CalibrationMarkerPainter painter = new CalibrationMarkerPainter(calibrationMarkers);
        markerView.addPlotPainter(painter);

        //Region of interest markers
        RoiDataList roiDataList = motionAnalysis.getObjectTrackerAnalysis().getRoiDataList();
        RoiMarkerListPainter roiMarkerList = new RoiMarkerListPainter(roiDataList);
        markerView.addPlotPainter(roiMarkerList);

        //Rectangle markers
        RectDataList rectDataList = motionAnalysis.getObjectTrackerAnalysis().getRectDataList();
        RectMarkerListPainter rectMarkerList = new RectMarkerListPainter(rectDataList);
        markerView.addPlotPainter(rectMarkerList);

        // origin markers
        if (motionAnalysis.getShowCoordinateSystem())
            addOriginMarkerPainter();

    }

    private void addOriginMarkerPainter() {
        MarkerDataModel originMarkers = motionAnalysis.getOriginMarkers();
        CalibrationXY calibrationXY = motionAnalysis.getCalibrationXY();
        originMarkerPainter = new OriginMarkerPainter(originMarkers, calibrationXY);
        markerView.addPlotPainter(originMarkerPainter);
    }

    public void release() {
        if (markerView != null)
            markerView.release();
        if (motionAnalysis != null)
            motionAnalysis.removeListener(motionAnalysisListener);
        if (frameDataModel != null)
            frameDataModel.removeListener(frameDataModelListener);
    }

    /**
     * Copy resize behaviour of the videoAnalysisView.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (motionAnalysis == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int specWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        int specHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        videoAnalysisView.measure(widthMeasureSpec, heightMeasureSpec);

        int width = videoAnalysisView.getMeasuredWidth();
        int height = videoAnalysisView.getMeasuredHeight();

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, specWidthMode),
                MeasureSpec.makeMeasureSpec(height, specHeightMode));
    }
}
