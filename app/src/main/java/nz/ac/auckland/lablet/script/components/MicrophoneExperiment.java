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
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.microphone.MicrophoneSensorPlugin;
import nz.ac.auckland.lablet.script.Script;

import java.io.File;


/**
 * Script component that has view for starting  a microphone experiment.
 */
public class MicrophoneExperiment extends SingleExperimentBase {
    public MicrophoneExperiment(Script script) {
        super(script);
        setDescriptionText("Please take an audio recording:");
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentMicExperimentView(context, (ScriptComponentSheetFragment)parent, this);
    }
}


/**
 * View to start a camera experiment activity.
 */
class ScriptComponentMicExperimentView extends ScriptComponentSingleExperimentBaseView {
    private MicrophoneExperiment micComponent;
    private CheckedTextView takenExperimentInfo = null;

    public ScriptComponentMicExperimentView(Context context, ScriptComponentSheetFragment sheetFragment,
                                            MicrophoneExperiment micComponent) {
        super(context, sheetFragment, micComponent);
        this.micComponent = micComponent;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.script_component_microphone_experiment, null, false);
        assert view != null;

        addView(view);

        TextView descriptionTextView = (TextView)view.findViewById(R.id.descriptionText);
        assert descriptionTextView != null;
        if (!this.micComponent.getDescriptionText().equals(""))
            descriptionTextView.setText(this.micComponent.getDescriptionText());

        Button takeExperiment = (Button)view.findViewById(R.id.takeExperimentButton);
        assert takeExperiment != null;
        takeExperiment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startExperimentActivity(new MicrophoneSensorPlugin());
            }
        });

        takenExperimentInfo = (CheckedTextView)view.findViewById(R.id.takenExperimentInfo);
        assert takenExperimentInfo != null;

        if (!this.micComponent.getExperiment().getExperimentPath().equals(""))
            onExperimentPerformed();
    }

    @Override
    protected void onExperimentPerformed() {
        takenExperimentInfo.setVisibility(View.INVISIBLE);

        final String experimentPath = micComponent.getExperiment().getExperimentPath();
        ExperimentData experimentData = ExperimentHelper.loadExperimentData(experimentPath);
        if (experimentData == null)
            return;

        takenExperimentInfo.setVisibility(View.VISIBLE);
        takenExperimentInfo.setChecked(true);
        File experimentPathFile = new File(experimentPath);
        takenExperimentInfo.setText(experimentPathFile.getName());
    }
}

