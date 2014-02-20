/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;

public class ScriptRunner {
    String lastError = "";
    Script script = new Script();

    public boolean run(File scriptFile) {
        if (!scriptFile.exists())
            return false;

        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadFile(scriptFile.getPath());
            chunk.call();

            LuaValue scriptLuaValue = CoerceJavaToLua.coerce(script);

            LuaValue testFunction = globals.get("onBuildExperimentScript");
            testFunction.call(scriptLuaValue);

        } catch (LuaError e) {
            lastError = e.getMessage() + "\n";
            return false;
        }


        script.start();
        return true;
    }

    public String getLastError() {
        return lastError;
    }
}
