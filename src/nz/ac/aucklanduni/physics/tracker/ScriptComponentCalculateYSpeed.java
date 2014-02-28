/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ScriptComponentCalculateYSpeed extends ScriptComponentFragmentHolder {
    private float speed1 = 0.f;
    private float speed2 = 0.f;

    public ScriptComponentCalculateYSpeed(Script script) {
        super(script);
    }

    @Override
    public Fragment createFragment() {
        ScriptComponentCalculateSpeedFragment fragment = new ScriptComponentCalculateSpeedFragment(this);
        return fragment;
    }

}

class ScriptComponentCalculateSpeedFragment extends ScriptComponentGenericFragment {
    private LinearLayout questionLayout = null;

    public ScriptComponentCalculateSpeedFragment(ScriptComponentCalculateYSpeed component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_calculate_speed);
        assert child != null;

        return view;
    }



}
