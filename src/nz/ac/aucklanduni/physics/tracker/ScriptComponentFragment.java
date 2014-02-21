package nz.ac.aucklanduni.physics.tracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

abstract public class ScriptComponentFragment extends ScriptComponent {
    abstract public android.support.v4.app.Fragment createFragment();
}


class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponent create(String componentName) {
        if (componentName.equals("QuestionsComponent"))
            return new ScriptComponentQuestions();

        return null;
    }
}


class ScriptComponentQuestions extends ScriptComponentFragment {

    @Override
    public Fragment createFragment() {
        return new ScriptComponentQuestionsFragment();
    }
}

class ScriptComponentQuestionsFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.script_component_questions_fragment, container, false);
        assert view != null;

        return view;
    }

}