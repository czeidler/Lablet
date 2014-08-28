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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.IExperimentPlugin;

import java.util.List;


/**
 * Fragment that displays a run view container and a tag data graph/table.
 */
public class ExperimentAnalysisFragment extends android.support.v4.app.Fragment {
    private ExperimentDataActivity.AnalysisEntry analysisEntry;

    public ExperimentAnalysisFragment() {
        super();
    }

    public ExperimentAnalysisFragment(int position) {
        super();

        Bundle args = new Bundle();
        args.putInt("analysisRunId", position);
        setArguments(args);
    }


    private ExperimentDataActivity.AnalysisEntry findExperimentFromArguments(Activity activity) {
        int position = getArguments().getInt("analysisRunId", 0);

        ExperimentAnalyserActivity experimentActivity = (ExperimentAnalyserActivity) activity;
        List<ExperimentDataActivity.AnalysisEntry> list = experimentActivity.getCurrentAnalysisRun();
        return list.get(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        analysisEntry = findExperimentFromArguments(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity) getActivity();
        final IExperimentPlugin plugin = analysisEntry.plugin;
        final SensorAnalysis sensorAnalysis = analysisEntry.analysis;

        return plugin.getAnalysis().createSensorAnalysisView(activity, sensorAnalysis);
    }
}
