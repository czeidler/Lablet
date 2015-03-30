/*
 * Copyright 2013-2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

import nz.ac.auckland.lablet.ScriptHomeActivity;
import nz.ac.auckland.lablet.misc.StorageLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class ScriptMetaData {
    final public File file;
    private float interfaceVersion;
    private String title = "";
    private String loadingError = "";

    public ScriptMetaData(File file) {
        this.file = file;
    }

    public float getInterfaceVersion() {
        return interfaceVersion;
    }

    public void setInterfaceVersion(float interfaceVersion) {
        this.interfaceVersion = interfaceVersion;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLoadingError() {
        return loadingError;
    }

    public void setLoadingError(String loadingError) {
        this.loadingError = loadingError;
    }

    public String getScriptFileName() {
        return StorageLib.removeExtension(file.getName());
    }

    public String getTitle() {
        if (!title.equals(""))
            return title;
        return getScriptFileName();
    }

    public String toString() {
        return getTitle();
    }

    public String readRemote() {
        File parent = file.getParentFile();
        File remoteFile = new File(parent, getScriptFileName() + "." + ScriptHomeActivity.REMOTE_TYPE);

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(remoteFile));
            return reader.readLine();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}
