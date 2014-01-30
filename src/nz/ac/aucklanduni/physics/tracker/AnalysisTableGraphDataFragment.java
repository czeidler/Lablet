/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AnalysisTableGraphDataFragment extends android.support.v4.app.Fragment {
    private TableView tableView = null;
    private GraphView2D graphView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();
        ExperimentAnalysis experimentAnalysis = activity.getExperimentAnalysis();

        View view = inflater.inflate(R.layout.analysis_table_graph_fragment, container, false);

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        tableView.setAdapter(new MarkerDataTableAdapter(experimentAnalysis.getTagMarkers(),
                experimentAnalysis));

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
