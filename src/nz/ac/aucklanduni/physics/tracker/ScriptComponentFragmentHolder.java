package nz.ac.aucklanduni.physics.tracker;


abstract public class ScriptComponentFragmentHolder extends ScriptComponent {
    protected Script script;

    public ScriptComponentFragmentHolder(Script script) {
        this.script = script;
    }

    abstract public android.support.v4.app.Fragment createFragment();
}


class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponent create(String componentName, Script script) {
        if (componentName.equals("QuestionsComponent"))
            return new ScriptComponentQuestions(script);
        if (componentName.equals("CameraExperiment"))
            return new ScriptComponentCameraExperiment(script);
        if (componentName.equals("ExperimentAnalysis"))
            return new ScriptComponentExperimentAnalysis(script);

        return null;
    }
}
