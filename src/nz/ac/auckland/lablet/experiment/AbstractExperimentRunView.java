/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


abstract public class AbstractExperimentRunView extends FrameLayout implements IExperimentRun.IExperimentRunListener {
    protected AbstractExperimentRun.State previewState = null;
    protected AbstractExperimentRun.State recordingState = null;
    protected AbstractExperimentRun.State playbackState = null;

    public AbstractExperimentRunView(Context context) {
        super(context);
    }

    public AbstractExperimentRunView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onStartPreview() {
        if (previewState != null)
            previewState.start();
    }

    @Override
    public void onStopPreview() {
        if (previewState != null)
            previewState.stop();
    }

    @Override
    public void onStartRecording() {
        if (recordingState != null)
            recordingState.start();
    }

    @Override
    public void onStopRecording() {
        if (recordingState != null)
            recordingState.stop();
    }

    @Override
    public void onStartPlayback() {
        if (playbackState != null)
            playbackState.start();
    }

    @Override
    public void onStopPlayback() {
        if (playbackState != null)
            playbackState.stop();
    }
}
