/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import nz.ac.auckland.lablet.script.*;


/**
 * Factory to create script components by name.
 * <p>
 * This class is used when building a {@link nz.ac.auckland.lablet.script.Script} from a lua file. The user
 * can specify the pages by name and the script builder creates the script components using this factory.
 * </p>
 */
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
