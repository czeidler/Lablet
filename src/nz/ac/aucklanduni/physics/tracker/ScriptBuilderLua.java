/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;


interface IScriptComponentFactory {
    public ScriptComponentTree create(String componentName, Script script);
}

class LuaScriptLoader {
    private String lastError = "";
    private ScriptBuilderLua builder;

    public LuaScriptLoader(IScriptComponentFactory factory) {
        builder = new ScriptBuilderLua(factory);
    }

    public Script load(File scriptFile) {
        if (!scriptFile.exists()) {
            lastError = "Script file does not exist!";
            return null;
        }

        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(scriptFile.getPath());
            chunk.call();

            LuaValue hookFunction = globals.get("onBuildExperimentScript");
            LuaValue arg = CoerceJavaToLua.coerce(builder);
            hookFunction.call(arg);
        } catch (LuaError e) {
            lastError = e.getMessage();
            return null;
        }

        Script script = builder.getScript();
        if (!script.initCheck()) {
            lastError = script.getLastError();
            return null;
        }
        return script;
    }

    public String getLastError() {
        return lastError;
    }
}

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
            lastComponent.setNextComponent(state, component);
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
