/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.views.FrameContainerView;
import nz.ac.auckland.lablet.views.FrameDataSeekBar;
import nz.ac.auckland.lablet.views.graph.*;
import nz.ac.auckland.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;


class CameraAnalysisFragmentView extends FrameLayout {
    class Layout extends ViewGroup {
        private FrameDataSeekBar runViewControl = null;
        private FrameContainerView sensorContainerView = null;
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
            sensorContainerView = new FrameContainerView(context);

            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            experimentDataView = (ViewGroup)inflater.inflate(R.layout.analysis_data_side_bar, null, false);

            addView(runViewControl);
            addView(sensorContainerView);
            addView(experimentDataView);
        }

        public FrameDataSeekBar getRunViewControl() {
            return runViewControl;
        }

        public FrameContainerView getSensorContainerView() {
            return sensorContainerView;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int width = right -left;
            int height = bottom - top;

            runViewControl.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
            int controlHeight = runViewControl.getMeasuredHeight();

            int containerHeight = height - controlHeight;
            sensorContainerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(containerHeight, MeasureSpec.EXACTLY));
            int containerWidth = sensorContainerView.getMeasuredWidth();

            // the child's measure methods have to be called with the final sizes, Android ^^...
            experimentDataView.measure(MeasureSpec.makeMeasureSpec(width - containerWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            runViewControl.measure(MeasureSpec.makeMeasureSpec(containerWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height - containerHeight, MeasureSpec.EXACTLY));

            sensorContainerView.layout(0, 0, containerWidth, containerHeight);
            runViewControl.layout(0, containerHeight, containerWidth, height);
            experimentDataView.layout(containerWidth, 0, width, height);
        }
    }

    private FrameContainerView runContainerView = null;
    private TableView tableView = null;
    private GraphView2D graphView = null;
    private Spinner graphSpinner = null;
    final private List<GraphSpinnerEntry> graphSpinnerEntryList = new ArrayList<>();

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


    public CameraAnalysisFragmentView(Context context, VideoAnalysis sensorAnalysis) {
        super(context);

        final Layout mainView = new Layout(getContext());
        assert mainView != null;
        addView(mainView);

        runContainerView = mainView.getSensorContainerView();
        // marker graph view
        graphView = (GraphView2D)mainView.findViewById(R.id.tagMarkerGraphView);
        assert graphView != null;
        tableView = (TableView)mainView.findViewById(R.id.tagMarkerTableView);
        assert tableView != null;

        final View sensorAnalysisView = new CameraExperimentFrameView(context, sensorAnalysis.getSensorData());
        if (sensorAnalysisView == null)
            return;

        final FrameDataSeekBar runViewControl = mainView.getRunViewControl();
        runViewControl.setTo(sensorAnalysis.getFrameDataModel());

        runContainerView.setTo(sensorAnalysisView, sensorAnalysis);
        runContainerView.addTagMarkerData(sensorAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(sensorAnalysis.getXYCalibrationMarkers());
        runContainerView.addOriginData(sensorAnalysis.getOriginMarkers(), sensorAnalysis.getCalibration());

        // marker table view
        final ColumnMarkerDataTableAdapter adapter = new ColumnMarkerDataTableAdapter(sensorAnalysis.getTagMarkers(),
                sensorAnalysis);
        adapter.addColumn(new RunIdDataTableColumn());
        adapter.addColumn(new TimeDataTableColumn());
        adapter.addColumn(new XPositionDataTableColumn());
        adapter.addColumn(new YPositionDataTableColumn());
        tableView.setAdapter(adapter);

        // graph spinner
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Position Data", new MarkerGraphAdapter(sensorAnalysis,
                "Position Data", new XPositionMarkerGraphAxis(), new YPositionMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("x-Velocity", new MarkerGraphAdapter(sensorAnalysis,
                "x-Velocity", new TimeMarkerGraphAxis(), new XSpeedMarkerGraphAxis())));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("y-Velocity", new MarkerGraphAdapter(sensorAnalysis,
                "y-Velocity", new TimeMarkerGraphAxis(), new YSpeedMarkerGraphAxis())));

        graphSpinner = (Spinner)mainView.findViewById(R.id.graphSpinner);
        graphSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
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
    }

    @Override
    public void finalize() {
        runContainerView.release();
        tableView.setAdapter(null);
        graphView.setAdapter(null);
    }
}
