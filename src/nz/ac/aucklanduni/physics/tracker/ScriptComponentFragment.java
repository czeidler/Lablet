package nz.ac.aucklanduni.physics.tracker;


abstract public class ScriptComponentFragment extends ScriptComponent {
    protected Script script;

    public ScriptComponentFragment(Script script) {
        this.script = script;
    }

    abstract public android.support.v4.app.Fragment createFragment();
}


class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponent create(String componentName, Script script) {
        if (componentName.equals("QuestionsComponent"))
            return new ScriptComponentQuestions(script);

        return null;
    }
}
