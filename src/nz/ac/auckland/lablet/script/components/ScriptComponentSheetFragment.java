/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;


/**
 * Fragment for the script sheet component.
 */
public class ScriptComponentSheetFragment extends ScriptComponentGenericFragment implements IActivityStarterViewParent {
    private FrameLayout sheetLayout = null;
    private int childViewThatHasStartedAnActivity = -1;
    private int childViewId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        childViewId = -1;

        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (component == null)
            return view;

        if (component == null)
            return view;

        View child = setChild(R.layout.script_component_sheet_fragment);
        assert child != null;

        sheetLayout = (FrameLayout)child.findViewById(R.id.sheetLayout);
        assert sheetLayout != null;

        ScriptComponentTreeSheet sheetComponent = (ScriptComponentTreeSheet)component;
        sheetComponent.resetCounter();
        View sheetView = sheetComponent.getSheetLayout().buildLayout(getActivity(), this);
        sheetLayout.addView(sheetView);

        return view;
    }

    public int getNewChildId() {
        childViewId ++;
        return childViewId;
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
