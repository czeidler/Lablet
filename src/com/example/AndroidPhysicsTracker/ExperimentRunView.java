package com.example.AndroidPhysicsTracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ExperimentRunView extends LinearLayout {
    private ExperimentRun run = null;
    private Button backButton = null;
    private Button doneButton = null;

    public ExperimentRunView(Context context, int layoutResId) {
        super(context);
        setOrientation(VERTICAL);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View child = layoutInflater.inflate(layoutResId, null);

        addView(child);

        LinearLayout buttonLayout = new LinearLayout(context);
        backButton = new Button(context);
        backButton.setText("Back");
        buttonLayout.addView(backButton);

        doneButton = new Button(context);
        doneButton.setText("Done");
        buttonLayout.addView(doneButton);

        addView(buttonLayout);

    }

    public void setRun(ExperimentRun newRun) {
        run = newRun;
        invalidate();
    }

    public ExperimentRun getRun() {
        return run;
    }


}
