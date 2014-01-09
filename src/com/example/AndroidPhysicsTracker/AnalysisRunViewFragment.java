package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisRunViewFragment extends android.support.v4.app.Fragment {
    private ExperimentPlugin plugin = null;
    private Experiment experiment = null;

    private RunContainerView runContainerView = null;

    public AnalysisRunViewFragment(ExperimentPlugin plugin, Experiment experiment) {
        this.plugin = plugin;
        this.experiment = experiment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analysis_runview_fragment, container, false);

        View experimentRunView = plugin.createExperimentRunView(getActivity(), experiment);

        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();

        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)view.findViewById(
                R.id.experimentRunViewControl);
        runViewControl.setTo(activity.getRunDataModel());

        runContainerView = (RunContainerView)view.findViewById(R.id.experimentRunContainer);
        runContainerView.setTo(experimentRunView, activity.getRunDataModel());
        runContainerView.addMarkerData(experiment.getTagMarkers());

        return view;
    }

    @Override
    public void onDestroyView() {
        runContainerView.release();

        super.onDestroyView();
    }
}
