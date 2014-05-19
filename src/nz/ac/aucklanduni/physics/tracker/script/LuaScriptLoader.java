/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;


/**
 * Load a lua script file from disk.
 */
public class LuaScriptLoader {
    private String lastError = "";
    private ScriptBuilderLua builder;

    public LuaScriptLoader(IScriptComponentFactory factory) {
        builder = new ScriptBuilderLua(factory);
    }

    // this is basically a copy from the luaj code but it uses a BufferedInputStream
    private LuaValue loadfile(Globals globals, String filename) {
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename));
            return globals.load(inputStream, "@" + filename, "bt", globals);
        } catch (Exception e) {
            return LuaValue.error("load " + filename + ": " + e);
        }
    }

    /**
     * Opens a script file and load it into a {@link Script}.
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

    /**
     * Returns the error message in case an error occurred in load.
     * @return error message string
     */
    public String getLastError() {
        return lastError;
    }
}
