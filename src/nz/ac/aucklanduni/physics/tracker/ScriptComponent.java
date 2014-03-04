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


abstract public class ScriptComponent {
    final static public int SCRIPT_STATE_INACTIVE = -2;
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    public interface IScriptComponentListener {
        public void onStateChanged(ScriptComponent item, int state);
    }

    private IScriptComponentListener listener = null;
    private int state = SCRIPT_STATE_ONGOING;
    protected String lastErrorMessage = "";

    abstract public boolean initCheck();
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setListener(IScriptComponentListener listener) {
        this.listener = listener;
    }

    // state < 0 means item is not done yet
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        if (listener != null)
            listener.onStateChanged(this, state);
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
}

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

abstract class ScriptComponentTree extends ScriptComponent implements Iterable<ScriptComponentTree> {
    private Script script = null;
    private ScriptComponentTree parent = null;
    private Map<Integer, ScriptComponentTree> connections = new HashMap<Integer, ScriptComponentTree>();

    public ScriptComponentTree(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    @Override
    public java.util.Iterator<ScriptComponentTree> iterator() {
        return new Iterator(this);
    }

    static String getChainHash(ScriptComponentTree component) {
        String hashData = component.getName();
        java.util.Iterator<ScriptComponentTree> iterator = component.connections.values().iterator();
        int childId = -1;
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponentTree child = iterator.next();
            childId++;

            hashData += childId;
            hashData += getChainHash(child);
        }

        return Hash.sha1Hex(hashData);
    }

    public List<ScriptComponentTree> getActiveChain() {
        List<ScriptComponentTree> list = new ArrayList<ScriptComponentTree>();
        if (getState() == SCRIPT_STATE_INACTIVE)
            return list;
        list.add(this);
        ScriptComponentTree current = this;
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

    public void setNextComponent(int state, ScriptComponentTree component) {
        connections.put(state, component);
        component.setParent(this);
    }

    public ScriptComponentTree getNext() {
        int state = getState();
        if (state < 0)
            return null;
        return connections.get(state);
    }

    @Override
    public void setState(int state) {
        super.setState(state);
        script.onComponentStateChanged(this, state);
    }

    public ScriptComponentTree getParent() {
        return parent;
    }

    public int getStepsToRoot() {
        int stepsToRoot = 1;
        ScriptComponentTree currentParent = parent;
        while (currentParent != null) {
            stepsToRoot++;
            currentParent = currentParent.getParent();
        }
        return stepsToRoot;
    }

    private void setParent(ScriptComponentTree parent) {
        this.parent = parent;
    }

    static private class Iterator implements java.util.Iterator<ScriptComponentTree> {
        private ScriptComponentTree currentComponent;
        private java.util.Iterator<ScriptComponentTree> currentComponentIterator;
        private java.util.Iterator<ScriptComponentTree> childIterator;

        Iterator(ScriptComponentTree root) {
            currentComponent = root;
            currentComponentIterator = currentComponent.connections.values().iterator();
        }

        @Override
        public ScriptComponentTree next() {
            if (childIterator == null) {
                ScriptComponentTree child = currentComponentIterator.next();
                childIterator = child.iterator();
                return child;
            } else {
                ScriptComponentTree child = childIterator.next();
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
        public void onComponentStateChanged(ScriptComponentTree current, int state);
        public void onGoToComponent(ScriptComponentTree next);
    }

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
        if (root.getState() >= ScriptComponentTree.SCRIPT_STATE_ONGOING)
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

