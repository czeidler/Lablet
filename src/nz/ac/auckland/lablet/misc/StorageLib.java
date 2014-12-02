/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;


import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Collection of storage related operations.
 *
 * Note: these function may take a significant amount of time depending where the files are located. For example,
 * moving a file on the same storage is faster than moving it somewhere to a different storage because this would
 * involve a copy and a delete operation.
 *
 * TODO: add listener interfaces to monitor copy, move, delete... progress
 */
public class StorageLib {
    /**
     * I file is a directory it deletes it recursively. If it is just a file it just deletes this file.
     * @param file
     * @return false on the first file that can't be deleted
     */
    static public boolean recursiveDeleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String child : children) {
                if (!recursiveDeleteFile(new File(file, child)))
                    return false;
            }
        }
        return file.delete();
    }

    /**
     * Move one single file. Note: if the file is on a different hd this could take a while.
     * @param source
     * @param destination
     * @return false on failure
     */
    static public boolean moveFile(File source, File destination) throws IOException {
        if (source.isDirectory())
            return false;
        // first try to just rename the file
        if (source.renameTo(destination))
            return true;
        // this could have failed because the file is on different storage cards so to a hard copy and then delete it
        copyFile(source, destination);

        return source.delete();
    }

    /**
     * Copies a file (not a directory) from source to destination
     * @param source
     * @param destination
     * @return false on failure
     */
    static public void copyFile(File source, File destination) throws IOException {
        FileInputStream inStream = new FileInputStream(source);
        FileOutputStream outStream = new FileOutputStream(destination);
        FileChannel inChannel = inStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outStream.getChannel());
        inStream.close();
        outStream.close();
    }

    static public void copyFile(File source, File destination, StreamHelper.IProgressListener listener, int reportingStep)
            throws IOException {
        FileInputStream inStream = new FileInputStream(source);
        FileOutputStream outStream = new FileOutputStream(destination);
        StreamHelper.copy(inStream, outStream, listener, reportingStep);
    }
}
