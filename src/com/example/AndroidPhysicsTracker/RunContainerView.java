package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class RunContainerView extends RelativeLayout {
    private View experimentRunView = null;
    private MarkerView markerView = null;

    public RunContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void setRunView(View view, Experiment experiment) {
        this.experimentRunView = view;

        RelativeLayout.LayoutParams runViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        addView(experimentRunView, runViewParams);

        RelativeLayout.LayoutParams makerViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        makerViewParams.addRule(RelativeLayout.ALIGN_LEFT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_TOP, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_RIGHT, experimentRunView.getId());
        makerViewParams.addRule(RelativeLayout.ALIGN_BOTTOM, experimentRunView.getId());

        markerView = new MarkerView(getContext(), experimentRunView);
        markerView.setTagMarkers(experiment.getTagMarkers());
        addView(markerView, makerViewParams);
    }

    public void setExperimentRunViewControl(ExperimentRunViewControl control) {
        control.setOnRunChangedListener(new ExperimentRunViewControl.RunChangedListener() {
            @Override
            public void onRunChanged(int run) {
                ((IExperimentRunView)experimentRunView).setCurrentRun(run);
                markerView.setCurrentRun(run);
            }
        });
    }
}
