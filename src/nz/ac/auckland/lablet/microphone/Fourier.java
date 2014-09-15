/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import edu.emory.mathcs.jtransforms.dct.FloatDCT_1D;
import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;
import nz.ac.auckland.lablet.R;

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

    static public float[] transform(float[] data, int windowSize, float stepFactor) {
        final int stepWidth =  (int)(stepFactor * windowSize);
        final int nSteps = (data.length - windowSize) / stepWidth + 1;
        final int outputSize = nSteps * windowSize / 2;
        final float[] out = new float[outputSize];

        for (int i = 0; i < nSteps; i++) {
            final int start = i * stepWidth;

            final float[] trafo = transform(data, start, windowSize);

            final int outPosition = windowSize / 2 * i;
            System.arraycopy(trafo, 0, out, outPosition, windowSize / 2);
        }

        return out;
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
            out[(i - 1) / 2] = (float) Math.sqrt(Math.pow(trafo[i], 2) + Math.pow(trafo[i - 1], 2));
        }

        return out;
    }

}

class FourierRenderScript {
    private Context context;
    private RenderScript renderScript;
    private ScriptC_fft script;

    public FourierRenderScript(Context context) {
        this.context = context;
        renderScript = RenderScript.create(context);
        script = new ScriptC_fft(renderScript, context.getResources(), R.raw.fft);
    }

    public float[] renderScriptFFT(float[] data, int windowSize, float stepFactor) {
        final int stepWidth =  (int)(stepFactor * windowSize);
        final int nSteps = (data.length - windowSize) / stepWidth + 1;
        final int outputSize = nSteps * windowSize / 2;

        Allocation dataAllocation = Allocation.createSized(renderScript, Element.F32(renderScript), data.length);
        dataAllocation.copyFrom(data);
        Allocation outAllocation = Allocation.createSized(renderScript, Element.F32(renderScript), outputSize);

        final int[] inStartValues = new int[nSteps];
        for (int i = 0; i < nSteps; i++)
            inStartValues[i] = i * stepWidth;

        Allocation inAllocation = Allocation.createSized(renderScript, Element.I32(renderScript), nSteps);
        inAllocation.copyFrom(inStartValues);

        script.set_gWindowSize(windowSize);
        script.bind_gData(dataAllocation);
        script.bind_gOutput(outAllocation);
        script.forEach_root(inAllocation, inAllocation);

        final float[] out = new float[outputSize];
        outAllocation.copyTo(out);

        renderScript.finish();

        return out;
    }
}
