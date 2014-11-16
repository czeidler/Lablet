/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.experiment.Unit;
import nz.ac.auckland.lablet.views.FrameContainerView;
import nz.ac.auckland.lablet.views.FrameDataSeekBar;
import nz.ac.auckland.lablet.views.graph.*;
import nz.ac.auckland.lablet.views.plotview.IPlotPainter;
import nz.ac.auckland.lablet.views.plotview.LinearFitPainter;
import nz.ac.auckland.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;


class MotionAnalysisFragmentView extends FrameLayout {
    private FrameContainerView runContainerView;
    private TableView tableView = null;
    private GraphView2D graphView = null;
    private Spinner graphSpinner = null;
    final private List<GraphSpinnerEntry> graphSpinnerEntryList = new ArrayList<>();

    private class GraphSpinnerEntry {
        private String name;
        private MarkerTimeGraphAdapter markerGraphAdapter;
        private boolean fit = false;

        public GraphSpinnerEntry(String name, MarkerTimeGraphAdapter adapter) {
            this.name = name;
            this.markerGraphAdapter = adapter;
        }

        public GraphSpinnerEntry(String name, MarkerTimeGraphAdapter adapter, boolean fit) {
            this.name = name;
            this.markerGraphAdapter = adapter;
            this.fit = fit;
        }

        public String toString() {
            return name;
        }

        public MarkerTimeGraphAdapter getMarkerGraphAdapter() {
            return markerGraphAdapter;
        }

        public boolean getFit() {
            return fit;
        }
    }

    public MotionAnalysisFragmentView(Context context, MotionAnalysis sensorAnalysis) {
        super(context);

        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View mainView = inflater.inflate(R.layout.motion_analysis, this, true);
        assert mainView != null;

        final DrawerLayout drawerLayout = (DrawerLayout)mainView.findViewById(R.id.drawer_layout);

        final Button drawerButton = (Button)mainView.findViewById(R.id.drawerButton);
        drawerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        final FrameLayout leftDrawer = (FrameLayout)mainView.findViewById(R.id.left_drawer);

        runContainerView = (FrameContainerView)mainView.findViewById(R.id.frameContainerView);
        final FrameDataSeekBar runViewControl = (FrameDataSeekBar)mainView.findViewById(R.id.frameDataSeekBar);

        final ViewGroup experimentDataView = (ViewGroup)inflater.inflate(R.layout.motion_analysis_data_side_bar, null, false);
        leftDrawer.addView(experimentDataView);

        // marker graph view
        graphView = (GraphView2D)mainView.findViewById(R.id.tagMarkerGraphView);
        assert graphView != null;
        tableView = (TableView)mainView.findViewById(R.id.tagMarkerTableView);
        assert tableView != null;

        final View sensorAnalysisView = new CameraExperimentFrameView(context, sensorAnalysis);
        if (sensorAnalysisView == null)
            return;

        runViewControl.setTo(sensorAnalysis.getFrameDataModel());

        runContainerView.setTo(sensorAnalysisView, sensorAnalysis);
        runContainerView.addTagMarkerData(sensorAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(sensorAnalysis.getXYCalibrationMarkers());
        runContainerView.addOriginData(sensorAnalysis.getOriginMarkers(), sensorAnalysis.getCalibrationXY());

        final Unit xUnit = sensorAnalysis.getXUnit();
        final Unit yUnit = sensorAnalysis.getYUnit();
        final Unit tUnit = sensorAnalysis.getTUnit();

        // marker table view
        final ITimeData timeData = sensorAnalysis.getTimeData();
        final MarkerDataTableAdapter adapter = new MarkerDataTableAdapter(sensorAnalysis.getTagMarkers());
        adapter.addColumn(new RunIdDataTableColumn());
        adapter.addColumn(new TimeDataTableColumn(tUnit, timeData));
        adapter.addColumn(new XPositionDataTableColumn(xUnit));
        adapter.addColumn(new YPositionDataTableColumn(yUnit));
        tableView.setAdapter(adapter);

        MarkerDataModel markerDataModel = sensorAnalysis.getTagMarkers();
        ITimeData timeCalibration = sensorAnalysis.getTimeData();
        // graph spinner
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Position Data", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "Position Data",
                new XPositionMarkerGraphAxis(xUnit, sensorAnalysis.getXMinRangeGetter()),
                new YPositionMarkerGraphAxis(yUnit, sensorAnalysis.getYMinRangeGetter()))));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("x-Velocity", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "x-Velocity", new TimeMarkerGraphAxis(tUnit),
                new XSpeedMarkerGraphAxis(xUnit, tUnit)), true));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("y-Velocity", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "y-Velocity", new TimeMarkerGraphAxis(tUnit),
                new YSpeedMarkerGraphAxis(yUnit, tUnit)), true));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("time vs x-Position", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "time vs x-Position", new TimeMarkerGraphAxis(tUnit),
                new XPositionMarkerGraphAxis(xUnit, sensorAnalysis.getXMinRangeGetter()))));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("time vs y-Position", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "time vs y-Position", new TimeMarkerGraphAxis(tUnit),
                new YPositionMarkerGraphAxis(yUnit, sensorAnalysis.getYMinRangeGetter()))));

        graphSpinner = (Spinner)mainView.findViewById(R.id.graphSpinner);
        graphSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                graphSpinnerEntryList));
        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                GraphSpinnerEntry entry = graphSpinnerEntryList.get(i);
                MarkerGraphAdapter adapter = entry.getMarkerGraphAdapter();
                LinearFitPainter fitPainter = null;
                if (entry.getFit()) {
                    fitPainter = new LinearFitPainter();
                    fitPainter.setDataAdapter(adapter);
                }
                graphView.setAdapter(adapter);
                graphView.setFitPainter(fitPainter);
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
