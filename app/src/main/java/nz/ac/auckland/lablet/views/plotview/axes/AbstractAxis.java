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
import nz.ac.auckland.lablet.misc.Unit;
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

abstract public class AbstractAxis extends ViewGroup implements Unit.IListener {
    protected String title = "";
    protected Unit unit = new Unit("");
    protected Unit.Prefix usedPrefix = null;
    protected Paint titlePaint = new Paint();
    protected Paint axisPaint = new Paint();

    protected LabelPartitioner labelPartitioner = new LabelPartitionerLinear();

    private List<LabelPartitioner.LabelEntry> labels;

    public AbstractAxis(Context context) {
        super(context);

        init();
    }

    public AbstractAxis(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        unit.removeListener(this);
    }

    private void init() {
        setWillNotDraw(false);

        titlePaint.setColor(PlotView.Defaults.PEN_COLOR);
        titlePaint.setStyle(Paint.Style.STROKE);

        axisPaint.setColor(Color.WHITE);
        axisPaint.setStrokeWidth(1f);
        axisPaint.setStyle(Paint.Style.STROKE);
    }

    public void setTitle(String title) {
        this.title = title;
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
        if (labels == null)
            labels = calculateLabels();
        return labels;
    }

    public void setUnit(Unit unit) {
        this.unit.removeListener(this);
        this.unit = unit;
        this.unit.addListener(this);
    }

    @Override
    public void onBaseExponentChanged() {
        requestLayout();
    }

    abstract protected List<LabelPartitioner.LabelEntry> calculateLabels();

    protected void invalidateLabels() {
        labels = null;
    }

    public void setLabelPartitioner(LabelPartitioner labelPartitioner) {
        this.labelPartitioner = labelPartitioner;
        invalidateLabels();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed)
            invalidateLabels();
    }

    protected void determineUsedPrefix(float value0, float value1) {
        usedPrefix = null;
        if (unit == null)
            return;
        float diff = Math.abs(value0 - value1);
        float diffOrderValue = LabelPartitionerHelper.getOrderValue(diff);
        int diffExponent = (int)Math.log10(diffOrderValue);

        int totalExponent = unit.getBaseExponent() + diffExponent;
        List<Unit.Prefix> prefixes = unit.getPrefixes();
        if (diffExponent > 4) {
            for (int i = prefixes.size() - 1; i >= 0; i--) {
                Unit.Prefix currentPrefix = prefixes.get(i);
                if (currentPrefix.exponent <= totalExponent) {
                    usedPrefix = currentPrefix;
                    break;
                }
            }
        } else if (diffExponent < -3) {
            for (int i = 0; i < prefixes.size(); i++) {
                Unit.Prefix currentPrefix = prefixes.get(i);
                if (currentPrefix.exponent <= totalExponent) {
                    usedPrefix = currentPrefix;
                    break;
                }
            }
        }
    }
}
