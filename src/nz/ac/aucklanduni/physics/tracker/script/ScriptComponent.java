/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.*;


abstract public class ScriptComponent {
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    public interface IScriptComponentListener {
        public void onStateChanged(ScriptComponent item, int state);
    }

    private WeakReference<IScriptComponentListener> listener = null;
    private int state = SCRIPT_STATE_ONGOING;
    protected String lastErrorMessage = "";

    /**
     * Checks if the component has all information to operate correctly, i.e., everything is setup correctly in the
     * script.
     * @return true if status is ok.
     */
    abstract public boolean initCheck();
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setListener(IScriptComponentListener listener) {
        this.listener = new WeakReference<IScriptComponentListener>(listener);
    }

    // state < 0 means item is not done yet
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        if (listener != null) {
            IScriptComponentListener listenerHard = listener.get();
            if (listenerHard != null)
                listenerHard.onStateChanged(this, state);
        }
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

    public boolean hasChild() {
        if (connections.size() > 0)
            return true;
        return false;
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