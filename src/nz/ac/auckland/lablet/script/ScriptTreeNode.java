package nz.ac.auckland.lablet.script;

import nz.ac.auckland.lablet.misc.Hash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Abstract base class for a component that has child components. A tree component can have multiple child. Each child
 * is assigned to a component state. For example, a script can be continued depending on the state of the current
 * component. A {@link ScriptTreeNode} has a link to its parent (if it exists).
 */
abstract public class ScriptTreeNode extends ScriptComponent implements Iterable<ScriptTreeNode> {
    private Script script = null;
    private ScriptTreeNode parent = null;
    private Map<Integer, ScriptTreeNode> connections = new HashMap<Integer, ScriptTreeNode>();

    public ScriptTreeNode(Script script) {
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
    public java.util.Iterator<ScriptTreeNode> iterator() {
        return new Iterator(this);
    }

    /**
     * Starting from the given component a hash is calculated that identifies the whole tree. The tree hash is dependent
     * from the child and their child components.
     * @param component
     * @return the hash of the whole tree
     */
    static public String getTreeHash(ScriptTreeNode component) {
        String hashData = component.getName();
        java.util.Iterator<ScriptTreeNode> iterator = component.connections.values().iterator();
        int childId = -1;
        while (true) {
            if (!iterator.hasNext())
                break;
            ScriptTreeNode child = iterator.next();
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
    public List<ScriptTreeNode> getActiveChain() {
        List<ScriptTreeNode> list = new ArrayList<ScriptTreeNode>();
        list.add(this);
        ScriptTreeNode current = this;
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
    public void setChildComponent(int state, ScriptTreeNode component) {
        connections.put(state, component);
        component.setParent(this);
    }

    /**
     * If the state is > 0 the component associated with this state is returned.
     *
     * @return the component associated with the current state
     */
    public ScriptTreeNode getActiveChild() {
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

    public ScriptTreeNode getParent() {
        return parent;
    }

    /**
     * Follows the parent links till the root parent is reached, i.e., the component without a parent.
     *
     * @return the number of steps to the root parent
     */
    public int getStepsToRootParent() {
        int stepsToRoot = 1;
        ScriptTreeNode currentParent = parent;
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

    private void setParent(ScriptTreeNode parent) {
        this.parent = parent;
    }

    /**
     * Recursive iterator for all child. Starting at the first child all child are recursively listed.
     */
    static private class Iterator implements java.util.Iterator<ScriptTreeNode> {
        private ScriptTreeNode currentComponent;
        private java.util.Iterator<ScriptTreeNode> currentComponentIterator;
        private java.util.Iterator<ScriptTreeNode> childIterator;

        Iterator(ScriptTreeNode root) {
            currentComponent = root;
            currentComponentIterator = currentComponent.connections.values().iterator();
        }

        @Override
        public ScriptTreeNode next() {
            if (childIterator == null) {
                ScriptTreeNode child = currentComponentIterator.next();
                childIterator = child.iterator();
                return child;
            } else {
                ScriptTreeNode child = childIterator.next();
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
