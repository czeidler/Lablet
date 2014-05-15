/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class Script {
    private ScriptComponentTree root = null;
    private IScriptListener listener = null;
    private String lastError = "";

    public void setListener(IScriptListener listener) {
        this.listener = listener;
    }

    public void notifyGoToComponent(ScriptComponentTree next) {
        if (listener == null || next == null)
            return;
        listener.onGoToComponent(next);
    }

    public void setRoot(ScriptComponentTree component) {
        root = component;
    }

    public ScriptComponentTree getRoot() {
        return root;
    }

    /**
     * Check if all components are initialized correctly.
     *
     * @return true if script is ok
     */
    public boolean initCheck() {
        java.util.Iterator<ScriptComponentTree> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponentTree component = iterator.next();
            if (!component.initCheck()) {
                lastError = "In Component \"" + component.getName() + "\": ";
                lastError += component.getLastErrorMessage();
                return false;
            }
        }
        return true;
    }

    public String getLastError() {
        return lastError;
    }

    public List<ScriptComponentTree> getActiveChain() {
        if (root == null)
            return new ArrayList<ScriptComponentTree>();

        return root.getActiveChain();
    }

    static public File getScriptDirectory(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        File scriptDir = new File(baseDir, "scripts");
        if (!scriptDir.exists())
            scriptDir.mkdir();
        return scriptDir;
    }

    static public File getScriptUserDataDir(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        File scriptDir = new File(baseDir, "script_user_data");
        if (!scriptDir.exists())
            scriptDir.mkdir();
        return scriptDir;
    }

    static public String generateScriptUid(String scriptName) {
        Time now = new Time(Time.getCurrentTimezone());
        CharSequence dateString = android.text.format.DateFormat.format("yyyy-MM-dd_hh-mm-ss", new java.util.Date());

        now.setToNow();
        String newUid = "";
        if (!scriptName.equals("")) {
            newUid += scriptName;
            newUid += "_";
        }
        newUid += dateString;
        return newUid;
    }

    public boolean start() {
        if (root == null)
            return false;
        if (root.getState() > ScriptComponentTree.SCRIPT_STATE_ONGOING)
            return false;
        root.setState(ScriptComponentTree.SCRIPT_STATE_ONGOING);
        return true;
    }

    public void onComponentStateChanged(ScriptComponentTree component, int state) {
        if (listener != null)
            listener.onComponentStateChanged(component, state);
    }

    public boolean saveScript(Bundle bundle) {
        if (root == null)
            return false;

        bundle.putString("scriptId", ScriptComponentTree.getChainHash(root));

        if (!saveScriptComponent(root, 0, bundle))
            return false;

        int componentId = 0;
        java.util.Iterator<ScriptComponentTree> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponentTree component = iterator.next();
            componentId++;

            if (!saveScriptComponent(component, componentId, bundle))
                return false;
        }
        return true;
    }

    private boolean saveScriptComponent(ScriptComponentTree component, int componentId, Bundle bundle) {
        Bundle componentBundle = new Bundle();
        component.toBundle(componentBundle);

        String bundleKey = Integer.toString(componentId);
        bundle.putBundle(bundleKey, componentBundle);
        return true;
    }

    public boolean loadScript(Bundle bundle) {
        if (root == null)
            return false;

        String scriptId = ScriptComponentTree.getChainHash(root);
        if (!bundle.get("scriptId").equals(scriptId)) {
            lastError = "Script has been updated and is now incompatible to the saved state.";
            return false;
        }

        if (!loadScriptComponent(root, 0, bundle))
            return false;

        int componentId = 0;
        java.util.Iterator<ScriptComponentTree> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponentTree component = iterator.next();
            componentId++;

            if (!loadScriptComponent(component, componentId, bundle))
                return false;
        }

        return true;
    }

    private boolean loadScriptComponent(ScriptComponentTree component, int componentId, Bundle bundle) {
        String bundleKey = Integer.toString(componentId);
        if (!bundle.containsKey(bundleKey)) {
            lastError = "Script component state can't be restored.";
            return false;
        }
        return component.fromBundle(bundle.getBundle(bundleKey));
    }
}
