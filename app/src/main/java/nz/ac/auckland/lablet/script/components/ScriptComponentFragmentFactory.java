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
 * This class is used when building a {@link Script} from a lua file. The user
 * can specify the pages by name and the script builder creates the script components using this factory.
 * </p>
 */
public class ScriptComponentFragmentFactory implements IScriptComponentFactory {
    public ScriptTreeNode create(String componentName, Script script) {
        if (componentName.equals("Sheet"))
            return new ScriptTreeNodeSheet(script);
        if (componentName.equals("MotionAnalysis"))
            return new ScriptTreeNodeMotionAnalysis(script);
        if (componentName.equals("FrequencyAnalysis"))
            return new ScriptTreeNodeFrequencyAnalysis(script);
        if (componentName.equals("AccelerometerAnalysis"))
            return new ScriptTreeNodeAccelerometerAnalysis(script);
        if (componentName.equals("CalculateXSpeed"))
            return new ScriptTreeNodeCalculateSpeed(script, true);
        if (componentName.equals("CalculateYSpeed"))
            return new ScriptTreeNodeCalculateSpeed(script, false);

        return null;
    }
}
