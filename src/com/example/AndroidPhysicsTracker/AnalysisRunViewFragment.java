package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisRunViewFragment extends android.support.v4.app.Fragment {
    private RunContainerView runContainerView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();
        ExperimentPlugin plugin = activity.getExperimentPlugin();
        ExperimentAnalysis experimentAnalysis = activity.getExperimentAnalysis();

        View view = inflater.inflate(R.layout.analysis_runview_fragment, container, false);

        View experimentRunView = plugin.createExperimentRunView(activity, experimentAnalysis.getExperiment());

        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)view.findViewById(
                R.id.experimentRunViewControl);
        runViewControl.setTo(experimentAnalysis.getRunDataModel());

        runContainerView = (RunContainerView)view.findViewById(R.id.experimentRunContainer);
        runContainerView.setTo(experimentRunView, experimentAnalysis.getRunDataModel());
        runContainerView.addTagMarkerData(experimentAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(experimentAnalysis.getXYCalibrationMarkers());

        return view;
    }

    @Override
    public void onDestroyView() {
        runContainerView.release();

        super.onDestroyView();
    }
}
