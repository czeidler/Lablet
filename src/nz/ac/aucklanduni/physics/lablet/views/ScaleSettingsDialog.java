/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.experiment.LengthCalibrationSetter;
import nz.ac.aucklanduni.physics.lablet.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Dialog to calibrate the length scale.
 */
public class ScaleSettingsDialog extends AlertDialog {
    private LengthCalibrationSetter calibrationSetter;
    private ExperimentAnalysis experimentAnalysis;
    private EditText lengthEditText;
    private Spinner spinnerUnit;

    public ScaleSettingsDialog(Context context, ExperimentAnalysis analysis) {
        super(context);

        this.calibrationSetter = analysis.getLengthCalibrationSetter();
        this.experimentAnalysis = analysis;
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
        if (experimentAnalysis.getXUnitPrefix().equals("cm"))
            spinnerUnit.setSelection(1);
        if (experimentAnalysis.getXUnitPrefix().equals("mm"))
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
                    experimentAnalysis.setXUnitPrefix("");
                    experimentAnalysis.setYUnitPrefix("");
                } else if (spinnerPosition == 1) {
                    experimentAnalysis.setXUnitPrefix("c");
                    experimentAnalysis.setYUnitPrefix("c");
                } else if (spinnerPosition == 2) {
                    experimentAnalysis.setXUnitPrefix("m");
                    experimentAnalysis.setYUnitPrefix("m");
                }

                dismiss();
            }
        });
    }
}
