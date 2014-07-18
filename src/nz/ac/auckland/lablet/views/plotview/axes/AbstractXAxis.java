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


abstract public class AbstractXAxis extends AbstractAxis {
    protected float realLeft = 0;
    protected float realRight = 10;

    public AbstractXAxis(Context context) {
        super(context);
    }

    public AbstractXAxis(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    abstract public float getAxisLeftOffset();
    abstract public float getAxisRightOffset();

    public void setDataRange(float left, float right) {
        realLeft = left;
        realRight = right;

        calculateLabels();
    }

    abstract public float optimalHeight();

    abstract protected void calculateLabels();
}
