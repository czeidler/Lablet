/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipHelper {
    private ZipOutputStream outputStream;
    private boolean canceled = false;

    public ZipHelper(String outputPath) throws IOException {
        init(new File(outputPath));
    }

    public ZipHelper(File outputFile) throws IOException {
        init(outputFile);
    }

    private void init(File outputFile) throws IOException {
        if (outputFile.exists())
            outputFile.delete();

        outputFile.createNewFile();
        outputStream = new ZipOutputStream(new FileOutputStream(outputFile));
    }

    public void close() throws IOException {
        outputStream.close();
    }

    public void addDir(File directory, StreamHelper.IProgressListener listener) throws IOException {
        File[] files = directory.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (canceled)
                return;

            if (files[i].isDirectory()) {
                addDir(files[i], listener);
                continue;
            }
            addFile(files[i], listener);
        }
    }

    public void cancel() throws IOException {
        canceled = true;
        close();
    }

    public void addFile(File file, StreamHelper.IProgressListener listener) throws IOException {
        FileInputStream in = new FileInputStream(file.getAbsolutePath());
        try {
            outputStream.putNextEntry(new ZipEntry(file.getAbsolutePath()));
            StreamHelper.copy(in, outputStream, listener);
            outputStream.closeEntry();
        } finally {
            in.close();
        }
    }
}
