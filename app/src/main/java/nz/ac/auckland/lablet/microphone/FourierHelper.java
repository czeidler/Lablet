/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import nz.ac.auckland.lablet.misc.AudioWavInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class FourierHelper {

    static public File convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                        float stepFactor) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("FrequencyData", "freq", outputDir);

        convertToFourier(context, audioWavInputStream, windowSize, stepFactor, outputFile);

        return outputFile;
    }

    static public void convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                        float stepFactor, File outFile) throws IOException {
        final FourierRenderScript fourierRenderScript = new FourierRenderScript(context);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(audioWavInputStream);

        final int maxBunchSize = 1024 * 1024;
        final int bunchSize = (maxBunchSize / windowSize) * windowSize;
        final byte[] buffer = new byte[bunchSize];

        final int totalSize = audioWavInputStream.getSize();
        final int sizeToRead = (totalSize / windowSize) * windowSize;

        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        try {
            for (int i = 0; i < sizeToRead; ) {
                int nRead = sizeToRead - i;
                if (nRead > bunchSize)
                    nRead = bunchSize;
                i += nRead;

                for (int a = 0; a < nRead; a++)
                    buffer[a] = (byte) bufferedInputStream.read();

                float[] amplitudes = AudioWavInputStream.toAmplitudeData(buffer, nRead);
                float[] frequencies = fourierRenderScript.renderScriptFFT(amplitudes, windowSize, stepFactor);
                for (int a = 0; a < frequencies.length; a++)
                    outputStream.writeFloat(a);
            }
        } finally {
            outputStream.close();
        }
    }

    static public class FrequencyFileReader implements AudioFrequencyMapAdapter.DataBackend {
        RandomAccessFile file;
        int windowSize;

        public FrequencyFileReader(File file, int windowSize) throws FileNotFoundException {
            this.file = new RandomAccessFile(file, "r");
            this.windowSize = windowSize;
        }

        private FrequencyFileReader(RandomAccessFile file, int windowSize) {
            this.file = file;
            this.windowSize = windowSize;
        }

        @Override
        public void clear() {

        }

        @Override
        public void add(float[] frequencies) {

        }

        @Override
        public int getBunchSize() {
            return windowSize;
        }

        @Override
        public float[] getBunch(int index) {
            float[] buffer = new float[windowSize];
            try {
                file.seek(Float.SIZE * index);

                ByteBuffer byteBuffer = ByteBuffer.allocate(Float.SIZE * windowSize);
                FileChannel inChannel = file.getChannel();
                inChannel.read(byteBuffer);

                byteBuffer.rewind();
                byteBuffer.asFloatBuffer().get(buffer);
                return buffer;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getBunchCount() {
            try {
                return (int)(file.length() / Float.SIZE) / windowSize;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public AudioFrequencyMapAdapter.DataBackend clone() {
            return new FrequencyFileReader(file, windowSize);
        }
    }
}
