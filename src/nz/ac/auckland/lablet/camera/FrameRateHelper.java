/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import java.util.ArrayList;
import java.util.List;


class FrameRateHelper {
    static public List<Integer> getPossibleAnalysisFrameRates(int maxFrameRate) {
        // sanitize the frame rate a bit
        if (maxFrameRate >= 28)
            maxFrameRate = 30;

        List<Integer> frameRateList = new ArrayList<>();
        frameRateList.add(1);
        for (int i = 2; i < maxFrameRate; i++) {
            if (maxFrameRate % i == 0)
                frameRateList.add(i);
        }
        frameRateList.add(maxFrameRate);
        return frameRateList;
    }

    /**
     * Returns the best possible analysis frame rate given a desired target frame rate.
     *
     * @param maxFrameRate the max possible frame rate
     * @param targetFrameRate the desired frame rate
     * @return a analysis frame rate that is closed to the target frame rate
     */
    static public int getBestPossibleAnalysisFrameRate(int maxFrameRate, int targetFrameRate) {
        List<Integer> possibleAnalysisFrameRates = getPossibleAnalysisFrameRates(maxFrameRate);

        int bestMatchingFrameRate = maxFrameRate;
        for (Integer frameRate : possibleAnalysisFrameRates) {
            if (Math.abs(bestMatchingFrameRate - targetFrameRate) > Math.abs(frameRate - targetFrameRate))
                bestMatchingFrameRate = frameRate;
        }

        return bestMatchingFrameRate;
    }
}
