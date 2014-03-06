/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import nz.ac.aucklanduni.physics.tracker.*;
import nz.ac.aucklanduni.physics.tracker.views.table.MarkerDataTableAdapter;
import nz.ac.aucklanduni.physics.tracker.views.table.MarkerDataXSpeedTableAdapter;
import nz.ac.aucklanduni.physics.tracker.views.table.MarkerDataYSpeedTableAdapter;
import nz.ac.aucklanduni.physics.tracker.views.table.TableView;


public class ScriptComponentTreeCalculateSpeed extends ScriptComponentTreeFragmentHolder {
    private ScriptComponentExperiment experiment;

    private boolean isXSpeed;

    private float speed1 = 0.f;
    private float speed2 = 0.f;

    public ScriptComponentTreeCalculateSpeed(Script script, boolean xSpeed) {
        super(script);

        isXSpeed = xSpeed;
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
    public android.support.v4.app.Fragment createFragment() {
        if (isXSpeed)
            return new ScriptComponentCalculateXSpeedFragment(this);
        else
            return new ScriptComponentCalculateYSpeedFragment(this);
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

abstract class ScriptComponentCalculateSpeedFragment extends ScriptComponentGenericFragment {
    private TextView textViewTime1 = null;
    private TextView textViewTime2 = null;
    private EditText editTextTime1 = null;
    private EditText editTextTime2 = null;
    private TableView rawDataTable = null;
    private TableView speedTable = null;
    protected MarkersDataModel tagMarker = null;

    public ScriptComponentCalculateSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_calculate_speed);
        assert child != null;

        rawDataTable = (TableView)child.findViewById(R.id.dataTable);
        assert rawDataTable != null;

        TextView enterSpeedTextView = (TextView)child.findViewById(R.id.enterSpeedTextView);
        assert enterSpeedTextView != null;
        enterSpeedTextView.setText(getEnterSpeedLabel());

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
                    speedTable.setVisibility(View.VISIBLE);
                    setState(ScriptComponentTree.SCRIPT_STATE_DONE);
                } else {
                    speedTable.setVisibility(View.INVISIBLE);
                    setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
                }
            }
        });

        speedTable = (TableView)child.findViewById(R.id.speedTable);
        assert speedTable != null;

        return view;
    }

    @Override
    public void onPause() {
        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;

        float speed1 = Float.parseFloat(String.valueOf(editTextTime1.getText()));
        float speed2 = Float.parseFloat(String.valueOf(editTextTime2.getText()));

        speedComponent.setSpeed1(speed1);
        speedComponent.setSpeed2(speed2);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;

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

        speedTable.setAdapter(createSpeedTableAdapter(experimentAnalysis));

        text = "";
        text += speedComponent.getSpeed1();
        editTextTime1.setText(text);
        text = "";
        text += speedComponent.getSpeed2();
        editTextTime2.setText(text);

        if (speedComponent.getState() != ScriptComponentTree.SCRIPT_STATE_DONE)
            speedTable.setVisibility(View.INVISIBLE);
    }

    private boolean checkInput() {
        float speed1 = Float.parseFloat(String.valueOf(editTextTime1.getText()));
        float speed2 = Float.parseFloat(String.valueOf(editTextTime2.getText()));
        // round value to one decimal, this fixes some problem with small speeds values
        float correctSpeed1 = ((float)Math.round(getSpeed(0) * 10)) / 10;
        float correctSpeed2 = ((float)Math.round(getSpeed(1) * 10)) / 10;

        float correctMargin = 0.1f;
        if (Math.abs(speed1 - correctSpeed1) > Math.abs(correctSpeed1 * correctMargin)
                && Math.abs(speed1 - correctSpeed1) > 0.11)
            return false;
        if (Math.abs(speed2 - correctSpeed2) > Math.abs(correctSpeed2 * correctMargin)
                && Math.abs(speed2 - correctSpeed2) > 0.11)
            return false;

        return true;
    }

    abstract String getEnterSpeedLabel();
    abstract float getSpeed(int index);
    abstract MarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis);
}


class ScriptComponentCalculateYSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private MarkerDataYSpeedTableAdapter speedData;

    public ScriptComponentCalculateYSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    String getEnterSpeedLabel() {
        return "Enter y speed [m/s]:";
    }

    @Override
    float getSpeed(int index) {
        return speedData.getSpeed(index);
    }

    @Override
    MarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        speedData = new MarkerDataYSpeedTableAdapter(tagMarker, experimentAnalysis);
        return speedData;
    }
}

class ScriptComponentCalculateXSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private MarkerDataXSpeedTableAdapter speedData;

    public ScriptComponentCalculateXSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    String getEnterSpeedLabel() {
        return "Enter x speed [m/s]:";
    }

    @Override
    float getSpeed(int index) {
        return speedData.getSpeed(index);
    }

    @Override
    MarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        speedData = new MarkerDataXSpeedTableAdapter(tagMarker, experimentAnalysis);
        return speedData;
    }
}