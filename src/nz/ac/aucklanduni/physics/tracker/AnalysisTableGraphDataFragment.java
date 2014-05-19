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
import nz.ac.aucklanduni.physics.tracker.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.tracker.views.graph.*;
import nz.ac.aucklanduni.physics.tracker.views.table.*;


public class AnalysisTableGraphDataFragment extends android.support.v4.app.Fragment {
    private TableView tableView = null;
    private GraphView2D graphView = null;
    private GraphView2D xSpeedGraphView = null;
    private GraphView2D ySpeedGraphView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();
        ExperimentAnalysis experimentAnalysis = activity.getExperimentAnalysis();

        View view = inflater.inflate(R.layout.analysis_table_graph_fragment, container, false);
        assert view != null;

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        assert tableView != null;
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(experimentAnalysis.getTagMarkers(),
                experimentAnalysis);
        adapter.addColumn(new RunIdDataTableColumn());
        adapter.addColumn(new TimeDataTableColumn());
        adapter.addColumn(new XPositionDataTableColumn());
        adapter.addColumn(new YPositionDataTableColumn());
        tableView.setAdapter(adapter);

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        assert graphView != null;
        graphView.setAdapter(MarkerGraphAdapter.createPositionAdapter(experimentAnalysis, "Position Data"));

        // velocity graph
        ySpeedGraphView = (GraphView2D)view.findViewById(R.id.yVelocityGraphView);
        assert ySpeedGraphView != null;
        ySpeedGraphView.setAdapter(MarkerGraphAdapter.createYSpeedAdapter(experimentAnalysis, "y-Velocity"));

        // velocity graph
        xSpeedGraphView = (GraphView2D)view.findViewById(R.id.xVelocityGraphView);
        assert xSpeedGraphView != null;
        xSpeedGraphView.setAdapter(MarkerGraphAdapter.createXSpeedAdapter(experimentAnalysis, "x-Velocity"));
        return view;
    }

    @Override
    public void onDestroyView() {
        tableView.setAdapter(null);
        graphView.setAdapter(null);
        ySpeedGraphView.setAdapter(null);

        super.onDestroyView();
    }
}
