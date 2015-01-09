/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import nz.ac.auckland.lablet.views.plotview.AbstractPlotPainter;
import nz.ac.auckland.lablet.views.plotview.NormRectF;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


public class MusicPainter extends AbstractPlotPainter {
    class OctaveList extends AbstractList<Tone> {
        List<Tone> octave0 = new ArrayList<>();

        public OctaveList() {
            octave0.add(new Tone(16.352f, "C", 0));
            octave0.add(new Tone(17.324f, "C♯/D♭", 0));
            octave0.add(new Tone(18.354f, "D", 0));
            octave0.add(new Tone(19.445f, "E♭/D♯", 0));
            octave0.add(new Tone(20.602f, "E", 0));
            octave0.add(new Tone(21.827f, "F", 0));
            octave0.add(new Tone(23.125f, "F♯/G♭", 0));
            octave0.add(new Tone(24.500f, "G", 0));
            octave0.add(new Tone(25.957f, "A♭/G♯", 0));
            octave0.add(new Tone(27.500f, "A", 0));
            octave0.add(new Tone(29.135f, "B♭/A♯", 0));
            octave0.add(new Tone(30.868f, "B", 0));
        }

        @Override
        public Tone get(int i) {
            if (i < octave0.size())
                return octave0.get(i);

            int octave = i / octave0.size();
            Tone tone0 = octave0.get(i % octave0.size());
            return new Tone(tone0.frequency * (float) Math.pow(2, octave), tone0.name, octave);
        }

        @Override
        public int size() {
            return octave0.size() * 11;
        }
    }

    class Tone {
        final public float frequency;
        final public String name;
        final public int octave;

        public Tone(float frequency, String name, int octave) {
            this.frequency = frequency;
            this.name = name;
            this.octave = octave;
        }
    }

    OctaveList octaveList = new OctaveList();
    Paint paint = new Paint();

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        RectF range = containerView.getRange();
        NormRectF normRange = new NormRectF(range);

        float leftScreen = containerView.toScreenX(range.left);
        float rightScreen = containerView.toScreenX(range.right);

        float lastDrawnFrequency = -20000;
        float lineHeightExtent = getLineHeightExtent();

        for (Tone tone : octaveList) {
            float frequency = tone.frequency;
            if (normRange.getTop() > frequency)
                continue;
            if (normRange.getBottom() < frequency)
                break;

            float frequencyScreen = containerView.toScreenY(frequency);
            if (Math.abs(frequencyScreen - lastDrawnFrequency) < lineHeightExtent)
                continue;
            lastDrawnFrequency = frequencyScreen;

            String name = tone.name + tone.octave + " (" + Math.round(frequency) + "Hz)";

            canvas.drawLine(leftScreen, frequencyScreen, rightScreen, frequencyScreen, paint);
            canvas.drawText(name, leftScreen, frequencyScreen - paint.descent() - 1, paint);
        }
    }

    private float getLineHeightExtent() {
        return paint.descent() - paint.ascent() + 10;
    }
}
