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
import nz.ac.auckland.lablet.ExperimentAnalysisBaseActivity;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.StartEndSeekBar;
import nz.ac.auckland.lablet.views.VideoFrameView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

/**
 * Activity to configure the camera experiment analysis.
 * <p>
 * The user is able to set video start and end point and set the analysis video frame rate.
 * </p>
 */
public class MotionAnalysisSettingsActivity extends ExperimentAnalysisBaseActivity {
    private CameraSensorData cameraSensorData;
    private VideoFrameView videoFrameView;

    private SeekBar seekBar = null;
    private StartEndSeekBar startEndSeekBar = null;

    // The marker data model keeps listeners as weak references. Thus we have to maintain our own hard reference.
    private MarkerDataModel.IListener startEndSeekBarListener = null;

    private NumberPicker frameRatePicker = null;
    private EditText editVideoStart = null;
    private EditText editVideoEnd = null;
    private EditText editFrames = null;
    private EditText editFrameLength = null;
    private MotionAnalysisSettingsHelpView helpView = null;

    private float videoStartValue;
    private float videoEndValue;

    // cache initial values to check if values have been changed
    private float initialFrameRate;
    private float initialVideoStartValue;
    private float initialVideoEndValue;

    private List<Float> frameRateList;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.simpel_settings_menu, menu);

        MenuItem helpItem = menu.findItem(R.id.action_help);
        assert helpItem != null;
        final MotionAnalysisSettingsActivity that = this;
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

        cameraSensorData = (CameraSensorData)experimentAnalysis.getCurrentSensorAnalysis().getData();

        setContentView(R.layout.motion_analysis_settings);

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
                float frameRate = getFrameRateFromPicker();
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

        startEndSeekBarListener = new MarkerDataModel.IListener() {
            @Override
            public void onDataAdded(MarkerDataModel model, int index) {

            }

            @Override
            public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

            }

            @Override
            public void onDataChanged(MarkerDataModel model, int index, int number) {
                float frameRate = getFrameRateFromPicker();
                float duration = getDurationAtFrameRate(frameRate);

                float progress = 0;
                for (int i = index; i < index + number; i++) {
                    if (i == 0) {
                        progress = model.getMarkerDataAt(i).getPosition().x * duration;
                        setVideoStart(progress);
                    } else if (i == 1) {
                        progress = model.getMarkerDataAt(i).getPosition().x * duration;
                        setVideoEnd(progress);
                    }
                }
                seekBar.setProgress(findFrame(progress));
                seekTo(Math.round(progress));

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
                float frameRate = frameRateList.get(newValue);
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
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        Bundle analysisSpecificData = extras.getBundle("analysisSpecificData");
        if (analysisSpecificData != null)
            runSettings = analysisSpecificData.getBundle("run_settings");
        MotionAnalysis motionAnalysis = (MotionAnalysis)experimentAnalysis.getCurrentSensorAnalysis();
        CalibrationVideoTimeData calibrationVideoTimeData = motionAnalysis.getCalibrationVideoTimeData();
        if (runSettings != null) {
            calibrationVideoTimeData.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
            calibrationVideoTimeData.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
            calibrationVideoTimeData.setAnalysisFrameRate(runSettings.getFloat("analysis_frame_rate"));
        }
        float analysisFrameRate = calibrationVideoTimeData.getAnalysisFrameRate();

        // initial views with values
        if (cameraSensorData.isRecordedAtReducedFrameRate())
            frameRateList = FrameRateHelper.getPossibleLowAnalysisFrameRates(cameraSensorData.getRecordingFrameRate());
        else
            frameRateList = FrameRateHelper.getPossibleAnalysisFrameRates(cameraSensorData.getVideoFrameRate());
        frameRatePicker.setMinValue(0);
        frameRatePicker.setMaxValue(frameRateList.size() - 1);
        frameRatePicker.setDisplayedValues(getFrameRateStringList());
        int pickerFrameRateIndex = getNumberPickerIndexForFrameRate(analysisFrameRate);
        frameRatePicker.setValue(pickerFrameRateIndex);

        videoStartValue = calibrationVideoTimeData.getAnalysisVideoStart();
        videoEndValue = calibrationVideoTimeData.getAnalysisVideoEnd();
        setVideoStart(videoStartValue);
        setVideoEnd(videoEndValue);
        PointF point = new PointF();
        float duration = getDurationAtFrameRate(getFrameRateFromPicker());
        // because the duration is for a certain frame rate it can be smaller than the actual video length
        if (point.x > 1)
            point.x = 1;
        point.x = videoStartValue / duration;
        startEndSeekBar.getMarkerDataModel().getMarkerDataAt(0).setPosition(point);
        point.x = videoEndValue / duration;
        if (point.x > 1)
            point.x = 1;
        startEndSeekBar.getMarkerDataModel().getMarkerDataAt(1).setPosition(point);

        helpView = (MotionAnalysisSettingsHelpView)findViewById(R.id.cameraSettingsHelp);
        assert helpView != null;
        Bundle options = getIntent().getBundleExtra("options");
        if (options != null && options.getBoolean("start_with_help", false))
            helpView.setVisibility(View.VISIBLE);

        float frameRate = getFrameRateFromPicker();
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

        if (videoFrameView != null)
            videoFrameView.seekToFrame(0);
    }

    @Override
    public void onBackPressed() {
        applySettingsAndFinish();
    }

    private int findFrame(float milliSeconds) {
        float frameRate = getFrameRateFromPicker();
        return Math.round(frameRate * milliSeconds / 1000);
    }

    private float getDurationAtFrameRate(float frameRate) {
        long duration = cameraSensorData.getVideoDuration();
        float stepSize = 1000.0f / frameRate;
        int numberOfSteps = (int)((frameRate * duration) / 1000);
        return stepSize * numberOfSteps;
    }

    private void updateEditFrames() {
        float frameRate = getFrameRateFromPicker();
        float duration = getDurationAtFrameRate(frameRate);

        float start = startEndSeekBar.getMarkerDataModel().getMarkerDataAt(0).getPosition().x;
        start *= duration;
        float end = startEndSeekBar.getMarkerDataModel().getMarkerDataAt(1).getPosition().x;
        end *= duration;

        int numberOfFrames = (int)((frameRate * (end - start)) / 1000) + 1;
        String text = "";
        text += numberOfFrames;
        editFrames.setText(text);
    }

    private String[] getFrameRateStringList() {
        String[] list = new String[frameRateList.size()];
        for (int i = 0; i < frameRateList.size(); i++)
            list[i] = new DecimalFormat("#.###").format(frameRateList.get(i));
        return list;
    }

    private void setFrameRate(float frameRate) {
        setFrameRateLengthEdit(frameRate);
        float duration = getDurationAtFrameRate(frameRate);
        int numberOfSteps = Math.round((duration * frameRate) / 1000);

        // seek bar
        float oldRelativeProgress = (float)seekBar.getProgress() / seekBar.getMax();
        seekBar.setMax(numberOfSteps);

        startEndSeekBar.setMax(numberOfSteps);

        // startEndSeek.setNumberOfSteps changes the progress bar, update it to the old value again
        seekBar.setProgress(Math.round(oldRelativeProgress * numberOfSteps));

        updateEditFrames();
    }

    private int getNumberPickerIndexForFrameRate(float frameRate) {
        for (int i = 0; i < frameRateList.size(); i++) {
            if (frameRateList.get(i) == frameRate)
                return i;
        }
        return frameRateList.size() - 1;
    }

    private void setFrameRateLengthEdit(float frameRate) {
        float length = 1.0f / frameRate * 1000;
        editFrameLength.setText(String.format("%.1f", length));
    }

    private int toDisplayTime(float videoTime) {
        return Math.round(videoTime);
    }

    private void setVideoStart(float value) {
        videoStartValue = value;
        String string = "";
        string += toDisplayTime(value);
        editVideoStart.setText(string);
    }

    private void setVideoEnd(float value) {
        videoEndValue = value;
        String string = "";
        string += toDisplayTime(value);
        editVideoEnd.setText(string);
    }

    private void applySettingsAndFinish() {
        Intent intent = new Intent();

        intent.putExtra("run_settings_changed", settingsChanged());

        Bundle runSettings = new Bundle();
        runSettings.putFloat("analysis_frame_rate", getFrameRateFromPicker());
        runSettings.putFloat("analysis_video_start", videoStartValue);
        runSettings.putFloat("analysis_video_end", videoEndValue);
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

    private float getFrameRateFromPicker() {
        return frameRateList.get(frameRatePicker.getValue());
    }

    public void seekTo(int time) {
        videoFrameView.seekToFrame(time * 1000);
    }
}