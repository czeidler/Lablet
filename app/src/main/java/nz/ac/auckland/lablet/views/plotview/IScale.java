/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


/**
 * Interface to describe a length scale.
 */
public interface IScale {
    /**
     * Scales a real value to a value on the scale.
     *
     * For example, a log scale would returns log(realValue).
     *
     * @param realValue the value that should be scaled
     * @return the scaled value
     */
    float scale(float realValue);
}

