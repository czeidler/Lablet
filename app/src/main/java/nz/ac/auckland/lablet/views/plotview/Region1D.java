/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import java.util.ArrayList;
import java.util.List;


/**
 * A class to describe a set of 1D ranges.
 *
 * If possible a new range is merged with the existing ranges in the region. Thus there are never two overlapping or
 * directly adjacent ranges in a region. The ranges in a region are ordered. For example, a region could look like this:
 * 2-5, 9-20, 22-23
 */
public class Region1D {
    private List<Range> ranges = new ArrayList<>();

    public Region1D() {

    }

    public Region1D(int min, int max) {
        addRange(min, max);
    }

    public Region1D(Range range) {
        ranges.add(range);
    }

    public Region1D(Region1D other) {
        for (Range range : other.ranges)
            ranges.add(new Range(range));
    }

    public int getMin() {
        if (ranges.size() == 0)
            return -1;
        return ranges.get(0).min;
    }

    public int getMax() {
        if (ranges.size() == 0)
            return -1;
        return ranges.get(ranges.size() - 1).max;
    }

    public void clear() {
        ranges.clear();
    }

    public int getSize() {
        return ranges.size();
    }

    public void addRange(Range range) {
        addRange(range.min, range.max);
    }

    public void addRange(int min, int max) {
        if (ranges.size() == 0) {
            ranges.add(new Range(min, max));
            return;
        }

        for (int i = 0; i < ranges.size(); i++){
            Range range = ranges.get(i);
            if (min > range.max + 1) {
                // last one?
                if (i < ranges.size() - 1)
                    continue;
                else {
                    ranges.add(new Range(min, max));
                    return;
                }

            }
            if (max < range.min - 1) {
                ranges.add(i, new Range(min, max));
                return;
            }

            // overlap
            if (min < range.min) {
                range.min = min;
            }

            if (max > range.max) {
                if (ranges.size() == i + 1) {
                    range.max = max;
                    return;
                }
                range.max = max;

                // merge following ranges if necessary
                for (int a = i + 1; a < ranges.size(); a++) {
                    Range nextRange = ranges.get(a);
                    if (max < nextRange.min)
                        return;

                    if (max < nextRange.max) {
                        range.max = nextRange.max;
                        ranges.remove(a);
                        return;
                    }

                    ranges.remove(a);
                    a--;
                }

            }
            return;
        }
    }

    public List<Range> getRanges() {
        return ranges;
    }

    @Override
    public boolean equals(Object object) {
        Region1D region1D = (Region1D)object;

        return equals(region1D.ranges);
    }

    public boolean equals(List<Range> otherRanges) {
        if (otherRanges.size() != ranges.size())
            return false;

        for (int i = 0; i < otherRanges.size(); i++) {
            Range otherRange = otherRanges.get(i);
            if (!otherRange.equals(ranges.get(i)))
                return false;
        }
        return true;
    }

}
