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
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.List;


class TextOnlyQuestion extends ScriptComponentItemViewHolder {
    private String text = "";
    public TextOnlyQuestion(String text) {
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

public class ScriptComponentSheet extends ScriptComponentFragmentHolder {
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

    public ScriptComponentItemContainer<ScriptComponentItemViewHolder> getItemContainer() {
        return itemContainer;
    }

    public void addTextOnlyQuestion(String text) {
        TextOnlyQuestion textOnlyQuestion = new TextOnlyQuestion(text);
        itemContainer.addItem(textOnlyQuestion);
    }
}

class ScriptComponentSheetFragment extends ScriptComponentGenericFragment {
    private LinearLayout questionLayout = null;

    public ScriptComponentSheetFragment(ScriptComponentSheet component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_questions_fragment);
        assert child != null;

        questionLayout = (LinearLayout)child.findViewById(R.id.questionLayout);
        assert questionLayout != null;

        ScriptComponentSheet questionsComponent = (ScriptComponentSheet)component;
        List<ScriptComponentItemViewHolder> itemList = questionsComponent.getItemContainer().getItems();
        for (ScriptComponentItemViewHolder item : itemList) {
            questionLayout.addView(item.createView(getActivity()));
            addSpace();
        }

        return view;
    }

    private void addSpace() {
        Space space = new Space(getActivity());
        space.setMinimumHeight(20);
        questionLayout.addView(space);
    }
}
