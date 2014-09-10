/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import edu.emory.mathcs.jtransforms.dct.FloatDCT_1D;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

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
        // in place window
        hammingWindow(trafo);
/*
        float out[] = new float[2 * trafo.length];
        for (int i = 0; i < trafo.length; i++) {
            out[i * 2] = trafo[i];
            out[i * 2 + 1] = 0;
        }
        //System.arraycopy(trafo, 0, out, 0, trafo.length);
        final FloatFFT_1D fft = new FloatFFT_1D(trafo.length);

        fft.complexForward(out);
        trafo = new float[trafo.length / 2];
        for (int i = 1; i < out.length / 2; i += 2) {
            trafo[(i - 1) / 2] = (float)Math.sqrt(Math.pow(out[i], 2) + Math.pow(out[i - 1], 2));
        }

        return trafo;
*/

        final FloatDCT_1D dct = new FloatDCT_1D(trafo.length);

        // in place transform: timeData becomes frequency data
        dct.forward(trafo, false);

        float[] out = new float[trafo.length / 2];
        for (int i = 1; i < trafo.length; i += 2) {
            out[(i - 1) / 2] = (float)Math.sqrt(Math.pow(trafo[i], 2) + Math.pow(trafo[i - 1], 2));
        }

        return out;
    }
}
