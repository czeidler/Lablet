/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class FrameRateSettingsDialog extends AlertDialog {
    final private List<Float> frameRateList;
    final private CameraExperimentSensor cameraExperimentSensor;
    private NumberPicker numberPicker;

    public FrameRateSettingsDialog(Context context, CameraExperimentSensor cameraExperimentSensor) {
        super(context);

        this.cameraExperimentSensor = cameraExperimentSensor;

        frameRateList = cameraExperimentSensor.getListOfAllowedFrameRates();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.recording_frame_rate_settings, null);
        setTitle("Recording Frame Rate");

        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        numberPicker = (NumberPicker)contentView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(frameRateList.size() - 1);
        numberPicker.setDisplayedValues(getIntervalStringList());
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setCaptureRate(cameraExperimentSensor.getRecordingFrameRate());

        // button bar
        Button cancelButton = (Button)contentView.findViewById(R.id.dismissButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Button applyButton = (Button)contentView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float captureRate = getCaptureRate();
                cameraExperimentSensor.setRecordingFrameRate(captureRate);

                dismiss();
            }
        });
    }

    private String[] getIntervalStringList() {
        String[] list = new String[frameRateList.size()];
        for (int i = 0; i < frameRateList.size(); i++)
            list[i] = new DecimalFormat("#.##").format(frameRateList.get(i));
        return list;
    }

    private float getCaptureRate() {
        return frameRateList.get(numberPicker.getValue());
    }

    private void setCaptureRate(float captureRate) {
        // find best matching interval
        float minDiff = Float.MAX_VALUE;
        int bestMatch = 0;
        for (int i = 0; i < frameRateList.size(); i++) {
            final float inListInterval = frameRateList.get(i);
            final float diff = Math.abs(inListInterval - captureRate);
            if (diff < minDiff) {
                minDiff = diff;
                bestMatch = i;
            }
        }
        numberPicker.setValue(bestMatch);
    }
}
