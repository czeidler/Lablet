/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class AudioWavInputStream extends InputStream implements Closeable {
    private InputStream inputStream;
    private int channelCount;
    private int sampleRate;
    private int audioDataSize;

    public AudioWavInputStream(File file) throws IOException {
        inputStream = new BufferedInputStream(new FileInputStream(file));
        readHeader();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return inputStream.read(buffer, offset, length);
    }

    @Override
    public void close() throws IOException {
        super.close();
        inputStream.close();
    }

    public int getSize() {
        return audioDataSize;
    }

    private void readHeader() throws IOException {
        byte[] buffer32 = new byte[4];
        if (inputStream.read(buffer32) != 4 || !(new String(buffer32)).equals("RIFF"))
            throw new IOException();
        // data size
        if (inputStream.read(buffer32) != 4)
            throw new IOException();
        if (inputStream.read(buffer32) != 4 || !(new String(buffer32)).equals("WAVE"))
            throw new IOException();
        if (inputStream.read(buffer32) != 4 || !(new String(buffer32)).equals("fmt "))
            throw new IOException();
        if (inputStream.read() != 16)
            throw new IOException();
        if (inputStream.read(buffer32) != 4 || buffer32[3] != 1)
            throw new IOException();
        inputStream.read();
        // channel count
        channelCount = inputStream.read();
        inputStream.read();
        // sample rate
        if (inputStream.read(buffer32) != 4)
            throw new IOException();
        ByteBuffer buffer = ByteBuffer.wrap(buffer32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        sampleRate = buffer.getInt();
        // byte rate
        if (inputStream.read(buffer32) != 4)
            throw new IOException();
        // some other data...
        if (inputStream.read(buffer32) != 4)
            throw new IOException();
        if (inputStream.read(buffer32) != 4 || !(new String(buffer32)).equals("data"))
            throw new IOException();
        // audio data size
        if (inputStream.read(buffer32) != 4)
            throw new IOException();
        buffer = ByteBuffer.wrap(buffer32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        audioDataSize = buffer.getInt();
    }

    static public float[] toAmplitudeData(byte[] buffer, int bufferSize) {
        final int bytesPerSample = 2;
        float frame[] = new float[bufferSize / bytesPerSample];

        int frameIndex = 0;
        for (int index = 0; index < bufferSize - bytesPerSample + 1; index += bytesPerSample) {
            float sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                int v = buffer[index + b];
                if (b < bytesPerSample - 1 || bytesPerSample == 1) {
                    v &= 0xFF;
                }
                sample += v << (b * 8);
            }
            frame[frameIndex] = sample;
            frameIndex++;
        }
        return frame;
    }
}
