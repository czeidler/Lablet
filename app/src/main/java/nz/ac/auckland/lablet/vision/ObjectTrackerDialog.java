/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 */
package nz.ac.auckland.lablet.vision;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import nz.ac.auckland.lablet.vision.ObjectTrackerAnalysis;
import org.opencv.core.Rect;

import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.camera.CalibrationVideoTimeData;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.vision.CamShiftTracker;


/**
 * Dialog to calibrate the length scale.
 */
public class ObjectTrackerDialog extends AlertDialog {

    private ProgressBar progressBar;
    private TextView textViewProgress;
    MotionAnalysis motionAnalysis;

    public ObjectTrackerDialog(Context context, MotionAnalysis motionAnalysis) {
        super(context);
        this.motionAnalysis = motionAnalysis;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.tracker_dialogue, null);
        setTitle("Tracking object");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        progressBar = (ProgressBar)contentView.findViewById(R.id.progressBarTracker);
        textViewProgress = (TextView)contentView.findViewById(R.id.textTrackerPercent);

        progressBar.setProgress(0);
        textViewProgress.setText("0%");

        CalibrationVideoTimeData timeData = motionAnalysis.getCalibrationVideoTimeData();
        int start = timeData.getClosestFrame(timeData.getAnalysisVideoStart());
        int end = timeData.getClosestFrame(timeData.getAnalysisVideoEnd());
        motionAnalysis.getObjectTrackerAnalysis().trackObjects(start, end, trackingListener);

        Button btnStop = (Button)contentView.findViewById(R.id.btnStopTracking);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                motionAnalysis.getObjectTrackerAnalysis().stopTracking();
                dismiss();
            }
        });
    }

    private final ObjectTrackerAnalysis.IListener trackingListener = new ObjectTrackerAnalysis.IListener() {
        @Override
        public void onTrackingFinished(SparseArray<Rect> results) {
            dismiss();
        }

        @Override
        public void onTrackingUpdate(Double percentDone) {
            int percent = (int)(percentDone * 100);
            progressBar.setProgress(percent);
            textViewProgress.setText(percent + "%");
        }
    };
}
