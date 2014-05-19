package nz.ac.aucklanduni.physics.tracker.script;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper class to create sha1 hex strings.
 */
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


/**
 * Abstract base class for a component that has child components. A tree component can have multiple child. Each child
 * is assigned to a component state. For example, a script can be continued depending on the state of the current
 * component. A {@link nz.ac.aucklanduni.physics.tracker.script.ScriptComponentTree} has a link to its parent (if it exists).
 */
abstract public class ScriptComponentTree extends ScriptComponent implements Iterable<ScriptComponentTree> {
    private Script script = null;
    private ScriptComponentTree parent = null;
    private Map<Integer, ScriptComponentTree> connections = new HashMap<Integer, ScriptComponentTree>();

    public ScriptComponentTree(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    /**
     * Iterator to list all child (and their child) of this component.
     *
     * @return iterator for all child and their child
     */
    @Override
    public java.util.Iterator<ScriptComponentTree> iterator() {
        return new Iterator(this);
    }

    /**
     * Starting from the given component a hash is calculated that identifies the whole tree. The tree hash is dependent
     * from the child and their child components.
     * @param component
     * @return the hash of the whole tree
     */
    static public String getTreeHash(ScriptComponentTree component) {
        String hashData = component.getName();
        java.util.Iterator<ScriptComponentTree> iterator = component.connections.values().iterator();
        int childId = -1;
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptComponentTree child = iterator.next();
            childId++;

            hashData += childId;
            hashData += getTreeHash(child);
        }

        return Hash.sha1Hex(hashData);
    }

    /**
     * Follows the child that is connected to the current state (active child). This is continued till the last child
     * with an active child is reached.
     *
     * @return an ordered list of active child components
     */
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

    /**
     * A name is for example needed to calculate the hash value.
     *
     * @return the component name
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Connect a state to a child component.
     * @param state
     * @param component
     */
    public void setChildComponent(int state, ScriptComponentTree component) {
        connections.put(state, component);
        component.setParent(this);
    }

    /**
     * If the state is > 0 the component associated with this state is returned.
     *
     * @return the component associated with the current state
     */
    public ScriptComponentTree getActiveChild() {
        int state = getState();
        if (state < 0)
            return null;
        return connections.get(state);
    }

    /**
     * Set the state and notifies the script about it.
     *
     * @param state
     */
    @Override
    public void setState(int state) {
        super.setState(state);
        script.onComponentStateChanged(this, state);
    }

    public ScriptComponentTree getParent() {
        return parent;
    }

    /**
     * Follows the parent links till the root parent is reached, i.e., the component without a parent.
     *
     * @return the number of steps to the root parent
     */
    public int getStepsToRootParent() {
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

    /**
     * Recursive iterator for all child. Starting at the first child all child are recursively listed.
     */
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
