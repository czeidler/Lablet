/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.FrameDataModel;


/**
 * Seek bar to work with the {@link nz.ac.auckland.lablet.experiment.FrameDataModel}.
 */
public class FrameDataSeekBar extends LinearLayout implements FrameDataModel.IFrameDataModelListener {
    private FrameDataModel frameDataModel = null;

    private TextView progressLabel = null;
    private SeekBar seekBar = null;

    @Override
    public void onFrameChanged(int newFrame) {
        updateViews();
    }

    @Override
    public void onNumberOfFramesChanged() {
        updateViews();
    }

    public FrameDataSeekBar(Context context) {
        super(context);

        init(context);
    }

    public FrameDataSeekBar(Context context, AttributeSet attrs) {
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
                frameDataModel.setCurrentFrame(frameDataModel.getCurrentFrame() - 1);
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                frameDataModel.setCurrentFrame(frameDataModel.getCurrentFrame() + 1);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser)
                    return;
                frameDataModel.setCurrentFrame(progress);
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

    public void setTo(FrameDataModel model) {
        if (frameDataModel != null)
            frameDataModel.removeListener(this);

        frameDataModel = model;
        frameDataModel.addListener(this);

        updateViews();
    }

    private void updateViews() {
        int run = frameDataModel.getCurrentFrame();
        String labelText = String.valueOf(run);
        labelText += "/";
        labelText += String.valueOf(frameDataModel.getNumberOfFrames() - 1);
        progressLabel.setText(labelText);

        seekBar.setMax(frameDataModel.getNumberOfFrames() - 1);
        seekBar.setProgress(run);
    }
}
