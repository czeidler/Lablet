/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;


public class IntegralView extends FrameLayout {

    public IntegralView(Context context) {
        super(context);

        setBackgroundColor(Color.BLACK);
        
        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.accelerometer_integral, null, false);
        addView(view);

    }
}
