/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.os.Bundle;
import nz.ac.auckland.lablet.experiment.ISensorAnalysis;

import java.util.List;


/**
 * Fragment that displays a run view container and a tag data graph/table.
 */
public class ExperimentAnalysisFragment extends android.support.v4.app.Fragment {
    private ISensorAnalysis sensorAnalysis;

    public ExperimentAnalysisFragment() {
        super();
    }

    public ExperimentAnalysisFragment(int position) {
        super();

        Bundle args = new Bundle();
        args.putInt("sensorAnalysisIndex", position);
        setArguments(args);
    }


    private ISensorAnalysis findExperimentFromArguments(Activity activity) {
        int position = getArguments().getInt("sensorAnalysisIndex", 0);

        ExperimentAnalyserActivity experimentActivity = (ExperimentAnalyserActivity) activity;
        List<ISensorAnalysis> list = experimentActivity.getCurrentAnalysisRun();
        return list.get(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        sensorAnalysis = findExperimentFromArguments(activity);
    }
}
