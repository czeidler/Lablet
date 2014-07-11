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


public class Region1D {
    private List<Range> ranges = new ArrayList<>();

    public void addRange(int min, int max) {
        if (ranges.size() == 0) {
            ranges.add(new Range(min, max));
        }

        for (int i = 0; i < ranges.size(); i++){
            Range range = ranges.get(i);
            if (min > range.max) {
                // last one?
                if (i < ranges.size() - 1)
                    continue;
                else {
                    ranges.add(new Range(min, max));
                    return;
                }
            }
            if (max < range.min) {
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
