/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


public class Log10Scale implements IScale {
    public float scale(float realValue) {
        if (realValue <= 0)
            return 0;
        return (float)Math.log10(realValue);
    }
}
