/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ScriptComponentCalculateYSpeed extends ScriptComponentFragmentHolder {
    private ScriptComponentExperiment experiment;

    private float speed1 = 0.f;
    private float speed2 = 0.f;

    public ScriptComponentCalculateYSpeed(Script script) {
        super(script);
    }

    @Override
    public boolean initCheck() {
        if (experiment == null) {
            lastErrorMessage = "no experiment given";
            return false;
        }
        return true;
    }

    @Override
    public Fragment createFragment() {
        ScriptComponentCalculateYSpeedFragment fragment = new ScriptComponentCalculateYSpeedFragment(this);
        return fragment;
    }

    public void setExperiment(ScriptComponentExperiment experiment) {
        this.experiment = experiment;
    }

    public ScriptComponentExperiment getExperiment() {
        return experiment;
    }

    public float getSpeed1() {
        return speed1;
    }

    public void setSpeed1(float speed1) {
        this.speed1 = speed1;
    }

    public float getSpeed2() {
        return speed2;
    }

    public void setSpeed2(float speed2) {
        this.speed2 = speed2;
    }


    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        bundle.putFloat("speed1", speed1);
        bundle.putFloat("speed2", speed2);
    }

    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        speed1 = bundle.getFloat("speed1", 0.0f);
        speed2 = bundle.getFloat("speed2", 0.0f);
        return true;
    }
}

class ScriptComponentCalculateYSpeedFragment extends ScriptComponentGenericFragment {
    private TextView textViewTime1 = null;
    private TextView textViewTime2 = null;
    private EditText editTextTime1 = null;
    private EditText editTextTime2 = null;
    private TableView rawDataTable = null;
    private TableView ySpeedTable = null;
    private MarkersDataModel tagMarker = null;
    private MarkerDataYSpeedTableAdapter speedData = null;

    public ScriptComponentCalculateYSpeedFragment(ScriptComponentCalculateYSpeed component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_calculate_yspeed);
        assert child != null;

        rawDataTable = (TableView)child.findViewById(R.id.dataTable);
        assert rawDataTable != null;

        textViewTime1 = (TextView)child.findViewById(R.id.textViewTime1);
        assert textViewTime1 != null;
        textViewTime2 = (TextView)child.findViewById(R.id.textViewTime2);
        assert textViewTime2 != null;
        editTextTime1 = (EditText)child.findViewById(R.id.editTextTime1);
        assert editTextTime1 != null;
        editTextTime2 = (EditText)child.findViewById(R.id.editTextTime2);
        assert editTextTime2 != null;

        Button okButton = (Button)child.findViewById(R.id.buttonOk);
        assert okButton != null;
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInput()) {
                    ySpeedTable.setVisibility(View.VISIBLE);
                    setState(ScriptComponent.SCRIPT_STATE_DONE);
                } else {
                    ySpeedTable.setVisibility(View.INVISIBLE);
                    setState(ScriptComponent.SCRIPT_STATE_ONGOING);
                }
            }
        });

        ySpeedTable = (TableView)child.findViewById(R.id.speedTable);
        assert ySpeedTable != null;

        return view;
    }

    @Override
    public void onPause() {
        ScriptComponentCalculateYSpeed speedComponent = (ScriptComponentCalculateYSpeed)component;

        float speed1 = Float.parseFloat(String.valueOf(editTextTime1.getText()));
        float speed2 = Float.parseFloat(String.valueOf(editTextTime2.getText()));

        speedComponent.setSpeed1(speed1);
        speedComponent.setSpeed2(speed2);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        ScriptComponentCalculateYSpeed speedComponent = (ScriptComponentCalculateYSpeed)component;

        ExperimentAnalysis experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(getActivity(),
                speedComponent.getExperiment().getExperimentPath());
        if (experimentAnalysis == null)
            return;

        tagMarker = experimentAnalysis.getTagMarkers();
        rawDataTable.setAdapter(new MarkerDataTableAdapter(tagMarker, experimentAnalysis));

        Experiment experiment = experimentAnalysis.getExperiment();
        if (tagMarker.getMarkerCount() < 3)
            return;

        String text = "";
        text += experiment.getRunValueAt(tagMarker.getMarkerDataAt(1).getRunId());
        text += " [";
        text += experiment.getRunValueUnit();
        text += "]:";
        textViewTime1.setText(text);
        text = "";
        text += experiment.getRunValueAt(tagMarker.getMarkerDataAt(2).getRunId());
        text += " [";
        text += experiment.getRunValueUnit();
        text += "]:";
        textViewTime2.setText(text);

        speedData = new MarkerDataYSpeedTableAdapter(tagMarker, experimentAnalysis);
        ySpeedTable.setAdapter(speedData);

        text = "";
        text += speedComponent.getSpeed1();
        editTextTime1.setText(text);
        text = "";
        text += speedComponent.getSpeed2();
        editTextTime2.setText(text);

        if (speedComponent.getState() != ScriptComponent.SCRIPT_STATE_DONE)
            ySpeedTable.setVisibility(View.INVISIBLE);
    }

    private boolean checkInput() {
        float speed1 = Float.parseFloat(String.valueOf(editTextTime1.getText()));
        float speed2 = Float.parseFloat(String.valueOf(editTextTime2.getText()));
        // round value to one decimal, this fixes some problem with small speeds values
        float correctSpeed1 = ((float)Math.round(speedData.getYSpeed(0) * 10)) / 10;
        float correctSpeed2 = ((float)Math.round(speedData.getYSpeed(1) * 10)) / 10;

        float correctMargin = 0.1f;
        if (Math.abs(speed1 - correctSpeed1) > Math.abs(correctSpeed1 * correctMargin)
                && Math.abs(speed1 - correctSpeed1) > 0.11)
            return false;
        if (Math.abs(speed2 - correctSpeed2) > Math.abs(correctSpeed2 * correctMargin)
                && Math.abs(speed2 - correctSpeed2) > 0.11)
            return false;

        return true;
    }
}
