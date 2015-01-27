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


abstract public class AbstractExperimentSensor implements IExperimentSensor {
    public interface State {
        public void start();
        public boolean stop();
    }

    private ExperimentRun experimentRun;
    private WeakReference<IListener> softListener = null;
    protected boolean unsavedExperimentData = false;

    protected State previewState = null;
    protected State recordingState = null;
    protected State playbackState = null;

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
    public ExperimentRun getExperimentRun() {
        return experimentRun;
    }

    public void setListener(IListener listener) {
        this.softListener = new WeakReference<>(listener);
        stateNotifier.notifyCurrentState();
    }

    @Override
    public void setExperimentRun(ExperimentRun experimentRun) {
        this.experimentRun = experimentRun;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void startPreview() {
        unsavedExperimentData = false;
        notifyStartPreview();
        stateNotifier = new PreviewStateNotifier();

        if (previewState != null)
            previewState.start();
    }

    @Override
    public void stopPreview() {
        notifyStopPreview();
        stateNotifier = new NoneStateNotifier();

        if (previewState != null)
            previewState.stop();
    }

    @Override
    public void startRecording() throws Exception {
        notifyStartRecording();
        stateNotifier = new RecordingStateNotifier();

        if (recordingState != null)
            recordingState.start();
    }

    @Override
    public boolean stopRecording() {
        unsavedExperimentData = true;
        notifyStopRecording();
        stateNotifier = new NoneStateNotifier();

        if (recordingState != null)
            return recordingState.stop();
        return true;
    }

    @Override
    public void startPlayback() {
        notifyStartPlayback();
        stateNotifier = new PlaybackStateNotifier();

        if (playbackState != null)
            playbackState.start();
    }

    @Override
    public void stopPlayback() {
        stateNotifier = new NoneStateNotifier();
        notifyStopPlayback();

        if (playbackState != null)
            playbackState.stop();
    }

    @Override
    public void finishExperiment(boolean saveData, File storageBaseDir) throws IOException {
        if (saveData)
            storageBaseDir.mkdirs();
        unsavedExperimentData = false;
    }

    protected File getSensorDataStorage(File storageBaseDir, String name) {
        for (int i = 0; true; i++) {
            String fileName = name;
            if (i != 0)
                fileName += i;
            File file = new File(storageBaseDir, fileName);
            if (file.exists())
                continue;
            return file;
        }
    }
    
    @Override
    public boolean dataTaken() {
        return unsavedExperimentData;
    }

    private IListener getListener() {
        if (softListener == null)
            return null;
        IListener listener = softListener.get();
        if (listener == null)
            softListener = null;
        return listener;
    }

    private void notifyStartPreview() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStartPreview();
    }

    private void notifyStopPreview() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStopPreview();
    }

    private void notifyStartRecording() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStartRecording();
    }

    private void notifyStopRecording() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStopRecording();
    }

    private void notifyStartPlayback() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStartPlayback();
    }

    private void notifyStopPlayback() {
        IListener listener = getListener();
        if (listener != null)
            listener.onStopPlayback();
    }

    protected void notifySettingsChanged() {
        IListener listener = getListener();
        if (listener != null)
            listener.onSettingsChanged();
    }
}
