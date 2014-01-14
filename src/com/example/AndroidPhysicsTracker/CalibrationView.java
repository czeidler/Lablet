package com.example.AndroidPhysicsTracker;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class CalibrationView extends AlertDialog {
    private LengthCalibrationSetter calibrationSetter;
    private EditText lengthEditText;

    protected CalibrationView(Context context, LengthCalibrationSetter calibrationSetter) {
        super(context);

        this.calibrationSetter = calibrationSetter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.calibration_settings, null);
        setTitle("Calibration");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        lengthEditText = (EditText)contentView.findViewById(R.id.lengthEditText);
        String text = new String();
        text += calibrationSetter.getCalibrationValue();
        lengthEditText.setText(text);

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
                dismiss();
            }
        });

    }
}
