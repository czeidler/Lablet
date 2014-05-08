/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;


abstract public class ScriptComponentTreeFragmentHolder extends ScriptComponentTree {
    protected String title = "";

    public ScriptComponentTreeFragmentHolder(Script script) {
        super(script);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    abstract public ScriptComponentGenericFragment createFragment();
}


class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponentTree create(String componentName, Script script) {
        if (componentName.equals("Sheet"))
            return new ScriptComponentTreeSheet(script);
        //if (componentName.equals("CameraExperiment"))
          //  return new ScriptComponentTreeCameraExperiment(script);
        if (componentName.equals("ExperimentSheet"))
            return new ScriptComponentTreeExperimentSheet(script);
        if (componentName.equals("ExperimentAnalysis"))
            return new ScriptComponentTreeExperimentAnalysis(script);
        if (componentName.equals("CalculateXSpeed"))
            return new ScriptComponentTreeCalculateSpeed(script, true);
        if (componentName.equals("CalculateYSpeed"))
            return new ScriptComponentTreeCalculateSpeed(script, false);

        return null;
    }
}
