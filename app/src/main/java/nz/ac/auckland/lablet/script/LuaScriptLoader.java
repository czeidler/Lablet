/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


/**
 * Load a lua script file from disk.
 */
public class LuaScriptLoader {
    final static private String NAMESPACE = "Lablet";
    final static private String INTERFACE_VAR = "interface";
    final static private String LABEL_VAR = "label";
    final static private String ENTRY_POINT_METHOD_KEY = "buildActivity";

    /**
     * Simple script builder. Only supports components with one child.
     */
    private class ScriptBuilder {
        private IScriptComponentFactory factory;
        private Script script = new Script();
        private ScriptTreeNode lastComponent;

        public ScriptBuilder(IScriptComponentFactory factory) {
            this.factory = factory;
        }

        /**
         * Adds the component at the SCRIPT_STATE_DONE slot.
         *
         * @param component to be added
         */
        public void add(ScriptTreeNode component) {
            add(ScriptTreeNode.SCRIPT_STATE_DONE, component);
        }

        /**
         * Adds a component to the state slot and make it the current component (Successive components will be added to
         * the new component).
         *
         * @param state the slot where the component will be inserted
         * @param component to be added
         */
        public void add(int state, ScriptTreeNode component) {
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

        public ScriptTreeNode create(String componentName) {
            return factory.create(componentName, script);
        }
    }

    private String lastError = "";
    private ScriptBuilder builder;

    public LuaScriptLoader(IScriptComponentFactory factory) {
        builder = new ScriptBuilder(factory);
    }

    // this is basically a copy from the luaj code but it uses a BufferedInputStream
    static private LuaValue loadfile(Globals globals, String filename) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
            return globals.load(inputStream, "@" + filename, "bt", globals);
        } catch (Exception e) {
            return LuaValue.error("load " + filename + ": " + e);
        }
    }

    /**
     * Opens a script file and load it into a {@link Script}.
     *
     * @param scriptFile the script location
     * @return a new script or null on failure
     */
    public Script load(File scriptFile) {
        if (!scriptFile.exists()) {
            lastError = "Script file does not exist!";
            return null;
        }

        try {
            Globals globals = JsePlatform.standardGlobals();
            //LuaValue chunk = globals.loadfile(scriptFile.getPath());
            LuaValue chunk = loadfile(globals, scriptFile.getPath());
            chunk.call();

            LuaValue labletNamespace = globals.get(NAMESPACE);
            float version = labletNamespace.get(INTERFACE_VAR).tofloat();
            if (version != 1.0) {
                lastError = "Incompatible script interface or the interface variable is missing.";
                return null;
            }

            LuaValue hookFunction = labletNamespace.get(ENTRY_POINT_METHOD_KEY);
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

    static public ScriptMetaData getScriptMetaData(File scriptFile) {
        if (!scriptFile.exists()) {
            return null;
        }

        ScriptMetaData scriptMetaData = new ScriptMetaData(scriptFile);
        try {
            Globals globals = JsePlatform.standardGlobals();
            //LuaValue chunk = globals.loadfile(scriptFile.getPath());
            LuaValue chunk = loadfile(globals, scriptFile.getPath());
            chunk.call();

            LuaValue labletNamespace = globals.get(NAMESPACE);
            float version = labletNamespace.get(INTERFACE_VAR).tofloat();
            String label = labletNamespace.get(LABEL_VAR).toString();
            scriptMetaData.setInterfaceVersion(version);
            if (!label.equals("nil"))
                scriptMetaData.setLabel(label);

        } catch (LuaError e) {
            scriptMetaData.setLoadingError(e.getMessage());
        }
        return scriptMetaData;
    }

    /**
     * Returns the error message in case an error occurred in load.
     * @return error message string
     */
    public String getLastError() {
        return lastError;
    }
}
