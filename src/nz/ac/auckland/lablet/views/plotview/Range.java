/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;


public class Range {
    public int min;
    public int max;

    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean equals(Object object) {
        Range otherRange = (Range)object;
        return otherRange.min == min && otherRange.max == max;
    }
}
