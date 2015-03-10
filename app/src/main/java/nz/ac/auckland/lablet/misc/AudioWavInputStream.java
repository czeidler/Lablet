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
    private int byteRate;
    private int audioDataSize;
    final static public int BYTES_PER_SAMPLE = 2;
    private byte[] convertByteBuffer = new byte[BYTES_PER_SAMPLE];
    private float[] convertFloatBuffer = new float[1];

    public AudioWavInputStream(File file) throws IOException {
        inputStream = new FileInputStream(file);
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

    public int getChannelCount() {
        return channelCount;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getDurationMilliSeconds() {
        return (int)(1000f / getByteRate() * audioDataSize);
    }

    public int getByteRate() {
        return byteRate;
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
        buffer = ByteBuffer.wrap(buffer32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byteRate = buffer.getInt();
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
        float outBuffer[] = new float[bufferSize / BYTES_PER_SAMPLE];
        return toAmplitudeData(buffer, bufferSize, outBuffer);
    }

    static public float[] toAmplitudeData(byte[] buffer, int bufferSize, float[] outBuffer) {
        int frameIndex = 0;
        for (int index = 0; index < bufferSize - BYTES_PER_SAMPLE + 1; index += BYTES_PER_SAMPLE) {
            float sample = 0;
            for (int b = 0; b < BYTES_PER_SAMPLE; b++) {
                int v = buffer[index + b];
                if (b < BYTES_PER_SAMPLE - 1 || BYTES_PER_SAMPLE == 1) {
                    v &= 0xFF;
                }
                sample += v << (b * 8);
            }
            outBuffer[frameIndex] = sample;
            frameIndex++;
        }
        return outBuffer;
    }

    /**
     * This method allows to read amplitude float data from a stream.
     *
     * AudioWavInputStream is not buffered this method allows to read from a buffered stream.
     *
     * @param inputStream the input stream to read the data from
     * @param convertByteBuffer must have at least size 2
     * @param convertFloatBuffer must have at least size 1
     * @return
     * @throws IOException
     */
    static public float readFloatAmplitude(InputStream inputStream, byte[] convertByteBuffer,
                                           float[] convertFloatBuffer) throws IOException {
        convertByteBuffer[0] = (byte)inputStream.read();
        convertByteBuffer[1] = (byte)inputStream.read();

        return toAmplitudeData(convertByteBuffer, convertByteBuffer.length, convertFloatBuffer)[0];
    }
}
