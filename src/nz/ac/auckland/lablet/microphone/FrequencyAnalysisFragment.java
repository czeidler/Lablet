/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.lablet.ExperimentAnalysis;
import nz.ac.auckland.lablet.ExperimentAnalysisFragment;


public class FrequencyAnalysisFragment extends ExperimentAnalysisFragment {
    public FrequencyAnalysisFragment() {
        super();
    }

    public FrequencyAnalysisFragment(ExperimentAnalysis.AnalysisRef ref) {
        super(ref);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new FrequencyAnalysisView(getActivity(), (FrequencyAnalysis)sensorAnalysis);
    }
}
