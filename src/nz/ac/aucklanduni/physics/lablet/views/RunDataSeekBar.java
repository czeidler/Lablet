/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.aucklanduni.physics.lablet.R;
import nz.ac.aucklanduni.physics.lablet.experiment.RunDataModel;


/**
 * Seek bar to work with the {@link nz.ac.aucklanduni.physics.lablet.experiment.RunDataModel}.
 */
public class RunDataSeekBar extends LinearLayout implements RunDataModel.IRunDataModelListener {
    private RunDataModel runDataModel = null;

    private TextView progressLabel = null;
    private SeekBar seekBar = null;

    @Override
    public void onRunChanged(int newRun) {
        updateViews();
    }

    @Override
    public void onNumberOfRunsChanged() {
        updateViews();
    }

    public RunDataSeekBar(Context context) {
        super(context);

        init(context);
    }

    public RunDataSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.experiment_run_view_control, this, true);

        ImageButton prevButton = (ImageButton)findViewById(R.id.frameBackButton);
        ImageButton nextButton = (ImageButton)findViewById(R.id.nextFrameButton);
        progressLabel = (TextView)findViewById(R.id.progressLabel);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                runDataModel.setCurrentRun(runDataModel.getCurrentRun() - 1);
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                runDataModel.setCurrentRun(runDataModel.getCurrentRun() + 1);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                runDataModel.setCurrentRun(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        progressLabel.setText("--/--");
    }

    public void setTo(RunDataModel model) {
        if (runDataModel != null)
            runDataModel.removeListener(this);

        runDataModel = model;
        runDataModel.addListener(this);

        updateViews();
    }

    private void updateViews() {
        int run = runDataModel.getCurrentRun();
        String labelText = String.valueOf(run);
        labelText += "/";
        labelText += String.valueOf(runDataModel.getNumberOfRuns() - 1);
        progressLabel.setText(labelText);

        seekBar.setMax(runDataModel.getNumberOfRuns() - 1);
        seekBar.setProgress(run);
    }
}
