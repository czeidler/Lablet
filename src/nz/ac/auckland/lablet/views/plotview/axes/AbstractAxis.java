/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.List;


class AxisSettings {
    final private float fullTickSize = 8;
    final private float shortTickSize = 6;
    final private float spacing = 4;
    final protected float minSpacing = 20;

    public float getSpacing() {
        return spacing;
    }

    public float getFullTickSize() {
        return fullTickSize;
    }

    public float getShortTickSize() {
        return shortTickSize;
    }

    public float getMinSpacing() {
        return minSpacing;
    }
}

abstract public class AbstractAxis extends ViewGroup {
    protected String label = "";
    protected String unit = "";
    protected LabelPartitioner labelPartitioner = new LabelPartitionerLinear();

    protected List<LabelPartitioner.LabelEntry> labels;

    public AbstractAxis(Context context) {
        super(context);

        setWillNotDraw(false);
    }

    public AbstractAxis(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWillNotDraw(false);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setLabelPartitioner(LabelPartitioner labelPartitioner) {
        this.labelPartitioner = labelPartitioner;
    }
}
