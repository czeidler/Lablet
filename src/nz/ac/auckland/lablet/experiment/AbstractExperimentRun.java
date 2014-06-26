/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;


abstract public class AbstractExperimentRun implements IExperimentRun {
    private ExperimentRunGroup experimentRunGroup;
    private WeakReference<IExperimentRunListener> softListener = null;

    protected boolean unsavedExperimentData = false;

    private StateNotifier stateNotifier = new NoneStateNotifier();

    abstract class StateNotifier {
        abstract public void notifyCurrentState();
    }

    class NoneStateNotifier extends StateNotifier {
        @Override
        public void notifyCurrentState() {
        }
    }

    class PreviewStateNotifier extends StateNotifier {
        @Override
        public void notifyCurrentState() {
            notifyStartPreview();
        }
    }

    class RecordingStateNotifier extends StateNotifier {
        @Override
        public void notifyCurrentState() {
            notifyStartRecording();
        }
    }

    class PlaybackStateNotifier extends StateNotifier {
        @Override
        public void notifyCurrentState() {
            notifyStartPlayback();
        }
    }

    @Override
    public ExperimentRunGroup getExperimentRunGroup() {
        return experimentRunGroup;
    }

    public void setListener(IExperimentRunListener listener) {
        this.softListener = new WeakReference<IExperimentRunListener>(listener);
        stateNotifier.notifyCurrentState();
    }

    @Override
    public void setExperimentRunGroup(ExperimentRunGroup experimentRunGroup) {
        this.experimentRunGroup = experimentRunGroup;
    }

    @Override
    public void startPreview() {
        unsavedExperimentData = false;
        notifyStartPreview();
        stateNotifier = new PreviewStateNotifier();
    }

    @Override
    public void stopPreview() {
        notifyStopPreview();
        stateNotifier = new NoneStateNotifier();
    }

    @Override
    public void startRecording() throws Exception {
        notifyStartRecording();
        stateNotifier = new RecordingStateNotifier();
    }

    @Override
    public boolean stopRecording() {
        unsavedExperimentData = true;
        notifyStopRecording();
        stateNotifier = new NoneStateNotifier();
        return true;
    }

    @Override
    public void startPlayback() {
        notifyStartPlayback();
        stateNotifier = new PlaybackStateNotifier();
    }

    @Override
    public void stopPlayback() {
        stateNotifier = new NoneStateNotifier();
        notifyStopPlayback();
    }

    @Override
    public void finishExperiment(boolean saveData, File storageDir) throws IOException {
        unsavedExperimentData = false;
    }

    @Override
    public boolean dataTaken() {
        return unsavedExperimentData;
    }

    private IExperimentRunListener getListener() {
        if (softListener == null)
            return null;
        IExperimentRunListener listener = softListener.get();
        if (listener == null)
            softListener = null;
        return listener;
    }

    private void notifyStartPreview() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStartPreview();
    }

    private void notifyStopPreview() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStopPreview();
    }

    private void notifyStartRecording() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStartRecording();
    }

    private void notifyStopRecording() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStopRecording();
    }

    private void notifyStartPlayback() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStartPlayback();
    }

    private void notifyStopPlayback() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onStopPlayback();
    }

    protected void notifySettingsChanged() {
        IExperimentRunListener listener = getListener();
        if (listener != null)
            listener.onSettingsChanged();
    }
}
