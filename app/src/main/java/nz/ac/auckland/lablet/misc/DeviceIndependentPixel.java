/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.misc;

import android.content.Context;
import android.view.View;


public class DeviceIndependentPixel {
    final private Context context;

    public DeviceIndependentPixel(Context context) {
        this.context = context;
    }

    static public int toPixel(float densityIndependentPixel, View view) {
        return toPixel(densityIndependentPixel, view.getContext());
    }

    static public int toPixel(float densityIndependentPixel, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return Math.round(densityIndependentPixel * scale);
    }

    public int toPixel(float densityIndependentPixel) {
        return toPixel(densityIndependentPixel, context);
    }
}
