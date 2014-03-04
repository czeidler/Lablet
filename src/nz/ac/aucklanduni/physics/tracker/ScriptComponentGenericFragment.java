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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;


public class ScriptComponentGenericFragment extends android.support.v4.app.Fragment
        implements ScriptComponentTree.IScriptComponentListener{
    protected ScriptComponentTree component;
    protected TextView titleView = null;
    protected Button finishComponentButton = null;
    protected ScrollView containerView = null;

    ScriptComponentGenericFragment(ScriptComponentTree component) {
        this.component = component;
        component.setListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.script_component_generic_fragment, container, false);
        assert view != null;

        titleView = (TextView)view.findViewById(R.id.titleTextView);
        String title = ((ScriptComponentTreeFragmentHolder)component).getTitle();
        if (title.equals(""))
            setTitle(this.getClass().getSimpleName());
        else
            setTitle(title);

        finishComponentButton = (Button)view.findViewById(R.id.finishComponentButton);
        assert finishComponentButton != null;

        finishComponentButton.setEnabled(false);

        finishComponentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                component.getScript().notifyGoToComponent(component.getNext());
            }
        });

        containerView = (ScrollView)view.findViewById(R.id.childContainer);
        assert containerView != null;

        setState(component.getState());

        return view;
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    protected void setState(int state) {
        component.setState(state);
    }

    protected View setChild(int layoutId) {
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(layoutId, containerView, true);
        assert view != null;

        return view;
    }

    @Override
    public void onStateChanged(ScriptComponent item, int state) {
        assert item == component;

        if (state >= 0)
            finishComponentButton.setEnabled(true);
        else
            finishComponentButton.setEnabled(false);
    }
}
