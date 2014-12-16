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
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import nz.ac.auckland.lablet.experiment.CalibrationXY;
import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.*;


/**
 * Container for the {@link nz.ac.auckland.lablet.views.IExperimentFrameView} and a marker view overlay.
 * <p>
 * The resize behaviour of the run view is copied and the marker view is put exactly on top of the run view. In this way
 * the screen coordinates of the run view and the marker view are the same.
 * </p>
 */
public class FrameContainerView extends RelativeLayout {
    private View videoAnalysisView = null;
    private FrameDataSeekBar seekBar;
    private MarkerView markerView = null;
    private TagMarkerDataModelPainter painter = null;
    private FrameDataModel frameDataModel = null;
    private MotionAnalysis sensorAnalysis = null;
    private OriginMarkerPainter originMarkerPainter = null;

    private GestureDetector gestureDetector;
    final Handler handler = new Handler();

    private FrameDataModel.IFrameDataModelListener frameDataModelListener = new FrameDataModel.IFrameDataModelListener() {
        @Override
        public void onFrameChanged(int newFrame) {
            ((IExperimentFrameView) videoAnalysisView).setCurrentFrame(newFrame);
            markerView.setCurrentFrame(newFrame, null);
            markerView.invalidate();
            seekBarManager.open();
        }

        @Override
        public void onNumberOfFramesChanged() {

        }
    };

    class SeekBarManager {
        boolean open = false;
        long lastOpenRequest = 0;
        final int openTime = 3000;

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

            seekBar.setVisibility(View.VISIBLE);

            animate(FrameContainerView.this.getHeight(), FrameContainerView.this.getHeight() - seekBar.getHeight(),
                    new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animator = null;
                    scheduleClose(openTime);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animator = null;
                }
            });
        }

        private void close() {
            animate(FrameContainerView.this.getHeight() - seekBar.getHeight(), FrameContainerView.this.getHeight(),
                    new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animator = null;
                    open = false;
                    seekBar.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animator = null;
                }
            });
        }

        private void animate(int startY, int endY, AnimatorListenerAdapter listener) {
            // startY == endY == 0 can happen during init, wait for the real open event
            if (startY == endY) {
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

            MarkerDataModel markerDataModel = painter.getMarkerModel();
            int frameId = markerDataModel.getMarkerDataAt(tappedMarkerIndex).getFrameId();
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
                markerView.addPlotPainter(originMarkerPainter);
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

    public void setTo(View runView, FrameDataSeekBar seekBar, MotionAnalysis analysis) {
        this.seekBar = seekBar;
        seekBarManager = new SeekBarManager();

        if (sensorAnalysis != null)
            sensorAnalysis.removeListener(motionAnalysisListener);
        sensorAnalysis = analysis;
        sensorAnalysis.addListener(motionAnalysisListener);

        if (frameDataModel != null)
            frameDataModel.removeListener(frameDataModelListener);
        frameDataModel = sensorAnalysis.getFrameDataModel();
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

        RectF range = ((IExperimentFrameView) videoAnalysisView).getDataRange();
        markerView.setRange(range);
        markerView.setMaxRange(range);

        gestureDetector = new GestureDetector(getContext(), new GestureListener());
        markerView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    public void addTagMarkerData(MarkerDataModel data) {
        painter = new TagMarkerDataModelPainter(data);
        markerView.addPlotPainter(painter);

        frameDataModelListener.onFrameChanged(frameDataModel.getCurrentFrame());
    }

    public void addXYCalibrationData(MarkerDataModel data) {
        CalibrationMarkerPainter painter = new CalibrationMarkerPainter(data);
        markerView.addPlotPainter(painter);
    }

    public void removeOriginData() {
        markerView.removePlotPainter(originMarkerPainter);
    }

    public void addOriginData(MarkerDataModel data, CalibrationXY calibrationXY) {
        originMarkerPainter = new OriginMarkerPainter(data, calibrationXY);
        if (sensorAnalysis.getShowCoordinateSystem())
            markerView.addPlotPainter(originMarkerPainter);
    }

    public void release() {
        if (markerView != null)
            markerView.release();
        if (sensorAnalysis != null)
            sensorAnalysis.removeListener(motionAnalysisListener);
        if (frameDataModel != null)
            frameDataModel.removeListener(frameDataModelListener);
    }

    /**
     * Copy resize behaviour of the videoAnalysisView.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (sensorAnalysis == null) {
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
