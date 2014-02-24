/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.os.Bundle;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


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

public class ScriptComponent {
    final static public int SCRIPT_STATE_INACTIVE = -2;
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    private Bundle stateData = null;
    private ScriptComponent parent = null;
    private int state = SCRIPT_STATE_INACTIVE;
    private Map<Integer, ScriptComponent> connections = new HashMap<Integer, ScriptComponent>();

    static class Iterator {
        private ScriptComponent currentComponent;
        private java.util.Iterator<ScriptComponent> currentComponentIterator;
        private ScriptComponent.Iterator childIterator;

        Iterator(ScriptComponent root) {
            currentComponent = root;
            currentComponentIterator = currentComponent.connections.values().iterator();
        }

        ScriptComponent next() {
            ScriptComponent child = currentComponentIterator.next();
            if (child == null)
                return null;

            if (childIterator == null) {
                childIterator = new Iterator(child);
                return child;
            } else {
                child = childIterator.next();
                if (child == null) {
                    childIterator = null;
                    return next();
                } else
                    return child;
            }
        }
    }

    static String getChainHash(ScriptComponent component) {
        String hashData = component.getName();
        java.util.Iterator<ScriptComponent> iterator = component.connections.values().iterator();
        int childId = -1;
        while (true) {
            ScriptComponent child = iterator.next();
            if (child == null)
                break;
            childId++;

            hashData += childId;
            hashData += getChainHash(child);
        }

        return Hash.sha1Hex(hashData);
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
    }

    public void setState(int state, Bundle stateData) {
        this.state = state;
        this.stateData = stateData;
        onStateChanged(state);
    }

    protected void onStateChanged(int state) {

    }

    public int getState() {
        return state;
    }

    public void setStateData(Bundle data) {
        stateData = data;
    }

    public Bundle getStateData() {
        return stateData;
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
}

class Script {
    public interface IScriptListener {
        public void onCurrentComponentChanged(ScriptComponent current);
    }

    private ScriptComponent root = null;
    private ScriptComponent currentComponent = null;
    private IScriptListener listener = null;

    public void setListener(IScriptListener listener) {
        this.listener = listener;
    }

    public void setRoot(ScriptComponent component) {
        root = component;
    }

    public ScriptComponent getRoot() {
        return root;
    }

    public boolean start() {
        // already started?
        if (currentComponent != null)
            return false;

        if (root == null)
            return false;

        setCurrentComponent(root);
        return true;
    }

    public void saveScript(Bundle bundle) {
        bundle.putString("scriptId", ScriptComponent.getChainHash(root));

        int componentId = -1;
        ScriptComponent.Iterator iterator = new ScriptComponent.Iterator(root);
        while (true) {
            ScriptComponent component = iterator.next();
            if (component == null)
                break;
            componentId++;

            Bundle componentBundle = new Bundle();
            component.toBundle(componentBundle);

            String bundleKey = Integer.toString(componentId);
            bundle.putBundle(bundleKey, componentBundle);
        }
    }

    public boolean loadScript(Bundle bundle) {
        String scriptId = ScriptComponent.getChainHash(root);
        if (!bundle.get("scriptId").equals(scriptId)) {
            return false;
        }

        int componentId = -1;
        ScriptComponent.Iterator iterator = new ScriptComponent.Iterator(root);
        while (true) {
            ScriptComponent component = iterator.next();
            if (component == null)
                break;
            componentId++;

            String bundleKey = Integer.toString(componentId);
            if (!bundle.containsKey(bundleKey))
                return false;
            if (!component.fromBundle(bundle.getBundle(bundleKey)))
                return false;
        }

        return true;
    }

    public ScriptComponent getCurrentComponent() {
        return currentComponent;
    }

    public void setCurrentComponent(ScriptComponent component) {
        currentComponent = component;
        currentComponent.setState(ScriptComponent.SCRIPT_STATE_ONGOING);

        if (listener != null)
            listener.onCurrentComponentChanged(currentComponent);
    }

    public boolean cancelCurrent() {
        if (currentComponent == null)
            return false;

        ScriptComponent parent = currentComponent.getParent();
        if (parent == null)
            return false;

        currentComponent.setStateData(null);
        setCurrentComponent(parent);
        return true;
    }

    public boolean backToParent() {
        if (currentComponent == null)
            return false;

        ScriptComponent parent = currentComponent.getParent();
        if (parent == null)
            return false;

        setCurrentComponent(parent);
        return true;
    }

    public boolean next() {
        if (currentComponent == null)
            return false;

        ScriptComponent next = currentComponent.getNext();
        if (next == null)
            return false;

        setCurrentComponent(next);
        return true;
    }
}

