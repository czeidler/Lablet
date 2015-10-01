/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.TextView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.accelerometer.AccelerometerSensorPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.script.Script;

import java.io.File;


public class AccelerometerExperiment extends SingleExperimentBase {

    public AccelerometerExperiment(Script script) {
        super(script);
        setDescriptionText("Please take an accelerometer recording:");
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentAccExperimentView(context, (ScriptComponentSheetFragment)parent, this);
    }
}

/**
 * View to start a camera experiment activity.
 */
class ScriptComponentAccExperimentView extends ScriptComponentSingleExperimentBaseView {
    private AccelerometerExperiment accelerometerExperiment;
    private CheckedTextView takenExperimentInfo = null;

    public ScriptComponentAccExperimentView(Context context, ScriptComponentSheetFragment sheetFragment,
                                            AccelerometerExperiment accelerometerExperiment) {
        super(context, sheetFragment, accelerometerExperiment);
        this.accelerometerExperiment = accelerometerExperiment;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.script_component_accelerometer_experiment, null, false);
        assert view != null;

        addView(view);

        TextView descriptionTextView = (TextView)view.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        if (!this.accelerometerExperiment.getDescriptionText().equals(""))
            descriptionTextView.setText(this.accelerometerExperiment.getDescriptionText());

        Button takeExperiment = (Button)view.findViewById(R.id.takeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startExperimentActivity(new AccelerometerSensorPlugin());
            }
        });

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        if (!this.accelerometerExperiment.getExperiment().getExperimentPath().equals(""))
            onExperimentPerformed();
    }

    @Override
    protected void onExperimentPerformed() {
        takenExperimentInfo.setVisibility(View.INVISIBLE);

        final String experimentPath = accelerometerExperiment.getExperiment().getExperimentPath();
        ExperimentData experimentData = ExperimentHelper.loadExperimentData(experimentPath);
        if (experimentData == null)
            return;

        takenExperimentInfo.setVisibility(View.VISIBLE);
        takenExperimentInfo.setChecked(true);
        File experimentPathFile = new File(experimentPath);
        takenExperimentInfo.setText(experimentPathFile.getName());
    }
}

