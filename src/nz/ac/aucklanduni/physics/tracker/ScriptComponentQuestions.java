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
import android.widget.Button;
import android.widget.TextView;


public class ScriptComponentQuestions extends ScriptComponentFragmentHolder {
    public ScriptComponentQuestions(Script script) {
        super(script);
    }

    @Override
    public Fragment createFragment() {
        ScriptComponentQuestionsFragment fragment = new ScriptComponentQuestionsFragment(this);
        return fragment;
    }
}

class ScriptComponentQuestionsFragment extends ScriptComponentGenericFragment {

    public ScriptComponentQuestionsFragment(ScriptComponentQuestions component) {
        super(component);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View child = setChild(R.layout.script_component_questions_fragment);
        assert child != null;

        Button okButton = (Button)child.findViewById(R.id.button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setState(ScriptComponent.SCRIPT_STATE_DONE);
            }
        });

        return view;
    }

}
