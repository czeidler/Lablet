package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import java.io.InputStream;


public class ExperimentRunViewControl extends LinearLayout {
    private Experiment experiment = null;
    private IExperimentRunView runView = null;

    private int currentFrame = 0;

    private TextView progressLabel = null;
    private SeekBar seekBar = null;

    public ExperimentRunViewControl(Context context) {
        super(context);

        init(context);
    }

    public ExperimentRunViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public ExperimentRunViewControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.experimentrunviewcontrol, this, true);

        Button prevButton = (Button)findViewById(R.id.frameBackButton);
        Button nextButton = (Button)findViewById(R.id.nextFrameButton);
        progressLabel = (TextView)findViewById(R.id.progressLabel);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentFrame(getCurrentFrame() - 1);
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentFrame(getCurrentFrame() + 1);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                setCurrentFrame(progress);
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

    public void setTo(Experiment experiment, IExperimentRunView runView) {
        this.experiment = experiment;
        this.runView = runView;

        setCurrentFrame(0);
    }

    private void setCurrentFrame(int frame) {
        if (experiment == null)
            return;

        if (frame < 0 || frame >= experiment.getNumberOfRuns())
            return;

        currentFrame = frame;

        String labelText = String.valueOf(frame);
        labelText += "/";
        labelText += String.valueOf(experiment.getNumberOfRuns() - 1);
        progressLabel.setText(labelText);

        seekBar.setMax(experiment.getNumberOfRuns() - 1);
        seekBar.setProgress(frame);
        runView.setCurrentRun(experiment.getRunAt(frame));
    }

    private int getCurrentFrame() {
        return currentFrame;
    }
}
