/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import nz.ac.auckland.lablet.experiment.SensorAnalysis;
import nz.ac.auckland.lablet.experiment.IExperimentPlugin;
import nz.ac.auckland.lablet.views.FrameDataSeekBar;
import nz.ac.auckland.lablet.views.FrameContainerView;
import nz.ac.auckland.lablet.views.graph.*;
import nz.ac.auckland.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Fragment that displays a run view container and a tag data graph/table.
 */
public class AnalysisMixedDataFragment extends android.support.v4.app.Fragment {
    class Layout extends ViewGroup {
        private FrameDataSeekBar runViewControl = null;
        private FrameContainerView runContainerView = null;
        private ViewGroup experimentDataView = null;

        /**
         * After a long time of trying I was not able to create the desired layout using Androids layout classes. This
         * layout class builds the layout manually.
         *
         * @param context the fragment context
         */
        public Layout(Context context) {
            super(context);

            runViewControl = new FrameDataSeekBar(context);
            runContainerView = new FrameContainerView(context);

            LayoutInflater inflater = (LayoutInflater)context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            experimentDataView = (ViewGroup)inflater.inflate(R.layout.analysis_data_side_bar, null, false);

            addView(runViewControl);
            addView(runContainerView);
            addView(experimentDataView);
        }

        public FrameDataSeekBar getRunViewControl() {
            return runViewControl;
        }

        public FrameContainerView getRunContainerView() {
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

    private FrameContainerView runContainerView = null;
    private TableView tableView = null;
    private GraphView2D graphView = null;
    private Spinner graphSpinner = null;
    final private List<GraphSpinnerEntry> graphSpinnerEntryList = new ArrayList<>();
    private ExperimentDataActivity.AnalysisEntry analysisEntry;

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

    public AnalysisMixedDataFragment() {
        super();
    }

    public AnalysisMixedDataFragment(int position) {
        super();

        Bundle args = new Bundle();
        args.putInt("analysisRunId", position);
        setArguments(args);
    }

    private ExperimentDataActivity.AnalysisEntry findExperimentFromArguments(Activity activity) {
        int position = getArguments().getInt("analysisRunId", 0);

        ExperimentAnalyserActivity experimentActivity = (ExperimentAnalyserActivity)activity;
        List<ExperimentDataActivity.AnalysisEntry> list = experimentActivity.getCurrentAnalysisRun();
        return list.get(position);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        analysisEntry = findExperimentFromArguments(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ExperimentAnalyserActivity activity = (ExperimentAnalyserActivity)getActivity();
        final IExperimentPlugin plugin = analysisEntry.plugin;
        final SensorAnalysis sensorAnalysis = analysisEntry.analysis;

        final Layout view = new Layout(getActivity());
        assert view != null;

        final View experimentRunView = plugin.createSensorAnalysisView(activity, sensorAnalysis.getSensorData());

        final FrameDataSeekBar runViewControl = view.getRunViewControl();
        runViewControl.setTo(sensorAnalysis.getFrameDataModel());

        runContainerView = view.getRunContainerView();
        runContainerView.setTo(experimentRunView, sensorAnalysis);
        runContainerView.addTagMarkerData(sensorAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(sensorAnalysis.getXYCalibrationMarkers());
        runContainerView.addOriginData(sensorAnalysis.getOriginMarkers(), sensorAnalysis.getCalibration());

        // marker table view
        tableView = (TableView)view.findViewById(R.id.tagMarkerTableView);
        assert tableView != null;
        final ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(sensorAnalysis.getTagMarkers(),
                sensorAnalysis);
        adapter.addColumn(new RunIdDataTableColumn());
        adapter.addColumn(new TimeDataTableColumn());
        adapter.addColumn(new XPositionDataTableColumn());
        adapter.addColumn(new YPositionDataTableColumn());
        tableView.setAdapter(adapter);

        // marker graph view
        graphView = (GraphView2D)view.findViewById(R.id.tagMarkerGraphView);

        // graph spinner
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Position Data", new MarkerGraphAdapter(sensorAnalysis,
                "Position Data", new XPositionMarkerGraphAxis(), new YPositionMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("x-Velocity", new MarkerGraphAdapter(sensorAnalysis,
                "x-Velocity", new TimeMarkerGraphAxis(), new XSpeedMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("y-Velocity", new MarkerGraphAdapter(sensorAnalysis,
                "y-Velocity", new TimeMarkerGraphAxis(), new YSpeedMarkerGraphAxis())));

        graphSpinner = (Spinner)view.findViewById(R.id.graphSpinner);
        graphSpinner.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
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

