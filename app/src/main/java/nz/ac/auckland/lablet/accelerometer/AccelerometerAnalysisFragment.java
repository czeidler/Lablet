/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.lablet.ExperimentAnalysisFragment;


public class AccelerometerAnalysisFragment extends ExperimentAnalysisFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new AccelerometerAnalysisFragmentView(getActivity(), getSensorAnalysis());
    }

    private AccelerometerAnalysis getSensorAnalysis() {
        return (AccelerometerAnalysis)sensorAnalysis;
    }
}
