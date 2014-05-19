/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script.components;

import nz.ac.aucklanduni.physics.tracker.script.*;

public class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptComponentTree create(String componentName, Script script) {
        if (componentName.equals("Sheet"))
            return new ScriptComponentTreeSheet(script);
        if (componentName.equals("ExperimentAnalysis"))
            return new ScriptComponentTreeExperimentAnalysis(script);
        if (componentName.equals("CalculateXSpeed"))
            return new ScriptComponentTreeCalculateSpeed(script, true);
        if (componentName.equals("CalculateYSpeed"))
            return new ScriptComponentTreeCalculateSpeed(script, false);

        return null;
    }
}
