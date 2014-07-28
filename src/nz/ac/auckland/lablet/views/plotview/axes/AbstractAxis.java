/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview.axes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.ViewGroup;
import nz.ac.auckland.lablet.views.plotview.PlotView;

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
    protected String title = "";
    protected String unit = "";
    protected Paint titlePaint = new Paint();
    protected Paint axisPaint = new Paint();

    protected LabelPartitioner labelPartitioner = new LabelPartitionerLinear();

    protected List<LabelPartitioner.LabelEntry> labels;

    public AbstractAxis(Context context) {
        super(context);

        init();
    }

    public AbstractAxis(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        setWillNotDraw(false);

        titlePaint.setColor(PlotView.DEFAULT_PEN_COLOR);
        titlePaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.WHITE);
        axisPaint.setStrokeWidth(2);
        axisPaint.setStyle(Paint.Style.STROKE);
    }

    public void setTitle(String label) {
        this.title = label;
    }

    public String getTitle() {
        return title;
    }

    public Paint getTitlePaint() {
        return titlePaint;
    }

    public Paint getAxisPaint() {
        return axisPaint;
    }

    public List<LabelPartitioner.LabelEntry> getLabels() {
        return labels;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setLabelPartitioner(LabelPartitioner labelPartitioner) {
        this.labelPartitioner = labelPartitioner;
    }
}
