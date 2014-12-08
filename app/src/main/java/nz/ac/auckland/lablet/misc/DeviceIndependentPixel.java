/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import android.view.View;


public class DeviceIndependentPixel {
    final private View view;

    public DeviceIndependentPixel(View view) {
        this.view = view;
    }

    static public int toPixel(float densityIndependentPixel, View view) {
        final float scale = view.getResources().getDisplayMetrics().density;
        return Math.round(densityIndependentPixel * scale);
    }

    public int toPixel(float densityIndependentPixel) {
        return toPixel(densityIndependentPixel, view);
    }
}
