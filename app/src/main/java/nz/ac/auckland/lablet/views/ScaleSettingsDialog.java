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
import nz.ac.auckland.lablet.experiment.Unit;

import java.util.ArrayList;
import java.util.List;


/**
 * Dialog to calibrate the length scale.
 */
public class ScaleSettingsDialog extends AlertDialog {
    final private LengthCalibrationSetter calibrationSetter;
    final private Unit xUnit;
    final private Unit yUnit;
    private CalibrationXY calibrationXY;
    private EditText lengthEditText;
    private Spinner spinnerUnit;

    public ScaleSettingsDialog(Context context, LengthCalibrationSetter lengthCalibrationSetter, Unit xUnit,
                               Unit yUnit) {
        super(context);

        this.calibrationSetter = lengthCalibrationSetter;
        this.xUnit = xUnit;
        this.yUnit = yUnit;

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
        lengthEditText.setText(Float.toString(calibrationSetter.getCalibrationValue()));

        // unit spinner
        spinnerUnit = (Spinner)contentView.findViewById(R.id.spinnerUnit);
        List<String> list = new ArrayList<String>();
        list.add("[m]");
        list.add("[cm]");
        list.add("[mm]");
        ArrayAdapter<String> unitsAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        spinnerUnit.setAdapter(unitsAdapter);
        if (xUnit.getPrefix().equals("c"))
            spinnerUnit.setSelection(1);
        if (xUnit.getPrefix().equals("m"))
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
                    xUnit.setPrefix("");
                    yUnit.setPrefix("");
                } else if (spinnerPosition == 1) {
                    xUnit.setPrefix("c");
                    yUnit.setPrefix("c");
                } else if (spinnerPosition == 2) {
                    xUnit.setPrefix("m");
                    yUnit.setPrefix("m");
                }

                dismiss();
            }
        });
    }
}
