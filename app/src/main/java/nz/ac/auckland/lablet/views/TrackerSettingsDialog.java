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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.CalibrationXY;
import nz.ac.auckland.lablet.experiment.LengthCalibrationSetter;
import nz.ac.auckland.lablet.misc.Unit;


/**
 * Dialog to calibrate the length scale.
 */
public class TrackerSettingsDialog extends AlertDialog {

    private SeekBar seekBarVMax;
    private SeekBar seekBarVMin;
    private SeekBar seekBarSMin;

    private TextView textViewVMax;
    private TextView textViewVMin;
    private TextView textViewSMin;

    MotionAnalysis motionAnalysis;

    public TrackerSettingsDialog(Context context, MotionAnalysis motionAnalysis) {
        super(context);
        this.motionAnalysis = motionAnalysis;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.tracker_settings, null);
        setTitle("Tracker Settings");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        seekBarVMax = (SeekBar)contentView.findViewById(R.id.seekBarVMax);
        seekBarVMin = (SeekBar)contentView.findViewById(R.id.seekBarVMin);
        seekBarSMin = (SeekBar)contentView.findViewById(R.id.seekBarSMin);

        textViewVMax = (TextView)contentView.findViewById(R.id.textViewVMax);
        textViewVMin = (TextView)contentView.findViewById(R.id.textViewVMin);
        textViewSMin = (TextView)contentView.findViewById(R.id.textViewSMin);

        seekBarVMax.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarVMin.setOnSeekBarChangeListener(seekBarChangeListener);
        seekBarSMin.setOnSeekBarChangeListener(seekBarChangeListener);

//        seekBarVMax.setProgress(motionAnalysis.getTrackerVMax());
//        seekBarVMin.setProgress(motionAnalysis.getTrackerVMin());
//        seekBarSMin.setProgress(motionAnalysis.getTrackerSMin());

        Button btnDone = (Button)contentView.findViewById(R.id.trackerSettingsDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            int seekId = seekBar.getId();

//            if(seekId == R.id.seekBarVMax)
//            {
//                textViewVMax.setText("" + i);
//                motionAnalysis.setTrackerVMax(i);
//            } else if (seekId == R.id.seekBarVMin) {
//                textViewVMin.setText("" + i);
//                motionAnalysis.setTrackerVMin(i);
//            } else {
//                textViewSMin.setText("" + i);
//                motionAnalysis.setTrackerSMin(i);
//            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
