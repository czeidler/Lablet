/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.camera;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import nz.ac.aucklanduni.physics.tracker.*;
import nz.ac.aucklanduni.physics.tracker.ExperimentActivity;
import nz.ac.aucklanduni.physics.tracker.views.StartEndSeekBar;
import nz.ac.aucklanduni.physics.tracker.views.VideoFrameView;

import java.io.File;
import java.util.ArrayList;


public class CameraRunSettingsActivity extends ExperimentActivity {
    private CameraExperiment cameraExperiment;
    private VideoFrameView videoFrameView;

    private SeekBar seekBar = null;
    private StartEndSeekBar startEndSeekBar = null;
    private NumberPicker frameRatePicker = null;
    private EditText editVideoStart = null;
    private EditText editVideoEnd = null;
    private EditText editFrames = null;
    private EditText editFrameLength = null;

    private MarkersDataModel.IMarkersDataModelListener startEndSeekBarListener;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameRateList = new ArrayList<Integer>();

        Intent intent = getIntent();
        if (!loadExperiment(getIntent()))
            return;

        cameraExperiment = (CameraExperiment)getExperiment();

        setContentView(R.layout.camera_run_settings);

        videoFrameView = (VideoFrameView)findViewById(R.id.videoFrameView);
        File storageDir = cameraExperiment.getStorageDir();
        File videoFile = new File(storageDir, cameraExperiment.getVideoFileName());
        videoFrameView.setVideoFilePath(videoFile.getPath());
        videoFrameView.seekToFrame(0);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        int duration = cameraExperiment.getVideoDuration();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                int frameRate = getFrameRateFromPicker();
                float frameSize = 1000.f / frameRate;
                seekTo((int)(frameSize * progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        startEndSeekBar = (StartEndSeekBar)findViewById(R.id.startEndSeekBar);
        startEndSeekBar.setPadding(seekBar.getPaddingLeft(), seekBar.getPaddingTop(), seekBar.getPaddingRight(),
                seekBar.getPaddingBottom());

        // The marker data model keeps listeners as weak references. Thus we have to maintain our own a hard reference.
        startEndSeekBarListener = new MarkersDataModel.IMarkersDataModelListener() {
            @Override
            public void onDataAdded(MarkersDataModel model, int index) {

            }

            @Override
            public void onDataRemoved(MarkersDataModel model, int index, MarkerData data) {

            }

            @Override
            public void onDataChanged(MarkersDataModel model, int index, int number) {
                int frameRate = getFrameRateFromPicker();
                int duration = getDurationAtFrameRate(frameRate);

                int progress = 0;
                for (int i = index; i < index + number; i++) {
                    if (i == 0) {
                        progress = Math.round(model.getMarkerDataAt(i).getPosition().x * duration);
                        setVideoStart(progress);
                    } else if (i == 1){
                        progress = Math.round(model.getMarkerDataAt(i).getPosition().x * duration);
                        setVideoEnd(progress);
                    }
                }
                seekBar.setProgress(findFrame(progress));
                seekTo(progress);

                updateEditFrames();
            }

            @Override
            public void onAllDataChanged(MarkersDataModel model) {

            }

            @Override
            public void onDataSelected(MarkersDataModel model, int index) {

            }
        };
        startEndSeekBar.getMarkersDataModel().addListener(startEndSeekBarListener);

        editFrameLength = (EditText)findViewById(R.id.editFrameLength);

        frameRatePicker = (NumberPicker)findViewById(R.id.frameRatePicker);
        frameRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int old, int newValue) {
                int frameRate = frameRateList.get(newValue);
                setFrameRate(frameRate);
            }
        });

        editVideoStart = (EditText)findViewById(R.id.editStart);
        editVideoEnd = (EditText)findViewById(R.id.editEnd);
        editFrames = (EditText)findViewById(R.id.editFrames);

        // get initial values
        Bundle runSettings = null;
        Bundle extras = intent.getExtras();
        assert extras != null;
        Bundle analysisSpecificData = extras.getBundle("analysisSpecificData");
        if (analysisSpecificData != null)
            runSettings = analysisSpecificData.getBundle("run_settings");
        if (runSettings != null) {
            cameraExperiment.setFrameRate(runSettings.getInt("analysis_frame_rate"));
            cameraExperiment.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
            cameraExperiment.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        }
        int analysisFrameRate = cameraExperiment.getAnalysisFrameRate();


        // initial views with values
        calculateFrameRateValues(videoFrameView.getVideoFrameRate());
        frameRatePicker.setMinValue(0);
        frameRatePicker.setMaxValue(frameRateList.size() - 1);
        frameRatePicker.setDisplayedValues(getFrameRateStringList());
        int pickerFrameRateIndex = getNumberPickerIndexForFrameRate(analysisFrameRate);
        frameRatePicker.setValue(pickerFrameRateIndex);

        videoStartValue = cameraExperiment.getAnalysisVideoStart();
        videoEndValue = cameraExperiment.getAnalysisVideoEnd();
        setVideoStart(videoStartValue);
        setVideoEnd(videoEndValue);
        PointF point = new PointF();
        point.x = (float)videoStartValue / duration;
        startEndSeekBar.getMarkersDataModel().getMarkerDataAt(0).setPosition(point);
        point.x = (float)videoEndValue / duration;
        startEndSeekBar.getMarkersDataModel().getMarkerDataAt(1).setPosition(point);

        int frameRate = getFrameRateFromPicker();
        setFrameRate(frameRate);

        initialFrameRate = frameRate;
        initialVideoStartValue = videoStartValue;
        initialVideoEndValue = videoEndValue;
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
        int duration = cameraExperiment.getVideoDuration();
        int stepSize = Math.round(1000.0f / frameRate);
        int numberOfSteps = Math.round((float)(frameRate * duration) / 1000);
        duration = stepSize * (numberOfSteps - 1);
        return duration;
    }

    private void updateEditFrames() {
        int frameRate = getFrameRateFromPicker();
        int duration = getDurationAtFrameRate(frameRate);

        float start = startEndSeekBar.getMarkersDataModel().getMarkerDataAt(0).getPosition().x;
        start *= duration;
        float end = startEndSeekBar.getMarkersDataModel().getMarkerDataAt(1).getPosition().x;
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
        int duration = cameraExperiment.getVideoDuration();
        int numberOfSteps = Math.round((float) (frameRate * duration) / 1000);

        // seek bar
        float oldRelativeProgress = (float)seekBar.getProgress() / seekBar.getMax();
        seekBar.setMax(numberOfSteps - 1);

        startEndSeekBar.setMax(numberOfSteps - 1);

        // startEndSeek.setNumberOfSteps changes the progress bar, update it to the old value again
        seekBar.setProgress((int)(oldRelativeProgress * numberOfSteps));

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