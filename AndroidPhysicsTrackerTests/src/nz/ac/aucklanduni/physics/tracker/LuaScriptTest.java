/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;
import nz.ac.aucklanduni.physics.tracker.script.LuaScriptLoader;
import nz.ac.aucklanduni.physics.tracker.script.Script;
import nz.ac.aucklanduni.physics.tracker.script.components.ScriptComponentFragmentFactory;

import java.io.*;


@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class LuaScriptTest extends ActivityInstrumentationTestCase2<ScriptActivity> {
    private Activity activity;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public LuaScriptTest() {
        super(ScriptActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();

        copyResourceScripts(true);
    }

    /**
     * Load a script and check its initial state.
     */
    public void testScriptLoading() {
        File dir = ScriptActivity.getScriptDirectory(activity);
        assertNotNull(dir);

        Context testContext = getInstrumentation().getContext();
        File scriptFile = new File(getScriptDirectory(testContext), "ScriptTesting.lua");
        assertTrue(scriptFile.exists());

        ScriptComponentFragmentFactory factory = new ScriptComponentFragmentFactory();
        LuaScriptLoader loader = new LuaScriptLoader(factory);
        Script script = loader.load(scriptFile);
        assertNotNull(script);

        assertTrue(script.start());

        assertEquals(1, script.getActiveChain().size());
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    static public File getScriptDirectory(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        File scriptDir = new File(baseDir, "scripts");
        if (!scriptDir.exists())
            scriptDir.mkdir();
        return scriptDir;
    }

    private void copyResourceScripts(boolean overwriteExisting) {
        File scriptDir = ScriptActivity.getScriptDirectory(getInstrumentation().getContext());
        if (!scriptDir.exists()) {
            if (!scriptDir.mkdir())
                return;
        }
        try {
            String[] files = getInstrumentation().getContext().getAssets().list("");
            for (String file : files) {
                if (!isLuaFile(file))
                    continue;
                InputStream inputStream = getInstrumentation().getContext().getAssets().open(file);
                File scriptOutFile = new File(scriptDir, file);
                if (!overwriteExisting && scriptOutFile.exists())
                    continue;

                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(scriptOutFile, false));
                byte[] buffer = new byte[16384];
                while(true) {
                    int n = inputStream.read(buffer);
                    if (n <= -1)
                        break;
                    outputStream.write(buffer, 0, n);
                }

                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isLuaFile(String name) {
        return name.lastIndexOf(".lua") == name.length() - 4;
    }
}
