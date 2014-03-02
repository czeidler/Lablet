/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


abstract public class ScriptComponentFragmentHolder extends ScriptComponent {
    protected String title = "";

    public ScriptComponentFragmentHolder(Script script) {
        super(script);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    abstract public android.support.v4.app.Fragment createFragment();
}


class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponent create(String componentName, Script script) {
        if (componentName.equals("Sheet"))
            return new ScriptComponentSheet(script);
        if (componentName.equals("CameraExperiment"))
            return new ScriptComponentCameraExperiment(script);
        if (componentName.equals("ExperimentAnalysis"))
            return new ScriptComponentExperimentAnalysis(script);
        if (componentName.equals("CalculateYSpeed"))
            return new ScriptComponentCalculateYSpeed(script);

        return null;
    }
}
