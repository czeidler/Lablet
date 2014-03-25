/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.aucklanduni.physics.tracker.R;

class ScriptComponentPotentialEnergy1View extends FrameLayout {
    private ScriptComponentPotentialEnergy1 component;
    private TextView massQuestionTextView;
    private TextView heightQuestionTextView;
    private TextView energyQuestionTextView;
    private TextView pbjSandwichQuestionTextView;
    private EditText massEditText;
    private EditText heightEditText;
    private EditText energyEditText;
    private EditText pbjEditText;
    private CheckBox doneCheckBox;

    public ScriptComponentPotentialEnergy1View(Context context, ScriptComponentPotentialEnergy1 component) {
        super(context);
        this.component = component;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.script_component_potential_energy_1, null, true);
        assert view != null;
        addView(view);

        massQuestionTextView = (TextView)view.findViewById(R.id.massQuestionTextView);
        assert massQuestionTextView != null;
        heightQuestionTextView = (TextView)view.findViewById(R.id.heightQuestionTextView);
        assert heightQuestionTextView != null;
        energyQuestionTextView = (TextView)view.findViewById(R.id.energyQuestionTextView);
        assert energyQuestionTextView != null;
        pbjSandwichQuestionTextView = (TextView)view.findViewById(R.id.pbjSandwichQuestionTextView);
        assert pbjSandwichQuestionTextView != null;

        massEditText = (EditText)view.findViewById(R.id.massEditText);
        assert massEditText != null;
        heightEditText = (EditText)view.findViewById(R.id.heightEditText);
        assert heightEditText != null;
        energyEditText = (EditText)view.findViewById(R.id.energyEditText);
        assert energyEditText != null;
        pbjEditText = (EditText)view.findViewById(R.id.pbjEditText);
        assert pbjEditText != null;

        doneCheckBox = (CheckBox)view.findViewById(R.id.doneCheckBox);
        assert doneCheckBox != null;

        massQuestionTextView.setText(component.getMassQuestionText());
        heightQuestionTextView.setText(component.getHeightQuestionText());
        energyQuestionTextView.setText(component.getEnergyQuestionText());
        pbjSandwichQuestionTextView.setText(component.getPbjSandwichQuestionText());

        massEditText.setText(Float.toString(getMass()));
        heightEditText.setText(Float.toString(component.getHeight()));
        energyEditText.setText(Float.toString(component.getEnergy()));
        pbjEditText.setText(Float.toString(component.getPbjValue()));

        massEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    setMass(Float.parseFloat(editable.toString()));
                } catch (NumberFormatException e) {

                }
            }
        });
        heightEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    setHeight(Float.parseFloat(editable.toString()));
                } catch (NumberFormatException e) {

                }
            }
        });
        energyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    setEnergy(Float.parseFloat(editable.toString()));
                } catch (NumberFormatException e) {

                }
            }
        });
        pbjEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    setPbjValue(Float.parseFloat(editable.toString()));
                } catch (NumberFormatException e) {

                }
            }
        });

        update();
    }

    private float getMass() {
        return component.getMass() * 1000.f;
    }

    private void setMass(float height) {
        component.setMass(height / 1000.f);
        update();
    }

    private void setHeight(float height) {
        component.setHeight(height);
        update();
    }

    private void setEnergy(float energy) {
        component.setEnergy(energy);
        update();
    }

    private void setPbjValue(float value) {
        component.setPbjValue(value);
        update();
    }

    private void update() {
        if (checkValues()) {
            component.setState(ScriptComponent.SCRIPT_STATE_DONE);
            doneCheckBox.setChecked(true);
            doneCheckBox.setText("Correct!");
        } else {
            component.setState(ScriptComponent.SCRIPT_STATE_ONGOING);
            doneCheckBox.setChecked(false);
            doneCheckBox.setText("");
        }
    }

    private boolean checkValues() {
        final float g = 9.81f;
        final float cal = 4.184f;
        final float pbjSandwichCal = 432.f;

        if (isFuzzyZero(component.getHeight()) || isFuzzyZero(component.getMass()))
            return false;

        float correctEnergy = component.getMass() * component.getHeight() * g;
        float correctPbjValue = pbjSandwichCal / (correctEnergy / cal);

        if (!isFuzzyEqual(component.getEnergy(), correctEnergy))
            return false;
        if (!isFuzzyEqual(component.getPbjValue(), correctPbjValue))
            return false;

        return true;
    }

    private boolean isFuzzyZero(float value) {
        if (Math.abs(value) < 0.0001)
            return true;
        return false;
    }

    private boolean isFuzzyEqual(float value, float correctValue) {
        if (Math.abs(value - correctValue) < correctValue * 0.05f)
            return true;
        return false;
    }
}


public class ScriptComponentPotentialEnergy1 extends ScriptComponentViewHolder {
    private String massQuestionText = "Mass:";
    private String heightQuestionText = "Height of the of the mass:";
    private String energyQuestionText = "What is its energy?";

    private String pbjSandwichQuestionText = "A typical peanut butter jam (PBJ) sandwich contains 432 calories. " +
            "How many throws could you perform with one PBJ sandwich?\n(1 calorie = 4.184 J)";

    private float mass = 0.1f;
    private float height = 0.0f;
    private float energy = 0.0f;
    private float pbjValue = 0.0f;

    public ScriptComponentPotentialEnergy1() {
        setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
    }

    public String getMassQuestionText() {
        return massQuestionText;
    }
    public void setMassQuestionText(String massQuestionText) {
        this.massQuestionText = massQuestionText;
    }
    public String getHeightQuestionText() {
        return heightQuestionText;
    }
    public void setHeightQuestionText(String heightQuestionText) {
        this.heightQuestionText = heightQuestionText;
    }
    public String getEnergyQuestionText() {
        return energyQuestionText;
    }
    public void setEnergyQuestionText(String energyQuestionTextView) {
        this.energyQuestionText = energyQuestionTextView;
    }
    public String getPbjSandwichQuestionText() {
        return pbjSandwichQuestionText;
    }
    public void setPbjSandwichQuestionText(String pbjSandwichQuestionText) {
        this.pbjSandwichQuestionText = pbjSandwichQuestionText;
    }


    public float getMass() {
        return mass;
    }
    public void setMass(float mass) {
        this.mass = mass;
    }
    public float getHeight() {
        return height;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public float getEnergy() {
        return energy;
    }
    public void setEnergy(float energy) {
        this.energy = energy;
    }
    public float getPbjValue() {
        return pbjValue;
    }
    public void setPbjValue(float pbjValue) {
        this.pbjValue = pbjValue;
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        return new ScriptComponentPotentialEnergy1View(context, this);
    }

    @Override
    public boolean initCheck() {
        return true;
    }

    @Override
    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        bundle.putFloat("mass", mass);
        bundle.putFloat("height", height);
        bundle.putFloat("energy", energy);
        bundle.putFloat("pbjValue", pbjValue);
    }

    @Override
    public boolean fromBundle(Bundle bundle) {
        mass = bundle.getFloat("mass", 1.f);
        height = bundle.getFloat("height", 0.f);
        energy = bundle.getFloat("energy");
        pbjValue = bundle.getFloat("pbjValue");

        return super.fromBundle(bundle);
    }
}