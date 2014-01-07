package com.example.AndroidPhysicsTracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisTableGraphDataFragment extends android.support.v4.app.Fragment {
    private ExperimentPlugin plugin = null;
    private Experiment experiment = null;

    public AnalysisTableGraphDataFragment(ExperimentPlugin plugin, Experiment experiment) {
        this.plugin = plugin;
        this.experiment = experiment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.analysis_table_graph_fragment, container, false);

        // marker table view
        TableView tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        tableView.setAdapter(new MarkerDataTableAdapter(experiment.getTagMarkers()));

        // marker graph view
        GraphView2D graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        graphView.setAdapter(new MarkerGraphAdapter(experiment.getTagMarkers()));

        return view;
    }
}
