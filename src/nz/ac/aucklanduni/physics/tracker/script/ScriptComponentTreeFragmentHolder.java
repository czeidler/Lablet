/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;


import nz.ac.aucklanduni.physics.tracker.script.components.ScriptComponentGenericFragment;

abstract public class ScriptComponentTreeFragmentHolder extends ScriptComponentTree {
    protected String title = "";

    public ScriptComponentTreeFragmentHolder(Script script) {
        super(script);
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    abstract public ScriptComponentGenericFragment createFragment();
}


