/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamHelper {
    public interface IProgressListener {
        public void onNewProgress(int totalProgress);
    }

    static public int BUFFER_SIZE = 1024;

    static public void copyBytes(InputStream inputStream, OutputStream outputStream, int size) throws IOException {
        int bufferLength = BUFFER_SIZE;
        byte[] buf = new byte[bufferLength];
        int bytesRead = 0;
        while (bytesRead < size) {
            int requestedBunchSize = Math.min(size - bytesRead, bufferLength);
            int read = inputStream.read(buf, 0, requestedBunchSize);
            bytesRead += read;
            outputStream.write(buf, 0, read);
        }
    }

    static public void copy(InputStream inputStream, OutputStream outputStream, IProgressListener listener)
            throws IOException {
        copy(inputStream, outputStream, listener, 32 * 1024);
    }

    static public void copy(InputStream inputStream, OutputStream outputStream, IProgressListener listener,
                            int reportingStep)
            throws IOException {
        int totalProgress = 0;
        int lastReportedProgress = 0;

        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);

            if (listener != null) {
                totalProgress += length;
                if (totalProgress - lastReportedProgress > reportingStep) {
                    lastReportedProgress = totalProgress;
                    listener.onNewProgress(totalProgress);
                }
            }
        }
    }

    static public void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        copy(inputStream, outputStream, null);
    }
}
