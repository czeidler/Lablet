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

public class AnalysisMixedDataFragment extends android.support.v4.app.Fragment {
    private RunContainerView runContainerView = null;
    private TableView tableView = null;
    private GraphView2D graphView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();
        ExperimentPlugin plugin = activity.getExperimentPlugin();
        ExperimentAnalysis experimentAnalysis = activity.getExperimentAnalysis();

        View view = inflater.inflate(R.layout.analysis_runview_table_graph_fragment, container, false);
        assert view != null;

        View experimentRunView = plugin.createExperimentRunView(activity, experimentAnalysis.getExperiment());

        ExperimentRunViewControl runViewControl = (ExperimentRunViewControl)view.findViewById(
            R.id.experimentRunViewControl);
        assert runViewControl != null;
        runViewControl.setTo(experimentAnalysis.getRunDataModel());

        runContainerView = (RunContainerView)view.findViewById(R.id.experimentRunContainer);
        runContainerView.setTo(experimentRunView, experimentAnalysis);
        runContainerView.addTagMarkerData(experimentAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(experimentAnalysis.getXYCalibrationMarkers());
        runContainerView.addOriginData(experimentAnalysis.getOriginMarkers(), experimentAnalysis.getCalibration());

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        assert tableView != null;
        tableView.setAdapter(new MarkerDataTableAdapter(experimentAnalysis.getTagMarkers(),
                experimentAnalysis));

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);
        graphView.setAdapter(new MarkerGraphAdapter(experimentAnalysis, "Position Data", new XPositionMarkerGraphAxis(),
                new YPositionMarkerGraphAxis()));
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

