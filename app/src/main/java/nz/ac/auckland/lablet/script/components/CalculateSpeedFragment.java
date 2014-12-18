/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.auckland.lablet.*;
import nz.ac.auckland.lablet.camera.ITimeData;
import nz.ac.auckland.lablet.camera.MotionAnalysis;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.script.Script;
import nz.ac.auckland.lablet.script.ScriptComponent;
import nz.ac.auckland.lablet.script.ScriptTreeNodeFragmentHolder;
import nz.ac.auckland.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Script component for the speed/acceleration calculation.
 * <p>
 * It can hold a fragment for either the x or the y direction.
 * </p>
 */
class ScriptTreeNodeCalculateSpeed extends ScriptTreeNodeFragmentHolder {
    private ScriptExperimentRef experiment;

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

    public ScriptTreeNodeCalculateSpeed(Script script, boolean xSpeed) {
        super(script);

        isXSpeed = xSpeed;
    }

    // To be called from lua, don't remove.
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
    public ScriptComponentGenericFragment createFragment() {
        ScriptComponentGenericFragment fragment;
        if (isXSpeed)
            fragment = new CalculateXSpeedFragment();
        else
            fragment = new CalculateYSpeedFragment();
        fragment.setScriptComponent(this);
        return fragment;
    }

    public void setExperiment(ScriptExperimentRef experiment) {
        this.experiment = experiment;
    }

    public ScriptExperimentRef getExperiment() {
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

/**
 * Abstract base class for a script component that ask the user to calculate speed and acceleration of a dataset.
 */
abstract class CalculateSpeedFragment extends ScriptComponentGenericFragment {
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
    private CorrectPosAndVeloValues correctPosAndVeloValues = null;
    private CorrectAccelerationValues correctAccelerationValues = null;

    private List<String> unitList = new ArrayList<>();
    private String correctSpeedUnit = "[m/s]";
    private String correctAccelerationUnit = "[m/s^2]";

    protected MarkerDataModel tagMarker;
    protected ITimeData timeData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (component == null)
            return view;

        ScriptTreeNodeCalculateSpeed speedComponent = (ScriptTreeNodeCalculateSpeed)component;

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
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
        ScriptTreeNodeCalculateSpeed speedComponent = (ScriptTreeNodeCalculateSpeed)component;

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

    /**
     * The first taken data points might not be optimal, e.g,, the ball is still in the hand.
     * @return the index of the first data point to use for speed/acceleration calculation
     */
    private int getFirstDataPointIndex() {
        int numberOfDataPoints = tagMarker.getMarkerCount();
        if (numberOfDataPoints >= 5)
            return 2;
        else
            return numberOfDataPoints - 3;
    }

    protected MotionAnalysis getMotionAnalysis() {
        ScriptTreeNodeCalculateSpeed speedComponent = (ScriptTreeNodeCalculateSpeed)component;
        return speedComponent.getExperiment().getMotionAnalysis(getActivity(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();

        ScriptTreeNodeCalculateSpeed speedComponent = (ScriptTreeNodeCalculateSpeed)component;

        MotionAnalysis sensorAnalysis = getMotionAnalysis();
        if (sensorAnalysis == null)
            return;
        tagMarker = sensorAnalysis.getTagMarkers();
        timeData = sensorAnalysis.getTimeData();

        // first update the tables because otherwise update() can cause a crash when accessing data (an update is
        // triggered when changing a text view)
        speedTable.setAdapter(createSpeedTableAdapter());
        accelerationTable.setAdapter(createAccelerationTableAdapter());

        positionUnitTextView.setText("[" + getPositionUnit() + "]");

        MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(tagMarker);
        adapter.addColumn(new TimeDataTableColumn(sensorAnalysis.getTUnit(), timeData));
        adapter.addColumn(new XPositionDataTableColumn(sensorAnalysis.getXUnit()));
        adapter.addColumn(new YPositionDataTableColumn(sensorAnalysis.getYUnit()));
        rawDataTable.setAdapter(adapter);

        if (tagMarker.getMarkerCount() < 3)
            return;

        String text = "";
        text += timeData.getTimeAt(tagMarker.getMarkerDataAt(getFirstDataPointIndex()).getId());
        time1EditText.setText(text);
        text = "";
        text += timeData.getTimeAt(tagMarker.getMarkerDataAt(getFirstDataPointIndex() + 1).getId());
        time2EditText.setText(text);
        text = "";
        text += timeData.getTimeAt(tagMarker.getMarkerDataAt(getFirstDataPointIndex() + 2).getId());
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

        // cache correct values here
        correctPosAndVeloValues = getCorrectValues();

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
        // always set state to done (in case there is a bug in the calculation and the student can't get further)
        // TODO: remove again!
        setState(ScriptComponent.SCRIPT_STATE_DONE);
        return;

        /*
        if (done)
            setState(ScriptComponent.SCRIPT_STATE_DONE);
        else
            setState(ScriptComponent.SCRIPT_STATE_ONGOING);
        */
    }

    // error propagation
    private double getSpeedError(double deltaX, double deltaT, double xError, double tError) {
        return Math.sqrt(Math.pow(1.d / deltaT * xError, 2) + Math.pow(deltaX / deltaT / deltaT * tError, 2));
    }

    private boolean fuzzyEqual(float value, float correctValue, float error) {
        if (Math.abs(value - correctValue) > error)
            return false;
        return true;
    }

    private class CorrectPosAndVeloValues {
        public float x0;
        public float x1;
        public float x2;

        public float v0;
        public float v1;

        public float v0Error;
        public float v1Error;
    }

    final float errorX = 0.01f;
    final float errorT = 0.001f;

    private CorrectPosAndVeloValues getCorrectValues() {
        CorrectPosAndVeloValues values = new CorrectPosAndVeloValues();
        values.x0 = getPosition(getFirstDataPointIndex()) * getUnitToMeterFactor();
        values.x1 = getPosition(getFirstDataPointIndex() + 1) * getUnitToMeterFactor();
        values.x2 = getPosition(getFirstDataPointIndex() + 2) * getUnitToMeterFactor();

        values.v0 = getSpeed(getFirstDataPointIndex()) * getUnitToMeterFactor();
        values.v1 = getSpeed(getFirstDataPointIndex() + 1) * getUnitToMeterFactor();

        float deltaT = (Float.parseFloat(String.valueOf(time2EditText.getText()))
                - Float.parseFloat(String.valueOf(time1EditText.getText()))) / 1000.f;

        // The entered and the correct values can differ quite bit, thus calculate the error here and later check if the
        // entered values are compatible
        values.v0Error = (float)getSpeedError(values.x1 - values.x0, deltaT, errorX, errorT);
        values.v1Error = (float)getSpeedError(values.x2 - values.x1, deltaT, errorX, errorT);

        return values;
    }

    final float errorV = 0.01f;

    private class CorrectAccelerationValues {
        public float a0;
        public float a0Error;
    }

    /** We take the user input values and check if they did the calculation right. We don't use the original values
     * since this would lead to a huge error range and we would mark a value as correct even if the calculation was
     * wrong. For example, the error range for the acceleration can easily be in the order of 10m/s^2.
     */
    private CorrectAccelerationValues getCorrectAccelerationValues(float v0Input, float v1Input) {
        CorrectAccelerationValues values = new CorrectAccelerationValues();

        float deltaT = (Float.parseFloat(String.valueOf(time2EditText.getText()))
                - Float.parseFloat(String.valueOf(time1EditText.getText()))) / 1000.f;

        values.a0 = (v1Input - v0Input) / deltaT;

        // we can use getSpeedError here since the calculation is the same for the acceleration error
        values.a0Error = (float)getSpeedError(v1Input - v0Input, deltaT, errorV, errorT);

        return values;
    }

    private boolean checkPositionInput(CorrectPosAndVeloValues correctPosAndVeloValues) {
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

        if (!fuzzyEqual(position1, correctPosAndVeloValues.x0, errorX))
            return false;
        if (!fuzzyEqual(position2, correctPosAndVeloValues.x1, errorX))
            return false;
        if (!fuzzyEqual(position3, correctPosAndVeloValues.x2, errorX))
            return false;

        return true;
    }

    private boolean checkSpeedInput(CorrectPosAndVeloValues correctPosAndVeloValues) {
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

        if (!fuzzyEqual(speed1, correctPosAndVeloValues.v0, correctPosAndVeloValues.v0Error))
            return false;
        if (!fuzzyEqual(speed2, correctPosAndVeloValues.v1, correctPosAndVeloValues.v1Error))
            return false;

        // if correct calculate the correct acceleration values from user input
        correctAccelerationValues = getCorrectAccelerationValues(speed1, speed2);

        return true;
    }

    private boolean checkAccelerationInput(CorrectAccelerationValues correctValues) {
        float acceleration1;
        try {
            acceleration1 = Float.parseFloat(String.valueOf(acceleration1EditText.getText()));
        } catch (NumberFormatException e) {
            return false;
        }

        String unit = unitList.get(accelerationUnitSpinner.getSelectedItemPosition());
        if (!unit.equals(correctAccelerationUnit))
            return false;

        return fuzzyEqual(acceleration1, correctValues.a0, correctValues.a0Error);
    }

    private void update() {
        if (tagMarker.getMarkerCount() < 3 || correctPosAndVeloValues == null)
            return;

        boolean allDone = true;
        if (checkPositionInput(correctPosAndVeloValues)) {
            positionCheckBox.setChecked(true);
            positionCheckBox.setText("Correct!");
        } else {
            allDone = false;
            positionCheckBox.setChecked(false);
            positionCheckBox.setText("");
        }
        if (checkSpeedInput(correctPosAndVeloValues)) {
            speedCheckBox.setChecked(true);
            speedCheckBox.setText("Correct!");
            speedTable.setVisibility(View.VISIBLE);

            if (checkAccelerationInput(correctAccelerationValues)) {
                accelerationCheckBox.setChecked(true);
                accelerationCheckBox.setText("Correct!");
                accelerationTable.setVisibility(View.VISIBLE);
            } else {
                allDone = false;
                accelerationCheckBox.setChecked(false);
                accelerationCheckBox.setText("");
                accelerationTable.setVisibility(View.INVISIBLE);
            }
        } else {
            allDone = false;
            speedCheckBox.setChecked(false);
            speedCheckBox.setText("");
            speedTable.setVisibility(View.INVISIBLE);

            accelerationCheckBox.setChecked(false);
            accelerationCheckBox.setText("");
            accelerationTable.setVisibility(View.INVISIBLE);
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
    abstract MarkerDataTableAdapter createSpeedTableAdapter();
    abstract MarkerDataTableAdapter createAccelerationTableAdapter();
}


