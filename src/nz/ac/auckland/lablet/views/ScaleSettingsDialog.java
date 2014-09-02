/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.experiment.CalibrationXY;
import nz.ac.auckland.lablet.experiment.LengthCalibrationSetter;
import nz.ac.auckland.lablet.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Dialog to calibrate the length scale.
 */
public class ScaleSettingsDialog extends AlertDialog {
    private LengthCalibrationSetter calibrationSetter;
    private CalibrationXY calibrationXY;
    private EditText lengthEditText;
    private Spinner spinnerUnit;

    public ScaleSettingsDialog(Context context, LengthCalibrationSetter lengthCalibrationSetter) {
        super(context);

        this.calibrationSetter = lengthCalibrationSetter;
        calibrationXY = calibrationSetter.getCalibrationXY();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.calibration_settings, null);
        setTitle("Calibration");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        // scale length
        lengthEditText = (EditText)contentView.findViewById(R.id.lengthEditText);
        String text = "";
        text += calibrationSetter.getCalibrationValue();
        lengthEditText.setText(text);

        // unit spinner
        spinnerUnit = (Spinner)contentView.findViewById(R.id.spinnerUnit);
        List<String> list = new ArrayList<String>();
        list.add("[m]");
        list.add("[cm]");
        list.add("[mm]");
        ArrayAdapter<String> unitsAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        spinnerUnit.setAdapter(unitsAdapter);
        if (calibrationXY.getXUnit().getPrefix().equals("c"))
            spinnerUnit.setSelection(1);
        if (calibrationXY.getXUnit().getPrefix().equals("m"))
            spinnerUnit.setSelection(2);

        // button bar
        Button cancelButton = (Button)contentView.findViewById(R.id.cancelButton);
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
                float calibrationValue = Float.parseFloat(lengthEditText.getText().toString());
                calibrationSetter.setCalibrationValue(calibrationValue);

                int spinnerPosition = spinnerUnit.getSelectedItemPosition();
                if (spinnerPosition == 0) {
                    calibrationXY.getXUnit().setPrefix("");
                    calibrationXY.getYUnit().setPrefix("");
                } else if (spinnerPosition == 1) {
                    calibrationXY.getXUnit().setPrefix("c");
                    calibrationXY.getYUnit().setPrefix("c");
                } else if (spinnerPosition == 2) {
                    calibrationXY.getXUnit().setPrefix("m");
                    calibrationXY.getYUnit().setPrefix("m");
                }

                dismiss();
            }
        });
    }
}
