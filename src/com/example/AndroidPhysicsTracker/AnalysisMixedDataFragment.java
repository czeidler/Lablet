package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisMixedDataFragment extends android.support.v4.app.Fragment {
    private ExperimentPlugin plugin = null;
    private ExperimentAnalysis experimentAnalysis = null;

    private RunContainerView runContainerView = null;
    private TableView tableView = null;
    private GraphView2D graphView = null;

    public AnalysisMixedDataFragment(ExperimentPlugin plugin, ExperimentAnalysis experimentAnalysis) {
        this.plugin = plugin;
        this.experimentAnalysis = experimentAnalysis;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analysis_runview_table_graph_fragment, container, false);

        View experimentRunView = plugin.createExperimentRunView(getActivity(), experimentAnalysis.getExperiment());

        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();

        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)view.findViewById(
            R.id.experimentRunViewControl);
        runViewControl.setTo(experimentAnalysis.getRunDataModel());

        runContainerView = (RunContainerView)view.findViewById(R.id.experimentRunContainer);
        runContainerView.setTo(experimentRunView, experimentAnalysis.getRunDataModel());
        runContainerView.addMarkerData(experimentAnalysis.getTagMarkers());

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        tableView.setAdapter(new MarkerDataTableAdapter(experimentAnalysis.getTagMarkers(),
                experimentAnalysis.getExperiment()));

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        graphView.setAdapter(new MarkerGraphAdapter(experimentAnalysis.getTagMarkers()));
        return view;
    }

    @Override
    public void onDestroyView() {
        runContainerView.release();
        tableView.setAdapter(null);
        graphView.setAdapter(null);

        super.onDestroyView();
    }
}

