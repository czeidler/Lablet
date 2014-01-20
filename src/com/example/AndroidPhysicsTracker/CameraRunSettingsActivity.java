package com.example.AndroidPhysicsTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;


public class CameraRunSettingsActivity extends ExperimentActivity {
    private CameraExperiment cameraExperiment;
    private VideoFrameView videoFrameView;

    private SeekBar seekBar = null;
    private NumberPicker frameRatePicker = null;
    private EditText editVideoStart = null;
    private EditText editVideoEnd = null;
    private EditText editFrameLength = null;

    private int videoStartValue;
    private int videoEndValue;

    private ArrayList<Integer> frameRateList;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.simpel_settings_menu, menu);

        MenuItem backItem = menu.findItem(R.id.action_cancel);
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return false;
            }
        });
        MenuItem applyMenuItem = menu.findItem(R.id.action_apply);
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
        seekBar.setMax(duration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        editFrameLength = (EditText)findViewById(R.id.editFrameLength);

        frameRatePicker = (NumberPicker)findViewById(R.id.frameRatePicker);
        frameRatePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int old, int newValue) {
                setFrameRateLengthEdit(frameRateList.get(newValue));
            }
        });

        editVideoStart = (EditText)findViewById(R.id.editStart);
        editVideoEnd = (EditText)findViewById(R.id.editEnd);

        // get initial values
        Bundle runSettings = null;
        Bundle analysisSpecificData = intent.getExtras().getBundle("analysisSpecificData");
        if (analysisSpecificData != null)
            runSettings = analysisSpecificData.getBundle("run_settings");
        if (runSettings != null) {
            cameraExperiment.setFrameRate(runSettings.getInt("analysis_frame_rate"));
            cameraExperiment.setAnalysisVideoStart(runSettings.getInt("analysis_video_start"));
            cameraExperiment.setAnalysisVideoEnd(runSettings.getInt("analysis_video_end"));
        }
        int analysisFrameRate = cameraExperiment.getAnalysisFrameRate();
        videoStartValue = cameraExperiment.getAnalysisVideoStart();
        videoEndValue = cameraExperiment.getAnalysisVideoEnd();

        // initial views with values
        calculateFrameRateValues(videoFrameView.getVideoFrameRate());
        frameRatePicker.setMinValue(0);
        frameRatePicker.setMaxValue(frameRateList.size() - 1);
        frameRatePicker.setDisplayedValues(getFrameRateStringList());
        int pickerFrameRateIndex = getNumberPickerIndexForFrameRate(analysisFrameRate);
        frameRatePicker.setValue(pickerFrameRateIndex);
        setFrameRateLengthEdit(frameRateList.get(pickerFrameRateIndex));
        setVideoStart(videoStartValue);
        setVideoEnd(videoEndValue);

        Button setStartButton = (Button)findViewById(R.id.buttonSetStart);
        setStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (seekBar.getProgress() >= videoEndValue) {
                    toastMessage("Start value must be smaller than the end value!");
                    return;
                }
                videoStartValue = seekBar.getProgress();
                setVideoStart(videoStartValue);
            }
        });

        Button setEndButton = (Button)findViewById(R.id.buttonSetEnd);
        setEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoStartValue >= seekBar.getProgress()) {
                    toastMessage("Start value must be smaller than the end value!");
                    return;
                }
                videoEndValue = seekBar.getProgress();
                setVideoEnd(videoEndValue);
            }
        });
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

    private int getNumberPickerIndexForFrameRate(int frameRate) {
        for (int i = 0; i < frameRateList.size(); i++) {
            if (frameRateList.get(i) == frameRate)
                return i;
        }
        return frameRateList.size() - 1;
    }

    private void setFrameRateLengthEdit(int frameRate) {
        float length = 1.0f / frameRate * 1000;
        String stringValue = "";
        stringValue += (int)length;
        editFrameLength.setText(stringValue);
    }

    private void setVideoStart(int value) {
        String string = "";
        string += value;
        editVideoStart.setText(string);
    }

    private void setVideoEnd(int value) {
        String string = "";
        string += value;
        editVideoEnd.setText(string);
    }

    private void applySettingsAndFinish() {
        Intent intent = new Intent();

        intent.putExtra("analysis_frame_rate", frameRateList.get(frameRatePicker.getValue()));
        intent.putExtra("analysis_video_start", Integer.parseInt(editVideoStart.getText().toString()));
        intent.putExtra("analysis_video_end", Integer.parseInt(editVideoEnd.getText().toString()));

        setResult(RESULT_OK, intent);

        finish();
    }

    public void seekTo(int time) {
        videoFrameView.seekToFrame(time * 1000);
    }

    protected void toastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}