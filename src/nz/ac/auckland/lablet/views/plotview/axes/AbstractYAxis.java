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


abstract public class AbstractYAxis extends AbstractAxis {
    protected float realTop = 10;
    protected float realBottom = 0;

    public AbstractYAxis(Context context) {
        super(context);
    }

    public AbstractYAxis(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Gets the view relativePosition of the axis top.
     *
     * @return axis top
     */
    abstract public float getAxisTopOffset();

    /**
     * Gets the view relativePosition of the axis bottom measured from the view bottom.
     *
     * @return axis bottom
     */
    abstract public float getAxisBottomOffset();

    /*
     * Set the number of relevant digits.
     *
     * For example, a value of 3 and a top of 9999.3324 and a bottom of 9999.3325 would give results in a displayed
     * range of 9999.32400 to 9999.32500.
     *
     * @param digits
     *
    abstract public void setRelevantLabelDigits(int digits);*/

    public void setDataRange(float bottom, float top) {
        realTop = top;
        realBottom = bottom;

        calculateLabels();

        invalidate();
    }

    abstract public float optimalWidthForHeight(float height);

    protected float getAxisLength() {
        return getHeight() - getAxisBottomOffset() - getAxisTopOffset();
    }
}
