package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisTableGraphDataFragment extends android.support.v4.app.Fragment {
    private ExperimentPlugin plugin = null;
    private ExperimentAnalysis experimentAnalysis = null;

    private TableView tableView = null;
    private GraphView2D graphView = null;

    public AnalysisTableGraphDataFragment(ExperimentPlugin plugin, ExperimentAnalysis experimentAnalysis) {
        this.plugin = plugin;
        this.experimentAnalysis = experimentAnalysis;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analysis_table_graph_fragment, container, false);

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        tableView.setAdapter(new MarkerDataTableAdapter(experimentAnalysis.getTagMarkers()));

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        graphView.setAdapter(new MarkerGraphAdapter(experimentAnalysis.getTagMarkers()));

        return view;
    }

    @Override
    public void onDestroyView() {
        tableView.setAdapter(null);
        graphView.setAdapter(null);

        super.onDestroyView();
    }
}
