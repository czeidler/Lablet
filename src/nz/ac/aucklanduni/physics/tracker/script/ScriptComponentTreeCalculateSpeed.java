/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.aucklanduni.physics.tracker.*;
import nz.ac.aucklanduni.physics.tracker.views.table.*;

import java.util.ArrayList;
import java.util.List;


public class ScriptComponentTreeCalculateSpeed extends ScriptComponentTreeFragmentHolder {
    private ScriptComponentExperiment experiment;

    private boolean isXSpeed;

    private String header = "";
    private float position1 = 0.f;
    private float position2 = 0.f;
    private float position3 = 0.f;
    private float speed1 = 0.f;
    private float speed2 = 0.f;
    private float acceleration1 = 0.f;
    private int selectedSpeedUnitIndex = 0;
    private int selectedAccelerationUnitIndex = 0;

    public int getSelectedSpeedUnitIndex() {
        return selectedSpeedUnitIndex;
    }

    public void setSelectedSpeedUnitIndex(int selectedSpeedUnitIndex) {
        this.selectedSpeedUnitIndex = selectedSpeedUnitIndex;
    }

    public int getSelectedAccelerationUnitIndex() {
        return selectedAccelerationUnitIndex;
    }

    public void setSelectedAccelerationUnitIndex(int selectedAccelerationUnitIndex) {
        this.selectedAccelerationUnitIndex = selectedAccelerationUnitIndex;
    }

    public ScriptComponentTreeCalculateSpeed(Script script, boolean xSpeed) {
        super(script);

        isXSpeed = xSpeed;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
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

    public float getPosition1() {
        return position1;
    }

    public void setPosition1(float position1) {
        this.position1 = position1;
    }

    public float getPosition2() {
        return position2;
    }

    public void setPosition2(float position2) {
        this.position2 = position2;
    }

    public float getPosition3() {
        return position3;
    }

    public void setPosition3(float position3) {
        this.position3 = position3;
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

    public float getAcceleration1() {
        return acceleration1;
    }

    public void setAcceleration1(float acceleration1) {
        this.acceleration1 = acceleration1;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        bundle.putFloat("position1", position1);
        bundle.putFloat("position2", position2);
        bundle.putFloat("position3", position3);
        bundle.putFloat("speed1", speed1);
        bundle.putFloat("speed2", speed2);
        bundle.putFloat("acceleration1", acceleration1);
        bundle.putInt("selectedSpeedUnitIndex", selectedSpeedUnitIndex);
        bundle.putInt("selectedAccelerationUnitIndex", selectedAccelerationUnitIndex);
    }

    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        position1 = bundle.getFloat("position1", 0.0f);
        position2 = bundle.getFloat("position2", 0.0f);
        position3 = bundle.getFloat("position3", 0.0f);
        speed1 = bundle.getFloat("speed1", 0.0f);
        speed2 = bundle.getFloat("speed2", 0.0f);
        acceleration1 = bundle.getFloat("acceleration1", 0.0f);
        selectedSpeedUnitIndex = bundle.getInt("selectedSpeedUnitIndex", 0);
        selectedAccelerationUnitIndex = bundle.getInt("selectedAccelerationUnitIndex", 0);
        return true;
    }
}

abstract class ScriptComponentCalculateSpeedFragment extends ScriptComponentGenericFragment {
    private EditText time1EditText = null;
    private EditText time2EditText = null;
    private EditText time3EditText = null;
    private EditText position1EditText = null;
    private EditText position2EditText = null;
    private EditText position3EditText = null;
    private EditText speed1EditText = null;
    private EditText speed2EditText = null;
    private EditText acceleration1EditText = null;
    private TableView rawDataTable = null;
    private TableView speedTable = null;
    private TableView accelerationTable = null;
    private CheckBox positionCheckBox = null;
    private CheckBox speedCheckBox = null;
    private CheckBox accelerationCheckBox = null;
    private Spinner speedUnitSpinner = null;
    private Spinner accelerationUnitSpinner = null;
    private TextView positionUnitTextView = null;

    private List<String> unitList = new ArrayList<String>();
    private String correctSpeedUnit = "[m/s]";
    private String correctAccelerationUnit = "[m/s^2]";
    private float unitFactor = 1f;

    protected MarkersDataModel tagMarker = null;


    public ScriptComponentCalculateSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;

        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_calculate_speed);
        assert child != null;

        TextView headerTextView = (TextView)child.findViewById(R.id.headerTextView);
        assert headerTextView != null;
        if (!speedComponent.getHeader().equals(""))
            headerTextView.setText(speedComponent.getHeader());

        rawDataTable = (TableView)child.findViewById(R.id.dataTable);
        assert rawDataTable != null;

        TextView enterSpeedTextView = (TextView)child.findViewById(R.id.enterSpeedTextView);
        assert enterSpeedTextView != null;
        enterSpeedTextView.setText(getDescriptionLabel());

        time1EditText = (EditText)child.findViewById(R.id.time1EditText);
        assert time1EditText != null;
        time2EditText = (EditText)child.findViewById(R.id.time2EditText);
        assert time2EditText != null;
        time3EditText = (EditText)child.findViewById(R.id.time3EditText);
        assert time3EditText != null;

        position1EditText = (EditText)child.findViewById(R.id.position1EditText);
        assert position1EditText != null;
        position2EditText = (EditText)child.findViewById(R.id.position2EditText);
        assert position2EditText != null;
        position3EditText = (EditText)child.findViewById(R.id.position3EditText);
        assert position3EditText != null;

        speed1EditText = (EditText)child.findViewById(R.id.speed1EditText);
        assert speed1EditText != null;
        speed2EditText = (EditText)child.findViewById(R.id.speed2EditText);
        assert speed2EditText != null;
        acceleration1EditText = (EditText)child.findViewById(R.id.acceleration1EditText);
        assert acceleration1EditText != null;

        positionCheckBox = (CheckBox)child.findViewById(R.id.positionCheckBox);
        assert positionCheckBox != null;
        speedCheckBox = (CheckBox)child.findViewById(R.id.speedCheckBox);
        assert speedCheckBox != null;
        accelerationCheckBox = (CheckBox)child.findViewById(R.id.accelerationCheckBox);
        assert accelerationCheckBox != null;

        positionUnitTextView = (TextView)child.findViewById(R.id.positionUnitTextView);

        speedTable = (TableView)child.findViewById(R.id.speedTable);
        assert speedTable != null;

        accelerationTable = (TableView)child.findViewById(R.id.accelerationTable);
        assert accelerationTable != null;

        speedUnitSpinner = (Spinner)child.findViewById(R.id.speedUnitSpinner);
        assert speedUnitSpinner != null;
        accelerationUnitSpinner = (Spinner)child.findViewById(R.id.accelerationUnitSpinner);
        assert accelerationUnitSpinner != null;

        unitList.add("select unit");
        unitList.add("[s/m]");
        unitList.add("[m]");
        unitList.add(correctSpeedUnit);
        unitList.add("[apples/s]");
        unitList.add("[s^2/m]");
        unitList.add(correctAccelerationUnit);
        unitList.add("[m^2/s^2]");
        unitList.add("[s]");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, unitList);

        speedUnitSpinner.setAdapter(adapter);
        accelerationUnitSpinner.setAdapter(adapter);

        speedUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        accelerationUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                update();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    @Override
    public void onPause() {
        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;

        float position1 = Float.parseFloat(String.valueOf(position1EditText.getText()));
        float position2 = Float.parseFloat(String.valueOf(position2EditText.getText()));
        float position3 = Float.parseFloat(String.valueOf(position3EditText.getText()));
        float speed1 = Float.parseFloat(String.valueOf(speed1EditText.getText()));
        float speed2 = Float.parseFloat(String.valueOf(speed2EditText.getText()));
        float acceleration1 = Float.parseFloat(String.valueOf(acceleration1EditText.getText()));

        speedComponent.setPosition1(position1);
        speedComponent.setPosition2(position2);
        speedComponent.setPosition3(position3);
        speedComponent.setSpeed1(speed1);
        speedComponent.setSpeed2(speed2);
        speedComponent.setAcceleration1(acceleration1);
        speedComponent.setSelectedSpeedUnitIndex(speedUnitSpinner.getSelectedItemPosition());
        speedComponent.setSelectedAccelerationUnitIndex(accelerationUnitSpinner.getSelectedItemPosition());

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

        positionUnitTextView.setText("[" + getPositionUnit() + "]");

        tagMarker = experimentAnalysis.getTagMarkers();
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        adapter.addColumn(new TimeDataTableColumn());
        adapter.addColumn(new XPositionDataTableColumn());
        adapter.addColumn(new YPositionDataTableColumn());
        rawDataTable.setAdapter(adapter);

        Experiment experiment = experimentAnalysis.getExperiment();
        if (tagMarker.getMarkerCount() < 3)
            return;

        String text = "";
        text += experiment.getRunValueAt(tagMarker.getMarkerDataAt(0).getRunId());
        time1EditText.setText(text);
        text = "";
        text += experiment.getRunValueAt(tagMarker.getMarkerDataAt(1).getRunId());
        time2EditText.setText(text);
        text = "";
        text += experiment.getRunValueAt(tagMarker.getMarkerDataAt(2).getRunId());
        time3EditText.setText(text);

        text = "";
        text += speedComponent.getPosition1();
        position1EditText.setText(text);
        text = "";
        text += speedComponent.getPosition2();
        position2EditText.setText(text);
        text = "";
        text += speedComponent.getPosition3();
        position3EditText.setText(text);

        text = "";
        text += speedComponent.getSpeed1();
        speed1EditText.setText(text);
        text = "";
        text += speedComponent.getSpeed2();
        speed2EditText.setText(text);
        text = "";
        text += speedComponent.getAcceleration1();
        acceleration1EditText.setText(text);

        installEditTextListener();

        speedUnitSpinner.setSelection(speedComponent.getSelectedSpeedUnitIndex());
        accelerationUnitSpinner.setSelection(speedComponent.getSelectedAccelerationUnitIndex());

        speedTable.setAdapter(createSpeedTableAdapter(experimentAnalysis));
        accelerationTable.setAdapter(createAccelerationTableAdapter(experimentAnalysis));

        update();
    }

    private
    void installEditTextListener() {
        position1EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });
        position2EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });
        position3EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });

        speed1EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });
        speed2EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });

        acceleration1EditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                update();
            }
        });
    }

    private void setDone(boolean done) {
        if (done) {
            speedTable.setVisibility(View.VISIBLE);
            accelerationTable.setVisibility(View.VISIBLE);
            setState(ScriptComponentTree.SCRIPT_STATE_DONE);
        } else {
            speedTable.setVisibility(View.INVISIBLE);
            accelerationTable.setVisibility(View.INVISIBLE);
            setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
        }
    }

    private boolean fuzzyEqual(float value, float correctValue) {
        float correctMargin = 0.1f;
        if (Math.abs(value - correctValue) > Math.abs(correctValue * correctMargin)
                && Math.abs(value - correctValue) > 0.11)
            return false;
        return true;
    }

    private boolean checkPositionInput() {
        float position1;
        float position2;
        float position3;
        try {
            position1 = Float.parseFloat(String.valueOf(position1EditText.getText()));
            position2 = Float.parseFloat(String.valueOf(position2EditText.getText()));
            position3 = Float.parseFloat(String.valueOf(position3EditText.getText()));
        } catch (NumberFormatException e) {
            return false;
        }

        if (!fuzzyEqual(position1, getPosition(0)))
            return false;
        if (!fuzzyEqual(position2, getPosition(1)))
            return false;
        if (!fuzzyEqual(position3, getPosition(2)))
            return false;

        return true;
    }

    private boolean checkSpeedInput() {
        float speed1;
        float speed2;
        try {
            speed1 = Float.parseFloat(String.valueOf(speed1EditText.getText()));
            speed2 = Float.parseFloat(String.valueOf(speed2EditText.getText()));
        } catch (NumberFormatException e) {
            return false;
        }

        String unit = unitList.get(speedUnitSpinner.getSelectedItemPosition());
        if (!unit.equals(correctSpeedUnit))
            return false;

        // round value to one decimal, this fixes some problem with small speeds values
        float correctSpeed1 = ((float)Math.round(getSpeed(0) * 10)) / 10;
        float correctSpeed2 = ((float)Math.round(getSpeed(1) * 10)) / 10;

        correctSpeed1 *= getUnitToMeterFactor();
        correctSpeed2 *= getUnitToMeterFactor();

        if (!fuzzyEqual(speed1, correctSpeed1))
            return false;
        if (!fuzzyEqual(speed2, correctSpeed2))
            return false;

        return true;
    }

    private boolean checkAccelerationInput() {
        float acceleration1;
        try {
            acceleration1 = Float.parseFloat(String.valueOf(acceleration1EditText.getText()));
        } catch (NumberFormatException e) {
            return false;
        }

        String unit = unitList.get(accelerationUnitSpinner.getSelectedItemPosition());
        if (!unit.equals(correctAccelerationUnit))
            return false;

        float correctAcceleration1 = ((float)Math.round(getAcceleration(0) * 10)) / 10;
        correctAcceleration1 *= getUnitToMeterFactor();

        return fuzzyEqual(acceleration1, correctAcceleration1);
    }

    private void update() {
        boolean allDone = true;
        if (checkPositionInput()) {
            positionCheckBox.setChecked(true);
            positionCheckBox.setText("Correct!");
        } else {
            allDone = false;
            positionCheckBox.setChecked(false);
            positionCheckBox.setText("");
        }
        if (checkSpeedInput()) {
            speedCheckBox.setChecked(true);
            speedCheckBox.setText("Correct!");
        } else {
            allDone = false;
            speedCheckBox.setChecked(false);
            speedCheckBox.setText("");
        }
        if (checkAccelerationInput()) {
            accelerationCheckBox.setChecked(true);
            accelerationCheckBox.setText("Correct!");
        } else {
            allDone = false;
            accelerationCheckBox.setChecked(false);
            accelerationCheckBox.setText("");
        }

        setDone(allDone);
    }

    private float getUnitToMeterFactor() {
        String unit = getPositionUnit();
        if (unit.equals("mm"))
            return 0.001f;
        if (unit.equals("cm"))
            return 0.01f;
        if (unit.equals("m"))
            return 1f;
        return 1f;
    }

    abstract String getDescriptionLabel();
    abstract float getPosition(int index);
    abstract String getPositionUnit();
    abstract float getSpeed(int index);
    abstract float getAcceleration(int index);
    abstract ColumnMarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis);
    abstract ColumnMarkerDataTableAdapter createAccelerationTableAdapter(ExperimentAnalysis experimentAnalysis);
}


class ScriptComponentCalculateXSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private XSpeedDataTableColumn speedDataTableColumn;
    private XAccelerationDataTableColumn accelerationDataTableColumn;

    public ScriptComponentCalculateXSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    String getDescriptionLabel() {
        return "Fill table for the x-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getCalibratedMarkerPositionAt(index).x;
    }

    @Override
    String getPositionUnit() {
        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;
        ExperimentAnalysis experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(getActivity(),
                speedComponent.getExperiment().getExperimentPath());
        if (experimentAnalysis == null)
            return "";
        return experimentAnalysis.getXUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    float getAcceleration(int index) {
        return accelerationDataTableColumn.getValue(index).floatValue();
    }

    @Override
    ColumnMarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        speedDataTableColumn = new XSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    ColumnMarkerDataTableAdapter createAccelerationTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        accelerationDataTableColumn = new XAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}

class ScriptComponentCalculateYSpeedFragment extends ScriptComponentCalculateSpeedFragment {
    private YSpeedDataTableColumn speedDataTableColumn;
    private YAccelerationDataTableColumn accelerationDataTableColumn;

    public ScriptComponentCalculateYSpeedFragment(ScriptComponentTreeCalculateSpeed component) {
        super(component);
    }

    @Override
    String getDescriptionLabel() {
        return "Fill table for the y-direction:";
    }

    @Override
    float getPosition(int index) {
        return tagMarker.getCalibratedMarkerPositionAt(index).y;
    }

    @Override
    String getPositionUnit() {
        ScriptComponentTreeCalculateSpeed speedComponent = (ScriptComponentTreeCalculateSpeed)component;
        ExperimentAnalysis experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(getActivity(),
                speedComponent.getExperiment().getExperimentPath());
        if (experimentAnalysis == null)
            return "";
        return experimentAnalysis.getYUnit();
    }

    @Override
    float getSpeed(int index) {
        return speedDataTableColumn.getValue(index).floatValue();
    }

    @Override
    float getAcceleration(int index) {
        return accelerationDataTableColumn.getValue(index).floatValue();
    }

    @Override
    ColumnMarkerDataTableAdapter createSpeedTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        speedDataTableColumn = new YSpeedDataTableColumn();
        adapter.addColumn(new SpeedTimeDataTableColumn());
        adapter.addColumn(speedDataTableColumn);

        return adapter;
    }

    @Override
    ColumnMarkerDataTableAdapter createAccelerationTableAdapter(ExperimentAnalysis experimentAnalysis) {
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(tagMarker, experimentAnalysis);
        accelerationDataTableColumn = new YAccelerationDataTableColumn();
        adapter.addColumn(new AccelerationTimeDataTableColumn());
        adapter.addColumn(accelerationDataTableColumn);
        return adapter;
    }
}