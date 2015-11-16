/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      James Diprose <jamie.diprose@gmail.com>
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;


import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import nz.ac.auckland.lablet.R;
import org.opencv.core.Rect;


public class MotionTrackingStatusView extends LinearLayout {
    private TextView timeLeft;
    private TextView timeElapsed;
    private ObjectTrackerAnalysis objectTrackerAnalysis;
    final private ObjectTrackerAnalysis.IListener trackerListener = new ObjectTrackerAnalysis.IListener() {
        @Override
        public void onTrackingStart() {
            setVisibility(VISIBLE);

            timeElapsed.setText("Time elapsed: 0");
            timeLeft.setText("Time left: ");
        }

        @Override
        public void onTrackingFinished(SparseArray<Rect> results) {
            setVisibility(GONE);
        }

        @Override
        public void onTrackingUpdate(int frameNumber, int totalNumberOfFrames) {
            long elapsedTime = objectTrackerAnalysis.getElapsedTime();
            timeElapsed.setText("Time elapsed: " + timeString(elapsedTime));
            timeLeft.setText("Time left: " + timeString(elapsedTime * (totalNumberOfFrames + 1 - frameNumber)
                    / (frameNumber + 1)));
        }
    };

    public MotionTrackingStatusView(Context context) {
        super(context);
        init(context);
    }

    public MotionTrackingStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mainView = inflater.inflate(R.layout.motion_tracking_status, this, true);

        timeElapsed = (TextView)mainView.findViewById(R.id.timeElapsed);
        timeLeft = (TextView)mainView.findViewById(R.id.timeLeft);

        setVisibility(View.GONE);
    }

    private String timeString(long timeMs) {
        int seconds = (int)(timeMs / 1000);
        int hours = seconds / 60 / 60;
        seconds -= hours * 60 * 60;
        int minutes = seconds / 60;
        seconds -= minutes * 60;

        String string = "";
        if (hours > 0)
            string += hours + "h ";
        if (minutes > 0)
            string += minutes + "m ";
        string += seconds + "s";
        return string;
    }

    public void setObjectTrackerAnalysis(ObjectTrackerAnalysis objectTrackerAnalysis) {
        if (this.objectTrackerAnalysis != null) {
            this.objectTrackerAnalysis.removeListener(trackerListener);
        }
        this.objectTrackerAnalysis = objectTrackerAnalysis;
        this.objectTrackerAnalysis.addListener(trackerListener);
    }
}
