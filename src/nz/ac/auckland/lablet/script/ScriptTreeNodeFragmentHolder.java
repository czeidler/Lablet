/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;


import nz.ac.auckland.lablet.script.components.ScriptComponentGenericFragment;

/**
 * Abstract base class for a component that is visualized by a Fragment.
 */
abstract public class ScriptTreeNodeFragmentHolder extends ScriptTreeNode {
    protected String title = "";

    public ScriptTreeNodeFragmentHolder(Script script) {
        super(script);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    /**
     * Creates the fragment that displays the component.
     *
     * @return the fragment of the component.
     */
    abstract public ScriptComponentGenericFragment createFragment();
}


