/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentAnalysis;
import nz.ac.aucklanduni.physics.lablet.experiment.ExperimentPlugin;
import nz.ac.aucklanduni.physics.lablet.views.ExperimentRunViewControl;
import nz.ac.aucklanduni.physics.lablet.views.RunContainerView;
import nz.ac.aucklanduni.physics.lablet.views.graph.*;
import nz.ac.aucklanduni.physics.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;

public class AnalysisMixedDataFragment extends android.support.v4.app.Fragment {
    class Layout extends ViewGroup {
        private ExperimentRunViewControl runViewControl = null;
        private RunContainerView runContainerView = null;
        private ViewGroup experimentDataView = null;

        /**
         * After a long time of trying I was not able to create the desired layout using Androids layout classes. This
         * layout class builds the layout manually.
         * @param context
         */
        public Layout(Context context) {
            super(context);

            runViewControl = new ExperimentRunViewControl(context);
            runContainerView = new RunContainerView(context);

            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            experimentDataView = (ViewGroup)inflater.inflate(R.layout.analysis_data_side_bar, null, false);

            addView(runViewControl);
            addView(runContainerView);
            addView(experimentDataView);
        }

        public ExperimentRunViewControl getRunViewControl() {
            return runViewControl;
        }

        public RunContainerView getRunContainerView() {
            return runContainerView;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int width = right -left;
            int height = bottom - top;

            runViewControl.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            int controlHeight = runViewControl.getMeasuredHeight();

            int containerHeight = height - controlHeight;
            runContainerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(containerHeight, MeasureSpec.EXACTLY));
            int containerWidth = runContainerView.getMeasuredWidth();

            // the child's measure methods have to be called with the final sizes, Android ^^...
            experimentDataView.measure(MeasureSpec.makeMeasureSpec(width - containerWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            runViewControl.measure(MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height - containerHeight, MeasureSpec.EXACTLY));

            runContainerView.layout(0, 0, containerWidth, containerHeight);
            runViewControl.layout(0, containerHeight, containerWidth, height);
            experimentDataView.layout(containerWidth, 0, width, height);
        }
    }

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

        Layout view = new Layout(getActivity());
        assert view != null;

        View experimentRunView = plugin.createExperimentRunView(activity, experimentAnalysis.getExperiment());

        ExperimentRunViewControl runViewControl = view.getRunViewControl();
        runViewControl.setTo(experimentAnalysis.getRunDataModel());

        runContainerView = view.getRunContainerView();
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

