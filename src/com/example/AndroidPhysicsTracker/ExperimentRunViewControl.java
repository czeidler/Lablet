package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;


public class ExperimentRunViewControl extends LinearLayout {
    private int numberOfRuns = 0;

    private int currentFrame = 0;

    private TextView progressLabel = null;
    private SeekBar seekBar = null;

    private RunChangedListener runChangedListener = null;

    static abstract public class RunChangedListener {
        abstract public void onRunChanged(int run);
    }

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

    public void setOnRunChangedListener(RunChangedListener listener) {
        runChangedListener = listener;
    }

    private void notifyRunChanged(int run) {
        if (runChangedListener != null)
            runChangedListener.onRunChanged(run);
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.experiment_run_view_control, this, true);

        Button prevButton = (Button)findViewById(R.id.frameBackButton);
        Button nextButton = (Button)findViewById(R.id.nextFrameButton);
        progressLabel = (TextView)findViewById(R.id.progressLabel);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentRun(getCurrentFrame() - 1);
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setCurrentRun(getCurrentFrame() + 1);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                setCurrentRun(progress);
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

    public void setTo(int nRuns) {
        numberOfRuns = nRuns;

        setCurrentRun(0);
    }

    private void setCurrentRun(int run) {
        if (run < 0 || run >= numberOfRuns)
            return;

        currentFrame = run;

        String labelText = String.valueOf(run);
        labelText += "/";
        labelText += String.valueOf(numberOfRuns - 1);
        progressLabel.setText(labelText);

        seekBar.setMax(numberOfRuns - 1);
        seekBar.setProgress(run);
        notifyRunChanged(run);
    }

    private int getCurrentFrame() {
        return currentFrame;
    }
}
