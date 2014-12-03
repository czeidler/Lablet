/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

/**
 * Interface to create script component by name.
 */
public interface IScriptComponentFactory {
    public ScriptTreeNode create(String componentName, Script script);
}
