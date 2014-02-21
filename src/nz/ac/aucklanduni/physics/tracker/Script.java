/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;


class ScriptComponent {
    final static public int SCRIPT_STATE_INACTIVE = -2;
    final static public int SCRIPT_STATE_ONGOING = -1;
    final static public int SCRIPT_STATE_DONE = 0;

    private Bundle stateData = null;
    private ScriptComponent parent = null;
    private int state = SCRIPT_STATE_INACTIVE;
    private Map<Integer, ScriptComponent> connections = new HashMap<Integer, ScriptComponent>();


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

    private void setParent(ScriptComponent parent) {
        this.parent = parent;
    }
}

public class Script {
    private ScriptComponent root = null;
    private ScriptComponent currentComponent = null;

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

    public ScriptComponent getCurrentComponent() {
        return currentComponent;
    }

    private void setCurrentComponent(ScriptComponent component) {
        if (currentComponent != null)
            currentComponent.setState(ScriptComponent.SCRIPT_STATE_INACTIVE);
        currentComponent = component;
        currentComponent.setState(ScriptComponent.SCRIPT_STATE_ONGOING);

        //notifyCurrentComponentChanged(currentComponent);
    }

    private boolean cancelCurrent() {
        if (currentComponent == null)
            return false;

        ScriptComponent parent = currentComponent.getParent();
        if (parent == null)
            return false;

        currentComponent.setStateData(null);
        setCurrentComponent(parent);
        return true;
    }

    private boolean backToParent() {
        if (currentComponent == null)
            return false;

        ScriptComponent parent = currentComponent.getParent();
        if (parent == null)
            return false;

        setCurrentComponent(parent);
        return true;
    }

    private boolean next() {
        if (currentComponent == null)
            return false;

        ScriptComponent next = currentComponent.getNext();
        if (next == null)
            return false;

        setCurrentComponent(next);
        return true;
    }
}

