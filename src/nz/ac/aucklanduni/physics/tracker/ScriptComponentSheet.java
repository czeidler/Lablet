/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;


class TextComponentItem extends ScriptComponentItemViewHolder {
    private String text = "";
    public TextComponentItem (String text) {
        this.text = text;
        setState(ScriptComponent.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context) {
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

class CheckBoxQuestion extends ScriptComponentItemViewHolder {
    private String text = "";
    public CheckBoxQuestion(String text) {
        this.text = text;
        setState(ScriptComponent.SCRIPT_STATE_ONGOING);
    }

    @Override
    public View createView(Context context) {
        CheckBox view = new CheckBox(context);
        view.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        view.setText(text);

        if (getState() == ScriptComponent.SCRIPT_STATE_DONE)
            view.setChecked(true);
        
        view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    setState(ScriptComponent.SCRIPT_STATE_DONE);
                else
                    setState(ScriptComponent.SCRIPT_STATE_ONGOING);
            }
        });
        return view;
    }

    @Override
    public boolean initCheck() {
        return true;
    }
}

public class ScriptComponentSheet extends ScriptComponentFragmentHolder {
    private String layoutType = "vertical";

    private ScriptComponentItemContainer<ScriptComponentItemViewHolder> itemContainer
            = new ScriptComponentItemContainer<ScriptComponentItemViewHolder>();

    public ScriptComponentSheet(Script script) {
        super(script);

        itemContainer.setListener(new ScriptComponentItemContainer.IItemContainerListener() {
            @Override
            public void onAllItemStatusChanged(boolean allDone) {
                if (allDone)
                    setState(ScriptComponent.SCRIPT_STATE_DONE);
                else
                    setState(ScriptComponent.SCRIPT_STATE_ONGOING);
            }
        });
    }

    @Override
    public boolean initCheck() {
        return itemContainer.initCheck();
    }

    @Override
    public Fragment createFragment() {
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

    public ScriptComponentItemContainer<ScriptComponentItemViewHolder> getItemContainer() {
        return itemContainer;
    }

    public void setLayoutType(String layout) {
        layoutType = layout;
    }

    public String getLayoutType() {
        return layoutType;
    }

    public void addText(String text) {
        TextComponentItem textOnlyQuestion = new TextComponentItem(text);
        addItemViewHolder(textOnlyQuestion);
    }

    public void addCheckQuestion(String text) {
        CheckBoxQuestion question = new CheckBoxQuestion(text);
        addItemViewHolder(question);
    }

    protected void addItemViewHolder(ScriptComponentItemViewHolder item) {
        itemContainer.addItem(item);
    }
}

class ScriptComponentSheetFragment extends ScriptComponentGenericFragment {
    private TableLayout sheetLayout = null;
    TableRow row;

    public ScriptComponentSheetFragment(ScriptComponentSheet component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_sheet_fragment);
        assert child != null;

        sheetLayout = (TableLayout)child.findViewById(R.id.sheetLayout);
        assert sheetLayout != null;

        row = new TableRow(getActivity());
        sheetLayout.addView(row);

        ScriptComponentSheet sheetComponent = (ScriptComponentSheet)component;

        List<ScriptComponentItemViewHolder> itemList = sheetComponent.getItemContainer().getItems();
        for (int i = 0; i < itemList.size(); i++) {
            ScriptComponentItemViewHolder item = itemList.get(i);
            add(item.createView(getActivity()), i == itemList.size() - 1);
        }

        sheetLayout.setStretchAllColumns(true);

        return view;
    }

    private void add(View view, boolean isLast) {
        view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT, 1f));
        row.addView(view);

        int xPadding = 20;
        int yPadding = 20;
        ScriptComponentSheet sheetComponent = (ScriptComponentSheet)component;
        if (!sheetComponent.getLayoutType().equalsIgnoreCase("horizontal")) {
            row = new TableRow(getActivity());
            sheetLayout.addView(row);

            if (isLast)
                yPadding = 0;
        } else if (isLast)
            xPadding = 0;

        view.setPadding(0, 0, xPadding, yPadding);
    }
}
