/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import junit.framework.TestCase;
import nz.ac.auckland.lablet.views.plotview.Range;
import nz.ac.auckland.lablet.views.plotview.Region1D;

import java.util.ArrayList;
import java.util.List;


public class Region1DTest extends TestCase {
    public void testRanges() {
        Region1D region1D = new Region1D();

        region1D.addRange(7, 9);
        region1D.addRange(15, 20);
        region1D.addRange(0, 2);
        // 0-2, 7-9, 15-20
        List<Range> expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 2));
        expectedRanges.add(new Range(7, 9));
        expectedRanges.add(new Range(15, 20));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(4, 5);
        // 0-2, 4-5, 7-9, 15-20
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 2));
        expectedRanges.add(new Range(4, 5));
        expectedRanges.add(new Range(7, 9));
        expectedRanges.add(new Range(15, 20));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(15, 16);
        // 0-2, 4-5, 7-9, 15-20
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(8, 10);
        // 0-2, 4-5, 7-10, 15-20
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 2));
        expectedRanges.add(new Range(4, 5));
        expectedRanges.add(new Range(7, 10));
        expectedRanges.add(new Range(15, 20));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(14, 15);
        // 0-2, 4-5, 7-10, 14-20
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 2));
        expectedRanges.add(new Range(4, 5));
        expectedRanges.add(new Range(7, 10));
        expectedRanges.add(new Range(14, 20));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(1, 4);
        // 0-5, 7-10, 14-20
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 5));
        expectedRanges.add(new Range(7, 10));
        expectedRanges.add(new Range(14, 20));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(0, 30);
        // 0-30
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 30));
        assertTrue(region1D.equals(expectedRanges));

        region1D.addRange(31, 40);
        // 0-40
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(0, 40));
        assertTrue(region1D.equals(expectedRanges));

        region1D.clear();

        region1D.addRange(20, 30);
        region1D.addRange(10, 19);
        // 10-30
        expectedRanges = new ArrayList();
        expectedRanges.add(new Range(10, 30));
        assertTrue(region1D.equals(expectedRanges));
    }

}
