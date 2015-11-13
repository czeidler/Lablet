/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.FrameDataModel;

import java.text.DecimalFormat;


/**
 * Seek bar to work with the {@link nz.ac.auckland.lablet.experiment.FrameDataModel}.
 */
public class FrameDataSeekBar extends LinearLayout implements FrameDataModel.IListener {

    public interface IListener {
        void onActionBtnClicked();
    }

    public enum Action {
        PLAY, STOP, PAUSE
    }

    private FrameDataModel frameDataModel = null;
    private ITimeData timeData = null;
    private IListener listener;

    private TextView progressLabel = null;
    private TextView timeLabel = null;
    private SeekBar seekBar = null;

    private long lastTouchEvent = 0;
    private Action currentAction = Action.PLAY;

    public void setListener(FrameDataSeekBar.IListener listener)
    {
        this.listener = listener;
    }

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

    public void setAction(Action action) {
        final ImageButton actionButton = (ImageButton)findViewById(R.id.frameActionBtn);
        currentAction = action;

        if(action == Action.STOP)
            actionButton.setImageResource(R.drawable.ic_media_stop_big);
        else if(action == Action.PLAY)
            actionButton.setImageResource(R.drawable.ic_media_play_big);
        else if(action == Action.PAUSE)
            actionButton.setImageResource(R.drawable.ic_media_pause_big);
    }

    public Action getCurrentAction()
    {
        return currentAction;
    }

    class FastSeeker {
        long longPressStart;
        final ImageButton button;
        final Handler handler = new Handler();
        final int updateInterval = 600;
        final int direction;
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!button.isPressed())
                    return;

                float updatesPerSecond = 1000f / updateInterval;
                float progress = 5f / updatesPerSecond;
                // acceleration
                long time = System.currentTimeMillis();
                if (time - longPressStart > 2000)
                    progress *= 2;
                if (time - longPressStart > 4000)
                    progress *= 3;
                if (time - longPressStart > 6000)
                    progress *= 5;

                if (progress < 1)
                    progress = 1;

                frameDataModel.setCurrentFrame(frameDataModel.getCurrentFrame() + direction * (int)progress);

                handler.postDelayed(this, updateInterval);
            }
        };

        public FastSeeker(ImageButton button, int direction) {
            this.button = button;
            this.direction = direction;
        }

        public void start() {
            longPressStart = System.currentTimeMillis();
            handler.post(runnable);
        }
    }

    private void init(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.frame_data_seek_bar, this, true);

        final ImageButton prevButton = (ImageButton)findViewById(R.id.frameBackButton);
        final ImageButton actionButton = (ImageButton)findViewById(R.id.frameActionBtn);
        final ImageButton nextButton = (ImageButton)findViewById(R.id.nextFrameButton);
        progressLabel = (TextView)findViewById(R.id.progressLabel);
        timeLabel = (TextView)findViewById(R.id.timeLabel);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        prevButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                frameDataModel.setCurrentFrame(frameDataModel.getCurrentFrame() - 1);
            }
        });
        prevButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                FastSeeker seeker = new FastSeeker(prevButton, -1);
                seeker.start();
                return true;
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                frameDataModel.setCurrentFrame(frameDataModel.getCurrentFrame() + 1);
            }
        });
        nextButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                FastSeeker seeker = new FastSeeker(nextButton, 1);
                seeker.start();
                return true;
            }
        });

        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onActionBtnClicked();
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

    public void setTo(FrameDataModel model, ITimeData timeData) {
        this.timeData = timeData;

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

        DecimalFormat df = new DecimalFormat("#.###");
        df.setMinimumFractionDigits(3);

        int frameTime = (int)timeData.getTimeAt(run);
        if (frameTime < 10000)
            timeLabel.setText("" + frameTime + " [ms]");
        else
            timeLabel.setText(df.format((float)frameTime / 1000) + " [s]");

        seekBar.setMax(frameDataModel.getNumberOfFrames() - 1);
        seekBar.setProgress(run);
    }

    public long getLastTouchEvent() {
        return lastTouchEvent;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastTouchEvent = ev.getEventTime();
        return super.dispatchTouchEvent(ev);
    }
}
