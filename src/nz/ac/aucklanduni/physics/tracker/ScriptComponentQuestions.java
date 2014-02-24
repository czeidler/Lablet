package nz.ac.aucklanduni.physics.tracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ScriptComponentQuestions extends ScriptComponentFragmentHolder {
    private String title = "Questions:";

    public ScriptComponentQuestions(Script script) {
        super(script);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Fragment createFragment() {
        ScriptComponentQuestionsFragment fragment = new ScriptComponentQuestionsFragment(script);
        fragment.setTitle(title);

        return fragment;
    }
}

class ScriptComponentQuestionsFragment extends android.support.v4.app.Fragment {
    private Script script;

    private String title = "";

    public ScriptComponentQuestionsFragment(Script script) {
        this.script = script;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.script_component_questions_fragment, container, false);
        assert view != null;

        TextView titleView = (TextView)view.findViewById(R.id.titleLabel);
        titleView.setText(title);

        Button okButton = (Button)view.findViewById(R.id.button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                script.getCurrentComponent().setState(ScriptComponent.SCRIPT_STATE_DONE);
                script.next();
            }
        });
        return view;
    }

}
