/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import nz.ac.aucklanduni.physics.tracker.*;
import nz.ac.aucklanduni.physics.tracker.views.RunContainerView;
import nz.ac.aucklanduni.physics.tracker.views.graph.*;
import nz.ac.aucklanduni.physics.tracker.views.table.*;

import java.util.ArrayList;
import java.util.List;

public class AnalysisMixedDataFragment extends android.support.v4.app.Fragment {
    private RunContainerView runContainerView = null;
    private TableView tableView = null;
    private GraphView2D graphView = null;
    private Spinner graphSpinner = null;
    private List<GraphSpinnerEntry> graphSpinnerEntryList = new ArrayList<GraphSpinnerEntry>();

    private class GraphSpinnerEntry {
        private String name;
        private MarkerGraphAdapter markerGraphAdapter;

        public GraphSpinnerEntry(String name, MarkerGraphAdapter adapter) {
            this.name = name;
            this.markerGraphAdapter = adapter;
        }

        public String toString() {
            return name;
        }

        public MarkerGraphAdapter getMarkerGraphAdapter() {
            return markerGraphAdapter;
        }
    }

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
        ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(experimentAnalysis.getTagMarkers(),
                experimentAnalysis);
        adapter.addColumn(new RunIdDataTableColumn());
        adapter.addColumn(new TimeDataTableColumn());
        adapter.addColumn(new XPositionDataTableColumn());
        adapter.addColumn(new YPositionDataTableColumn());
        tableView.setAdapter(adapter);

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);

        // graph spinner
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Position Data", new MarkerGraphAdapter(experimentAnalysis,
                "Position Data", new XPositionMarkerGraphAxis(), new YPositionMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("x-Velocity", new MarkerGraphAdapter(experimentAnalysis,
                "x-Velocity", new TimeMarkerGraphAxis(), new XSpeedMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("y-Velocity", new MarkerGraphAdapter(experimentAnalysis,
                "y-Velocity", new TimeMarkerGraphAxis(), new YSpeedMarkerGraphAxis())));

        graphSpinner = (Spinner)view.findViewById(R.id.graphSpinner);
        graphSpinner.setAdapter(new ArrayAdapter<GraphSpinnerEntry>(getActivity(), android.R.layout.simple_spinner_item,
                graphSpinnerEntryList));
        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GraphSpinnerEntry entry = graphSpinnerEntryList.get(i);
                graphView.setAdapter(entry.getMarkerGraphAdapter());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                graphView.setAdapter(null);
            }
        });
        graphSpinner.setSelection(0);

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

