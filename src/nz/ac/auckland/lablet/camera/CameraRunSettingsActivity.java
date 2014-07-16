/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Intent;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.*;
import nz.ac.auckland.lablet.ExperimentDataActivity;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.StartEndSeekBar;
import nz.ac.auckland.lablet.views.VideoFrameView;

import java.io.File;
import java.util.ArrayList;


/**
 * Activity to configure the camera experiment analysis.
 * <p>
 * The user is able to set video start and end point and set the analysis video frame rate.
 * </p>
 */
public class CameraRunSettingsActivity extends ExperimentDataActivity {
    private CameraSensorData cameraSensorData;
    private VideoFrameView videoFrameView;

    private SeekBar seekBar = null;
    private StartEndSeekBar startEndSeekBar = null;

    // The marker data model keeps listeners as weak references. Thus we have to maintain our own hard reference.
    private MarkerDataModel.IMarkerDataModelListener startEndSeekBarListener = null;

    private NumberPicker frameRatePicker = null;
    private EditText editVideoStart = null;
    private EditText editVideoEnd = null;
    private EditText editFrames = null;
    private EditText editFrameLength = null;
    private CameraRunSettingsHelpView helpView = null;

    private int videoStartValue;
    private int videoEndValue;

    // cache initial values to check if values have been changed
    private int initialFrameRate;
    private int initialVideoStartValue;
    private int initialVideoEndValue;

    private ArrayList<Integer> frameRateList;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.simpel_settings_menu, menu);

        MenuItem helpItem = menu.findItem(R.id.action_help);
        assert helpItem != null;
        final CameraRunSettingsActivity that = this;
        helpItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                helpView.setVisibility(View.VISIBLE);
                // call it just in case onPrepareOptionsMenu got called to late...
                helpView.setParent(that);
                return false;
            }
        });
        // We need to set the parent when the layout of the parent is ready. It seems to be ready here. However, as far
        // as I know there is not guaranteed for that; thus a bit hacky. We only call it here for the case that the help
        // screen should be open on start. Otherwise we would just call it in the on help item clicked listener (as we
        // do anyway...).
        helpView.setParent(that);

        MenuItem backItem = menu.findItem(R.id.action_cancel);
        assert backItem != null;
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return false;
            }
        });
        MenuItem applyMenuItem = menu.findItem(R.id.action_apply);
        assert applyMenuItem != null;
        applyMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                applySettingsAndFinish();
                return true;
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public Rect getFramePickerRect() {
        return getViewRect(frameRatePicker);
    }

    public Rect getRangeSeekBarRect() {
        return getViewRect(startEndSeekBar);
    }

    private Rect getViewRect(View view) {
        Rect rect = new Rect();
        view.getDrawingRect(rect);

        View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        int[] contentViewOffset = new int[2];
        contentView.getLocationInWindow(contentViewOffset);

        int[] viewOffset = new int[2];
        view.getLocationInWindow(viewOffset);

        int offsetX = viewOffset[0] - contentViewOffset[0];
        int offsetY = viewOffset[1] - contentViewOffset[1];

        rect.offset(offsetX, offsetY);
        return rect;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameRateList = new ArrayList<>();

        Intent intent = getIntent();
        if (!loadExperiment(intent))
            return;


        cameraSensorData = (CameraSensorData)currentAnalysisSensor.analysis.getSensorData();

        setContentView(R.layout.camera_run_settings);

        videoFrameView = (VideoFrameView)findViewById(R.id.videoFrameView);
        assert videoFrameView != null;
        File storageDir = cameraSensorData.getStorageDir();
        File videoFile = new File(storageDir, cameraSensorData.getVideoFileName());
        videoFrameView.setVideoFilePath(videoFile.getPath());

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        assert seekBar != null;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                int frameRate = getFrameRateFromPicker();
                float frameSize = 1000.f / frameRate;
                seekTo((int) (frameSize * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startEndSeekBar = (StartEndSeekBar)findViewById(R.id.startEndSeekBar);
        assert startEndSeekBar != null;
        startEndSeekBar.setPadding(seekBar.getPaddingLeft(), seekBar.getPaddingTop(), seekBar.getPaddingRight(),
                seekBar.getPaddingBottom());

        startEndSeekBarListener = new MarkerDataModel.IMarkerDataModelListener() {
            @Override
            public void onDataAdded(MarkerDataModel model, int index) {

            }

            @Override
            public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

            }

            @Override
            public void onDataChanged(MarkerDataModel model, int index, int number) {
                int frameRate = getFrameRateFromPicker();
                int duration = getDurationAtFrameRate(frameRate);

                int progress = 0;
                for (int i = index; i < index + number; i++) {
                    if (i == 0) {
                        progress = Math.round(model.getMarkerDataAt(i).getPosition().x * duration);
                        setVideoStart(progress);
                    } else if (i == 1) {
                        progress = Math.round(model.getMarkerDataAt(i).getPosition().x * duration);
                        setVideoEnd(progress);
                    }
                }
                seekBar.setProgress(findFrame(progress));
                seekTo(progress);

                updateEditFrames();
            }

            @Override
            public void onAllDataChanged(MarkerDataModel model) {

            }

            @Override
            public void onDataSelected(MarkerDataModel model, int index) {

            }
        };
        startEndSeekBar.getMarkerDataModel().addListener(startEndSeekBarListener);

        editFrameLength = (EditText)findViewById(R.id.editFrameLength);
        assert editFrameLength != null;
        frameRatePicker = (NumberPicker)findViewById(R.id.frameRatePicker);
        frameRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int old, int newValue) {
                int frameRate = frameRateList.get(newValue);
                setFrameRate(frameRate);
            }
        });

        editVideoStart = (EditText)findViewById(R.id.editStart);
        assert editVideoStart != null;
        editVideoEnd = (EditText)findViewById(R.id.editEnd);
        assert editVideoEnd != null;
        editFrames = (EditText)findViewById(R.id.editFrames);
        assert editFrames != null;

        // get initial values
        Bundle runSettings = null;
        Bundle extras = intent.getExtras();
        assert extras != null;
        Bundle analysisSpecificData = extras.getBundle("analysisSpecificData");
        if (analysisSpecificData != null)
            runSettings = analysisSpecificData.getBundle("run_settings");
        if (runSettings != null) {
            cameraSensorData.setAnalysisFrameRate(runSettings.getInt("analysis_frame_rate"));
            cameraSensorData.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
            cameraSensorData.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        }
        int analysisFrameRate = cameraSensorData.getAnalysisFrameRate();

        // initial views with values
        calculateFrameRateValues(videoFrameView.getVideoFrameRate());
        frameRatePicker.setMinValue(0);
        frameRatePicker.setMaxValue(frameRateList.size() - 1);
        frameRatePicker.setDisplayedValues(getFrameRateStringList());
        int pickerFrameRateIndex = getNumberPickerIndexForFrameRate(analysisFrameRate);
        frameRatePicker.setValue(pickerFrameRateIndex);

        videoStartValue = cameraSensorData.getAnalysisVideoStart();
        videoEndValue = cameraSensorData.getAnalysisVideoEnd();
        setVideoStart(videoStartValue);
        setVideoEnd(videoEndValue);
        PointF point = new PointF();
        int duration = getDurationAtFrameRate(getFrameRateFromPicker());
        point.x = (float)videoStartValue / duration;
        startEndSeekBar.getMarkerDataModel().getMarkerDataAt(0).setPosition(point);
        point.x = (float)videoEndValue / duration;
        startEndSeekBar.getMarkerDataModel().getMarkerDataAt(1).setPosition(point);

        helpView = (CameraRunSettingsHelpView)findViewById(R.id.cameraSettingsHelp);
        assert helpView != null;
        Bundle options = intent.getBundleExtra("options");
        if (options != null && options.getBoolean("start_with_help", false))
            helpView.setVisibility(View.VISIBLE);

        int frameRate = getFrameRateFromPicker();
        initialFrameRate = frameRate;
        initialVideoStartValue = videoStartValue;
        initialVideoEndValue = videoEndValue;

        setFrameRate(frameRate);
        // setAnalysisFrameRate set the seekBar max value so set the progress afterwards
        seekBar.setProgress(findFrame(videoStartValue));
    }

    @Override
    public void onResume() {
        super.onResume();

        videoFrameView.seekToFrame(0);
    }

    @Override
    public void onBackPressed() {
        applySettingsAndFinish();
    }

    private int findFrame(int milliSeconds) {
        int frameRate = getFrameRateFromPicker();
        return Math.round((float)(frameRate * milliSeconds) / 1000);
    }

    private int getDurationAtFrameRate(int frameRate) {
        int duration = cameraSensorData.getVideoDuration();
        int stepSize = Math.round(1000.0f / frameRate);
        int numberOfSteps = (int)((float)(frameRate * duration) / 1000);
        duration = stepSize * numberOfSteps;
        return duration;
    }

    private void updateEditFrames() {
        int frameRate = getFrameRateFromPicker();
        int duration = getDurationAtFrameRate(frameRate);

        float start = startEndSeekBar.getMarkerDataModel().getMarkerDataAt(0).getPosition().x;
        start *= duration;
        float end = startEndSeekBar.getMarkerDataModel().getMarkerDataAt(1).getPosition().x;
        end *= duration;

        int numberOfFrames = (int)((frameRate * (end - start)) / 1000) + 1;
        String text = "";
        text += numberOfFrames;
        editFrames.setText(text);
    }

    private void calculateFrameRateValues(int maxFrameRate) {
        frameRateList.clear();
        frameRateList.add(1);
        for (int i = 2; i < maxFrameRate; i++) {
            if (maxFrameRate % i == 0)
                frameRateList.add(i);
        }
        frameRateList.add(maxFrameRate);
    }

    private String[] getFrameRateStringList() {
        String[] list = new String[frameRateList.size()];
        for (int i = 0; i < frameRateList.size(); i++) {
            String stringValue = "";
            stringValue += frameRateList.get(i);
            list[i] = stringValue;
        }
        return list;
    }

    private void setFrameRate(int frameRate) {
        setFrameRateLengthEdit(frameRate);
        int duration = getDurationAtFrameRate(frameRate);
        int numberOfSteps = Math.round((float) (frameRate * duration) / 1000);

        // seek bar
        float oldRelativeProgress = (float)seekBar.getProgress() / seekBar.getMax();
        seekBar.setMax(numberOfSteps);

        startEndSeekBar.setMax(numberOfSteps);

        // startEndSeek.setNumberOfSteps changes the progress bar, update it to the old value again
        seekBar.setProgress(Math.round(oldRelativeProgress * numberOfSteps));

        updateEditFrames();
    }

    private int getNumberPickerIndexForFrameRate(int frameRate) {
        for (int i = 0; i < frameRateList.size(); i++) {
            if (frameRateList.get(i) == frameRate)
                return i;
        }
        return frameRateList.size() - 1;
    }

    private void setFrameRateLengthEdit(int frameRate) {
        float length = 1.0f / frameRate * 1000;
        editFrameLength.setText(String.format("%.1f", length));
    }

    private void setVideoStart(int value) {
        videoStartValue = value;
        String string = "";
        string += value;
        editVideoStart.setText(string);
    }

    private void setVideoEnd(int value) {
        videoEndValue = value;
        String string = "";
        string += value;
        editVideoEnd.setText(string);
    }

    private void applySettingsAndFinish() {
        Intent intent = new Intent();

        intent.putExtra("run_settings_changed", settingsChanged());

        Bundle runSettings = new Bundle();
        runSettings.putInt("analysis_frame_rate", getFrameRateFromPicker());
        runSettings.putInt("analysis_video_start", videoStartValue);
        runSettings.putInt("analysis_video_end", videoEndValue);
        intent.putExtra("run_settings", runSettings);

        setResult(RESULT_OK, intent);

        finish();
    }

    private boolean settingsChanged() {
        if (initialFrameRate != getFrameRateFromPicker())
            return true;
        if (initialVideoStartValue != videoStartValue)
            return true;
        if (initialVideoEndValue != videoEndValue)
            return true;
        return false;
    }

    private int getFrameRateFromPicker() {
        return frameRateList.get(frameRatePicker.getValue());
    }

    public void seekTo(int time) {
        videoFrameView.seekToFrame(time * 1000);
    }
}