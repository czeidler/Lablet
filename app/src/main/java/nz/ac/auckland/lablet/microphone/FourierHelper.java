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
import java.nio.channels.Channels;


public class FourierHelper {

    static public File convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                        float stepFactor) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("FrequencyData", ".freq", outputDir);

        convertToFourier(context, audioWavInputStream, windowSize, stepFactor, outputFile);

        return outputFile;
    }

    static public void convertToFourier(Context context, AudioWavInputStream audioWavInputStream, int windowSize,
                                        float stepFactor, File outFile) throws IOException {
        final FourierRenderScript fourierRenderScript = new FourierRenderScript(context);

        final int maxBunchSize = 1024 * 1024;
        final int stepWidth =  (int)(stepFactor * windowSize);
        final int maxSteps = (maxBunchSize - windowSize) / stepWidth + 1;
        // choose the bunch size that big that it fits all the steps
        final int bunchSize =  (maxSteps - 1) * stepWidth + windowSize;
        final float[] buffer = new float[bunchSize];

        final int totalSize = audioWavInputStream.getSize() / AudioWavInputStream.BYTES_PER_SAMPLE;
        final int overlap = windowSize - stepWidth;
        final float[] overlapBuffer = new float[overlap];

        DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        try {
            for (int i = 0; i < totalSize;) {
                final int remainingData = totalSize - i;
                if (remainingData < windowSize)
                    break;
                int nRead = Math.min(remainingData, bunchSize);
                i += nRead - overlap;

                int a = 0;
                if (i > 0) {
                    // copy the part that we already read
                    System.arraycopy(buffer, buffer.length - overlap, overlapBuffer, 0, overlap);
                    System.arraycopy(overlapBuffer, 0, buffer, 0, overlap);
                    a = overlap;
                }
                for (; a < nRead; a++)
                    buffer[a] = audioWavInputStream.readFloatAmplitude();

                final float[] frequencies = fourierRenderScript.renderScriptFFT(buffer, nRead, windowSize, stepFactor);
                for (a = 0; a < frequencies.length; a++)
                    outputStream.writeFloat(frequencies[a]);
            }
        } finally {
            outputStream.close();
        }
    }

    static public class FrequencyFileReader implements AudioFrequencyMapAdapter.DataBackend {
        final RandomAccessFile file;
        final long fileLength;
        final int bunchSize;

        final static int FLOAT_BYTES = 4;

        public FrequencyFileReader(File file, int windowSize) throws FileNotFoundException {
            this.file = new RandomAccessFile(file, "r");
            this.fileLength = file.length();
            this.bunchSize = windowSize / 2;
        }

        private FrequencyFileReader(RandomAccessFile file, long fileLength, int bunchSize) {
            this.file = file;
            this.fileLength = fileLength;
            this.bunchSize = bunchSize;
        }

        @Override
        public void clear() {

        }

        @Override
        public void add(float[] frequencies) {

        }

        @Override
        public int getBunchSize() {
            return bunchSize;
        }

        @Override
        public float[] getBunch(int index) {
            float[] buffer = new float[bunchSize];
            try {
                file.seek(index * FLOAT_BYTES * bunchSize);

                DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(Channels.newInputStream(
                        file.getChannel())));

                for (int i = 0; i < bunchSize; i++)
                    buffer[i] = dataInputStream.readFloat();

                return buffer;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getBunchCount() {
            return (int)(fileLength / FLOAT_BYTES) / bunchSize;
        }

        @Override
        public AudioFrequencyMapAdapter.DataBackend clone() {
            return new FrequencyFileReader(file, fileLength, bunchSize);
        }
    }
}
