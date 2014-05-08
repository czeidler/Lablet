/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import java.util.HashMap;
import java.util.Map;

import static nz.ac.aucklanduni.physics.tracker.R.*;


class TextComponent extends ScriptComponentViewHolder {
    private String text = "";
    private int typeface = Typeface.NORMAL;

    public TextComponent(String text) {
        this.text = text;
        setState(ScriptComponentTree.SCRIPT_STATE_DONE);
    }

    public void setTypeface(int typeface) {
        this.typeface = typeface;
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        textView.setTypeface(null, typeface);
        textView.setText(text);
        return textView;
    }

    @Override
    public boolean initCheck() {
        return true;
    }
}

class CheckBoxQuestion extends ScriptComponentViewHolder {
    private String text = "";
    public CheckBoxQuestion(String text) {
        this.text = text;
        setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        CheckBox view = new CheckBox(context);
        view.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        view.setBackgroundColor(context.getResources().getColor(color.sc_question_background_color));
        view.setText(text);

        if (getState() == ScriptComponentTree.SCRIPT_STATE_DONE)
            view.setChecked(true);

        view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    setState(ScriptComponentTree.SCRIPT_STATE_DONE);
                else
                    setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
            }
        });
        return view;
    }

    @Override
    public boolean initCheck() {
        return true;
    }
}

class ScriptComponentQuestion extends ScriptComponentViewHolder {
    private String text = "";
    private ScriptComponentTreeSheetBase component;

    public ScriptComponentQuestion(String text, ScriptComponentTreeSheetBase component) {
        this.text = text;
        this.component = component;

        setState(ScriptComponentTree.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        // we have to get a fresh counter here, if we cache it we will miss that it has been deleted in the sheet
        // component
        ScriptComponentTreeSheetBase.Counter counter = this.component.getCounter("QuestionCounter");

        TextView textView = new TextView(context);
        textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        textView.setBackgroundColor(context.getResources().getColor(color.sc_question_background_color));

        textView.setText("Q" + counter.increaseValue() + ": " + text);
        return textView;
    }

    @Override
    public boolean initCheck() {
        return true;
    }
}

class ScriptComponentTextQuestion extends ScriptComponentViewHolder {
    private String text = "";
    private String answer = "";
    private boolean optional = false;
    private ScriptComponentTreeSheetBase component;

    public ScriptComponentTextQuestion(String text, ScriptComponentTreeSheetBase component) {
        this.text = text;
        this.component = component;

        setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
        update();
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        ScriptComponentTreeSheetBase.Counter counter = this.component.getCounter("QuestionCounter");

        // Note: we have to do this programmatically cause findViewById would find the wrong child items if there are
        // more than one text question.
        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(context.getResources().getColor(color.sc_question_background_color));


        TextView textView = new TextView(context);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        textView.setText("Q" + counter.increaseValue() + ": " + text);

        EditText editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        assert editText != null;
        editText.setText(answer);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                answer = editable.toString();
                update();
            }
        });

        layout.addView(textView);
        layout.addView(editText);
        return layout;
    }

    @Override
    public boolean initCheck() {
        return true;
    }

    public void toBundle(Bundle bundle) {
        bundle.putString("answer", answer);
        super.toBundle(bundle);
    }

    public boolean fromBundle(Bundle bundle) {
        answer = bundle.getString("answer", "");
        return super.fromBundle(bundle);
    }

    private void update() {
        if (optional)
            setState(ScriptComponentTree.SCRIPT_STATE_DONE);
        else if (!answer.equals(""))
            setState(ScriptComponentTree.SCRIPT_STATE_DONE);
        else
            setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
    }
}

abstract class SheetLayout {
    protected ISheetLayoutItemParameters parameters;

    public SheetLayout() {
        this.parameters = new SheetGroupLayoutParameters();
    }

    public SheetLayout(ISheetLayoutItemParameters parameters) {
        this.parameters = parameters;
    }

    public ISheetLayoutItemParameters getParameters() {
        return parameters;
    }

    public abstract View buildLayout(Context context, android.support.v4.app.Fragment parentFragment);
}


class ScriptComponentTreeSheetBase extends ScriptComponentTreeFragmentHolder {
    public class Counter {
        private int counter = 0;

        public void setValue(int value) {
            counter = value;
        }

        public int increaseValue() {
            counter++;
            return counter;
        }
    }

    private SheetGroupLayout sheetGroupLayout = new SheetGroupLayout(LinearLayout.VERTICAL);
    private ScriptComponentContainer<ScriptComponentViewHolder> itemContainer
            = new ScriptComponentContainer<ScriptComponentViewHolder>();
    private Map<String, Counter> mapOfCounter = new HashMap<String, Counter>();

    public ScriptComponentTreeSheetBase(Script script) {
        super(script);

        itemContainer.setListener(new ScriptComponentContainer.IItemContainerListener() {
            @Override
            public void onAllItemStatusChanged(boolean allDone) {
                if (allDone)
                    setState(ScriptComponentTree.SCRIPT_STATE_DONE);
                else
                    setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
            }
        });
    }

    public SheetLayout getSheetLayout() {
        return sheetGroupLayout;
    }

    @Override
    public boolean initCheck() {
        return itemContainer.initCheck();
    }

    @Override
    public ScriptComponentGenericFragment createFragment() {
        ScriptComponentSheetFragment fragment = new ScriptComponentSheetFragment();
        fragment.setScriptComponent(this);
        return fragment;
    }

    @Override
    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        itemContainer.toBundle(bundle);
    }

    @Override
    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        return itemContainer.fromBundle(bundle);
    }

    public void setMainLayoutOrientation(String orientation) {
        if (orientation.equalsIgnoreCase("horizontal"))
            sheetGroupLayout.setOrientation(LinearLayout.HORIZONTAL);
        else
            sheetGroupLayout.setOrientation(LinearLayout.VERTICAL);
    }

    public SheetGroupLayout addHorizontalGroupLayout(SheetGroupLayout parent) {
        return addGroupLayout(LinearLayout.HORIZONTAL, parent);
    }

    public SheetGroupLayout addVerticalGroupLayout(SheetGroupLayout parent) {
        return addGroupLayout(LinearLayout.VERTICAL, parent);
    }

    protected SheetGroupLayout addGroupLayout(int orientation, SheetGroupLayout parent) {
        SheetGroupLayout layout = new SheetGroupLayout(orientation);
        if (parent == null)
            sheetGroupLayout.addLayout(layout);
        else
            parent.addLayout(layout);
        return layout;
    }

    protected SheetLayout addItemViewHolder(ScriptComponentViewHolder item,
                                            SheetGroupLayout parent) {
        itemContainer.addItem(item);
        SheetLayout layoutItem;
        if (parent == null)
            layoutItem = sheetGroupLayout.addView(item);
        else
            layoutItem = parent.addView(item);
        return layoutItem;
    }

    /**
     * The counter is valid for the lifetime of the fragment view. If not counter with the given name exist, a new
     * counter is created.
     * @param name counter name
     * @return a Counter
     */
    public Counter getCounter(String name) {
        if (!mapOfCounter.containsKey(name)) {
            Counter counter = new Counter();
            mapOfCounter.put(name, counter);
            return counter;
        }
        return mapOfCounter.get(name);
    }

    public void resetCounter() {
        mapOfCounter.clear();
    }
}


public class ScriptComponentTreeSheet extends ScriptComponentTreeSheetBase {

    public ScriptComponentTreeSheet(Script script) {
        super(script);
    }

    public ScriptComponentViewHolder addText(String text, SheetGroupLayout parent) {
        TextComponent textOnlyQuestion = new TextComponent(text);
        addItemViewHolder(textOnlyQuestion, parent);
        return textOnlyQuestion;
    }

    public ScriptComponentViewHolder addHeader(String text, SheetGroupLayout parent) {
        TextComponent component = new TextComponent(text);
        component.setTypeface(Typeface.BOLD);
        addItemViewHolder(component, parent);
        return component;
    }

    public ScriptComponentViewHolder addQuestion(String text, SheetGroupLayout parent) {
        ScriptComponentQuestion component = new ScriptComponentQuestion(text, this);
        addItemViewHolder(component, parent);
        return component;
    }

    public ScriptComponentViewHolder addTextQuestion(String text, SheetGroupLayout parent) {
        ScriptComponentTextQuestion component = new ScriptComponentTextQuestion(text, this);
        addItemViewHolder(component, parent);
        return component;
    }

    public ScriptComponentViewHolder addCheckQuestion(String text, SheetGroupLayout parent) {
        CheckBoxQuestion question = new CheckBoxQuestion(text);
        addItemViewHolder(question, parent);
        return question;
    }

    public ScriptComponentCameraExperiment addCameraExperiment(SheetGroupLayout parent) {
        ScriptComponentCameraExperiment cameraExperiment = new ScriptComponentCameraExperiment();
        addItemViewHolder(cameraExperiment, parent);
        return cameraExperiment;
    }

    public ScriptComponentPotentialEnergy1 addPotentialEnergy1Question(SheetGroupLayout parent) {
        ScriptComponentPotentialEnergy1 question = new ScriptComponentPotentialEnergy1();
        addItemViewHolder(question, parent);
        return question;
    }
}

interface IActivityStarterViewParent {
    public void startActivityForResultFromView(ActivityStarterView view, Intent intent, int requestCode);

    /**
     * The parent has to provide a unique id that stays the same also if the activity has been reloaded. For example,
     * if views get added in a fix order this should be a normal counter.
     *
     * @return a unique id
     */
    public int getNewChildId();
}

/**
 * Base class for a view that can start a child activity and is able to receive the activity response.
 */
abstract class ActivityStarterView extends FrameLayout {
    protected IActivityStarterViewParent parent;

    public ActivityStarterView(Context context, IActivityStarterViewParent parent) {
        super(context);

        setId(parent.getNewChildId());

        this.parent = parent;
    }

    public void startActivityForResult(android.content.Intent intent, int requestCode) {
        parent.startActivityForResultFromView(this, intent, requestCode);
    }

    abstract public void onActivityResult(int requestCode, int resultCode, Intent data);
}
