/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import edu.emory.mathcs.jtransforms.dct.FloatDCT_1D;

import java.util.Arrays;


public class Fourier {
    static private void hammingWindow(float[] samples) {
        for (int i = 0; i < samples.length; i++)
            samples[i] *= (0.54f - 0.46f * Math.cos(2 * Math.PI * i / (samples.length - 1)));
    }

    static public float[] transform(float[] in) {
        return transform(in, 0, in.length);
    }

    static public float[] transform(float[] in, int offset, int length) {
        final float trafo[] = Arrays.copyOfRange(in, offset, offset + length);
        return transformInternal(trafo);
    }

    static public float[] transform(float[] prevAmplitudes, float[] amplitudes) {
        final int length = amplitudes.length;
        final int half = length / 2;
        final float trafo[] = new float[length];
        System.arraycopy(prevAmplitudes, half, trafo, 0, half);
        System.arraycopy(amplitudes, 0, trafo, half, half);
        return transformInternal(trafo);
    }

    static private float[] transformInternal(float[] trafo) {
        final FloatDCT_1D dct = new FloatDCT_1D(trafo.length);
        // in place window
        hammingWindow(trafo);

        // in place transform: timeData becomes frequency data
        dct.forward(trafo, false);

        return trafo;
    }
}
