/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;


public class ScriptBuilderLua {
    private IScriptComponentFactory factory;
    private Script script = new Script();
    private ScriptComponentTree lastComponent;

    public ScriptBuilderLua(IScriptComponentFactory factory) {
        this.factory = factory;
    }

    public void add(ScriptComponentTree component) {
        add(ScriptComponentTree.SCRIPT_STATE_DONE, component);
    }

    public void add(int state, ScriptComponentTree component) {
        if (lastComponent == null) {
            script.setRoot(component);
            lastComponent = component;
        } else {
            lastComponent.setChildComponent(state, component);
            lastComponent = component;
        }
    }

    public Script getScript() {
        return script;
    }

    public ScriptComponentTree create(String componentName) {
        return factory.create(componentName, script);
    }
}
