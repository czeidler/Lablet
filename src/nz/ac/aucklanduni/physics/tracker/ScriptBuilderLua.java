package nz.ac.aucklanduni.physics.tracker;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;

interface IScriptComponentFactory {
    public ScriptComponent create(String componentName);
}

class LuaScriptLoader {
    private String lastError = "";
    private ScriptBuilderLua builder;

    public LuaScriptLoader(IScriptComponentFactory factory) {
        builder = new ScriptBuilderLua(factory);
    }

    public Script load(File scriptFile) {
        if (!scriptFile.exists())
            return null;

        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadFile(scriptFile.getPath());
            chunk.call();

            LuaValue hookFunction = globals.get("onBuildExperimentScript");
            hookFunction.call(CoerceJavaToLua.coerce(builder));

        } catch (LuaError e) {
            lastError = e.getMessage() + "\n";
            return null;
        }

        return builder.getScript();
    }

    public String getLastError() {
        return lastError;
    }
}

public class ScriptBuilderLua {
    private IScriptComponentFactory factory;
    private Script script = new Script();
    private ScriptComponent lastComponent;

    public ScriptBuilderLua(IScriptComponentFactory factory) {
        this.factory = factory;
    }

    public void add(LuaValue componentLua) {
        ScriptComponent component = (ScriptComponent)CoerceLuaToJava.coerce(componentLua, ScriptComponent.class);
        add(ScriptComponent.SCRIPT_STATE_DONE, component);
    }

    public void add(int state, ScriptComponent component) {
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

    public LuaValue create(String componentName) {
        ScriptComponent component = factory.create(componentName);
        LuaValue componentLua = CoerceJavaToLua.coerce(component);
        return componentLua;
    }
}
