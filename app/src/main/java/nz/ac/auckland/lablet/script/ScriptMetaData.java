/*
 * Copyright 2013-2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

import java.io.File;


public class ScriptMetaData {
    final public File file;
    final public float interfaceVersion;

    public ScriptMetaData(File file, float interfaceVersion) {
        this.file = file;
        this.interfaceVersion = interfaceVersion;
    }

    public String getScriptFileName() {
        String name = file.getName();
        return name.substring(0, name.length() - 4);
    }

    public String getLabel() {
        return getScriptFileName();
    }

    public String toString() {
        return getLabel();
    }
}
