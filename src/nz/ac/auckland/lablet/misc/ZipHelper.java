/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

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

    public void addDir(File dir, StreamHelper.IProgressListener listener) throws IOException {
        File rootDir = dir.getParentFile();
        if (rootDir == null)
            rootDir = dir;
        addDir(rootDir, dir.getName(), listener);
    }

    public void addDir(File rootDir, String subDir, StreamHelper.IProgressListener listener) throws IOException {
        File absoluteDir = new File(rootDir, subDir);
        File[] files = absoluteDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (canceled)
                return;

            File file = files[i];
            String subSubFile = subDir + File.separator + file.getName();

            if (file.isDirectory()) {
                addDir(rootDir, subSubFile, listener);
                continue;
            }
            addFile(rootDir, subSubFile, listener);
        }
    }

    public void cancel() throws IOException {
        canceled = true;
        close();
    }

    public void addFile(File rootDir, String file,  StreamHelper.IProgressListener listener) throws IOException {
        File absoluteFile = new File(rootDir, file);

        FileInputStream in = new FileInputStream(absoluteFile.getAbsolutePath());
        try {
            outputStream.putNextEntry(new ZipEntry(file));
            StreamHelper.copy(in, outputStream, listener);
            outputStream.closeEntry();
        } finally {
            in.close();
        }
    }
}
