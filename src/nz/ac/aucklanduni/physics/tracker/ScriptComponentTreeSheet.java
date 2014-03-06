/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;


class TextComponent extends ScriptComponentViewHolder {
    private String text = "";
    public TextComponent(String text) {
        this.text = text;
        setState(ScriptComponentTree.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parent) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
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


public class ScriptComponentTreeSheet extends ScriptComponentTreeFragmentHolder {
    private SheetGroupLayout sheetGroupLayout = new SheetGroupLayout(true);
    private ScriptComponentContainer<ScriptComponentViewHolder> itemContainer
            = new ScriptComponentContainer<ScriptComponentViewHolder>();

    public ScriptComponentTreeSheet(Script script) {
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
    public android.support.v4.app.Fragment createFragment() {
        ScriptComponentSheetFragment fragment = new ScriptComponentSheetFragment(this);
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

    public void setLayoutType(String layoutType) {
        if (layoutType.equalsIgnoreCase("horizontal"))
            sheetGroupLayout.setOrientation(false);
        else
            sheetGroupLayout.setOrientation(true);
    }

    public ScriptComponentViewHolder addText(String text, SheetGroupLayout parent) {
        TextComponent textOnlyQuestion = new TextComponent(text);
        addItemViewHolder(textOnlyQuestion, parent);
        return textOnlyQuestion;
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

    public SheetGroupLayout addGroupLayout(boolean vertical, SheetGroupLayout parent) {
        SheetGroupLayout layout = new SheetGroupLayout(vertical);
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
}

abstract class ActivityStarterView extends FrameLayout {
    protected ScriptComponentSheetFragment sheetFragment;

    public ActivityStarterView(Context context, ScriptComponentSheetFragment sheetFragment) {
        super(context);

        setId(View.generateViewId());

        this.sheetFragment = sheetFragment;
    }

    public void startActivityForResult(android.content.Intent intent, int requestCode) {
        sheetFragment.startActivityForResultFromView(this, intent, requestCode);
    }

    abstract public void onActivityResult(int requestCode, int resultCode, Intent data);
}


class ScriptComponentSheetFragment extends ScriptComponentGenericFragment {
    private FrameLayout sheetLayout = null;
    private int childViewThatHasStartedAnActivity = -1;

    public ScriptComponentSheetFragment(ScriptComponentTreeSheet component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_sheet_fragment);
        assert child != null;

        sheetLayout = (FrameLayout)child.findViewById(R.id.sheetLayout);
        assert sheetLayout != null;

        ScriptComponentTreeSheet sheetComponent = (ScriptComponentTreeSheet)component;
        View sheetView = sheetComponent.getSheetLayout().buildLayout(getActivity(), this);
        sheetLayout.addView(sheetView);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            childViewThatHasStartedAnActivity
                    = savedInstanceState.getInt("childViewThatHasStartedAnActivity", -1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("childViewThatHasStartedAnActivity", childViewThatHasStartedAnActivity);
    }

    public void startActivityForResultFromView(ActivityStarterView view, Intent intent, int requestCode) {
        childViewThatHasStartedAnActivity = view.getId();
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        View view = getView();
        if (childViewThatHasStartedAnActivity >= 0 && view != null) {
            ActivityStarterView starterView = (ActivityStarterView)view.findViewById(childViewThatHasStartedAnActivity);
            if (starterView == null) {
                super.onActivityResult(requestCode, resultCode, data);
                childViewThatHasStartedAnActivity = -1;
                return;
            }
            starterView.onActivityResult(requestCode, resultCode, data);
            childViewThatHasStartedAnActivity = -1;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
