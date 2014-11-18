/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.RectF;


/**
 * RectF wrapper that behaves like a "normed" rect with left < right and top < bottom.
 *
 * For example, a rect with left > right can be accessed and modified like a "normed" rect.
 */
class NormRectF {
    final private RectF rect;
    private boolean isXInverted = false;
    private boolean isYInverted = false;

    public NormRectF(RectF rect) {
        this.rect = rect;

        if (rect.left > rect.right)
            isXInverted = true;
        if (rect.top > rect.bottom)
            isYInverted = true;
    }

    public float getLeft() {
        if (isXInverted)
            return rect.right;
        return rect.left;
    }

    public float getRight() {
        if (isXInverted)
            return rect.left;
        return rect.right;
    }

    public float getTop() {
        if (isYInverted)
            return rect.bottom;
        return rect.top;
    }

    public float getBottom() {
        if (isYInverted)
            return rect.top;
        return rect.bottom;
    }

    public void setLeft(float value) {
        if (isXInverted)
            rect.right = value;
        rect.left = value;
    }

    public void setRight(float value) {
        if (isXInverted)
            rect.left = value;
        rect.right = value;
    }

    public void setTop(float value) {
        if (isYInverted)
            rect.bottom = value;
        rect.top = value;
    }

    public void setBottom(float value) {
        if (isYInverted)
            rect.top = value;
        rect.bottom = value;
    }

    public RectF get() {
        return rect;
    }
}
