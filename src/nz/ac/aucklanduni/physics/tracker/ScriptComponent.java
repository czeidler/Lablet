/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;

import java.io.File;
import java.security.MessageDigest;
import java.util.*;


class Hash {
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        // from: http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String sha1Hex(String data) {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(data.getBytes("UTF-8"));
            sha1 = bytesToHex(crypt.digest());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }
}

abstract public class ScriptComponent implements Iterable<ScriptComponent> {
    final static public int SCRIPT_STATE_INACTIVE = -2;
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    private Script script = null;
    private ScriptComponent parent = null;
    private int state = SCRIPT_STATE_INACTIVE;
    private Map<Integer, ScriptComponent> connections = new HashMap<Integer, ScriptComponent>();

    protected String lastErrorMessage = "";

    public ScriptComponent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    abstract public boolean initCheck();
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public java.util.Iterator<ScriptComponent> iterator() {
        return new Iterator(this);
    }

    static String getChainHash(ScriptComponent component) {
        String hashData = component.getName();
        java.util.Iterator<ScriptComponent> iterator = component.connections.values().iterator();
        int childId = -1;
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponent child = iterator.next();
            childId++;

            hashData += childId;
            hashData += getChainHash(child);
        }

        return Hash.sha1Hex(hashData);
    }

    public List<ScriptComponent> getActiveChain() {
        List<ScriptComponent> list = new ArrayList<ScriptComponent>();
        if (state == SCRIPT_STATE_INACTIVE)
            return list;
        list.add(this);
        ScriptComponent current = this;
        while (true) {
            current = current.connections.get(current.getState());
            if (current == null)
                break;
            list.add(current);
        }
        return list;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void toBundle(Bundle bundle) {
        bundle.putInt("state", state);
    }

    public boolean fromBundle(Bundle bundle) {
        if (!bundle.containsKey("state"))
            return false;
        state = bundle.getInt("state");
        return true;
    }

    public void setNextComponent(int state, ScriptComponent component) {
        connections.put(state, component);
        component.setParent(this);
    }

    public ScriptComponent getNext() {
        if (state < 0)
            return null;
        return connections.get(state);
    }

    public void setState(int state) {
        this.state = state;
        script.onComponentStateChanged(this, state);
    }

    public int getState() {
        return state;
    }

    public ScriptComponent getParent() {
        return parent;
    }

    public int getStepsToRoot() {
        int stepsToRoot = 1;
        ScriptComponent currentParent = parent;
        while (currentParent != null) {
            stepsToRoot++;
            currentParent = currentParent.getParent();
        }
        return stepsToRoot;
    }

    private void setParent(ScriptComponent parent) {
        this.parent = parent;
    }

    static private class Iterator implements java.util.Iterator<ScriptComponent> {
        private ScriptComponent currentComponent;
        private java.util.Iterator<ScriptComponent> currentComponentIterator;
        private java.util.Iterator<ScriptComponent> childIterator;

        Iterator(ScriptComponent root) {
            currentComponent = root;
            currentComponentIterator = currentComponent.connections.values().iterator();
        }

        @Override
        public ScriptComponent next() {
            if (childIterator == null) {
                ScriptComponent child = currentComponentIterator.next();
                childIterator = child.iterator();
                return child;
            } else {
                ScriptComponent child = childIterator.next();
                if (!childIterator.hasNext())
                    childIterator = null;

                return child;
            }
        }

        @Override
        public void remove() {

        }

        @Override
        public boolean hasNext() {
            if (childIterator != null && childIterator.hasNext())
                return true;
            return currentComponentIterator.hasNext();
        }
    }
}

class Script {
    public interface IScriptListener {
        public void onComponentStateChanged(ScriptComponent current, int state);
        public void onGoToComponent(ScriptComponent next);
    }

    private ScriptComponent root = null;
    private IScriptListener listener = null;
    private String lastError = "";

    public void setListener(IScriptListener listener) {
        this.listener = listener;
    }

    public void notifyGoToComponent(ScriptComponent next) {
        if (listener == null || next == null)
            return;
        listener.onGoToComponent(next);
    }

    public void setRoot(ScriptComponent component) {
        root = component;
    }

    public ScriptComponent getRoot() {
        return root;
    }

    /**
     * Check if all components are initialized correctly.
     *
     * @return true if script is ok
     */
    public boolean initCheck() {
        java.util.Iterator<ScriptComponent> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponent component = iterator.next();
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

    public List<ScriptComponent> getActiveChain() {
        if (root == null)
            return new ArrayList<ScriptComponent>();

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
        if (root.getState() >= ScriptComponent.SCRIPT_STATE_ONGOING)
            return false;
        root.setState(ScriptComponent.SCRIPT_STATE_ONGOING);
        return true;
    }

    public void onComponentStateChanged(ScriptComponent component, int state) {
        if (listener != null)
            listener.onComponentStateChanged(component, state);
    }

    public boolean saveScript(Bundle bundle) {
        if (root == null)
            return false;

        bundle.putString("scriptId", ScriptComponent.getChainHash(root));

        if (!saveScriptComponent(root, 0, bundle))
            return false;

        int componentId = 0;
        java.util.Iterator<ScriptComponent> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponent component = iterator.next();
            componentId++;

            if (!saveScriptComponent(component, componentId, bundle))
                return false;
        }
        return true;
    }

    private boolean saveScriptComponent(ScriptComponent component, int componentId, Bundle bundle) {
        Bundle componentBundle = new Bundle();
        component.toBundle(componentBundle);

        String bundleKey = Integer.toString(componentId);
        bundle.putBundle(bundleKey, componentBundle);
        return true;
    }

    public boolean loadScript(Bundle bundle) {
        if (root == null)
            return false;

        String scriptId = ScriptComponent.getChainHash(root);
        if (!bundle.get("scriptId").equals(scriptId)) {
            lastError = "Script has been updated and is now incompatible to the saved state.";
            return false;
        }

        if (!loadScriptComponent(root, 0, bundle))
            return false;

        int componentId = 0;
        java.util.Iterator<ScriptComponent> iterator = root.iterator();
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponent component = iterator.next();
            componentId++;

            if (!loadScriptComponent(component, componentId, bundle))
                return false;
        }

        return true;
    }

    private boolean loadScriptComponent(ScriptComponent component, int componentId, Bundle bundle) {
        String bundleKey = Integer.toString(componentId);
        if (!bundle.containsKey(bundleKey)) {
            lastError = "Script component state can't be restored.";
            return false;
        }
        return component.fromBundle(bundle.getBundle(bundleKey));
    }
}

