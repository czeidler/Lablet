/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.experiment.Unit;
import nz.ac.auckland.lablet.views.FrameContainerView;
import nz.ac.auckland.lablet.views.FrameDataSeekBar;
import nz.ac.auckland.lablet.views.graph.*;
import nz.ac.auckland.lablet.views.plotview.LinearFitPainter;
import nz.ac.auckland.lablet.views.table.*;

import java.util.ArrayList;
import java.util.List;


class MotionAnalysisFragmentView extends FrameLayout {
    private MarkerDataTableAdapter markerDataTableAdapter;
    final private FrameContainerView runContainerView;
    final private GraphView2D graphView;
    final private Spinner graphSpinner;
    final private DrawerLayout drawerLayout;
    final private FrameLayout drawer;
    final private TableView tableView;
    final private List<GraphSpinnerEntry> graphSpinnerEntryList = new ArrayList<>();
    private boolean releaseAdaptersWhenDrawerClosed = false;

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

        public void release() {
            markerGraphAdapter.release();
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

        drawerLayout = (DrawerLayout)mainView.findViewById(R.id.drawer_layout);

        final Button drawerButton = (Button)mainView.findViewById(R.id.drawerButton);
        drawerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.RIGHT);
            }
        });

        drawer = (FrameLayout)mainView.findViewById(R.id.left_drawer);

        runContainerView = (FrameContainerView)mainView.findViewById(R.id.frameContainerView);
        final FrameDataSeekBar runViewControl = (FrameDataSeekBar)mainView.findViewById(R.id.frameDataSeekBar);

        final ViewGroup experimentDataView = (ViewGroup)inflater.inflate(R.layout.motion_analysis_data_side_bar, null, false);
        drawer.addView(experimentDataView);

        graphSpinner = (Spinner)mainView.findViewById(R.id.graphSpinner);

        // marker graph view
        graphView = (GraphView2D)mainView.findViewById(R.id.tagMarkerGraphView);
        assert graphView != null;

        tableView = (TableView)mainView.findViewById(R.id.tableView);
        assert tableView != null;
        tableView.setColumnWeights(1f, 1.8f, 1.4f, 1.4f);

        final View sensorAnalysisView = new CameraExperimentFrameView(context, sensorAnalysis);
        if (sensorAnalysisView == null)
            return;

        runViewControl.setTo(sensorAnalysis.getFrameDataModel(), sensorAnalysis.getTimeData());

        runContainerView.setTo(sensorAnalysisView, sensorAnalysis);
        runContainerView.addTagMarkerData(sensorAnalysis.getTagMarkers());
        runContainerView.addXYCalibrationData(sensorAnalysis.getXYCalibrationMarkers());
        runContainerView.addOriginData(sensorAnalysis.getOriginMarkers(), sensorAnalysis.getCalibrationXY());

        final Unit xUnit = sensorAnalysis.getXUnit();
        final Unit yUnit = sensorAnalysis.getYUnit();
        final Unit tUnit = sensorAnalysis.getTUnit();

        // marker table view
        final ITimeData timeData = sensorAnalysis.getTimeData();
        markerDataTableAdapter = new MarkerDataTableAdapter(sensorAnalysis.getTagMarkers());
        markerDataTableAdapter.addColumn(new RunIdDataTableColumn());
        markerDataTableAdapter.addColumn(new TimeDataTableColumn(tUnit, timeData));
        markerDataTableAdapter.addColumn(new XPositionDataTableColumn(xUnit));
        markerDataTableAdapter.addColumn(new YPositionDataTableColumn(yUnit));

        MarkerDataModel markerDataModel = sensorAnalysis.getTagMarkers();
        ITimeData timeCalibration = sensorAnalysis.getTimeData();
        if (timeCalibration.getSize() > 400)
            releaseAdaptersWhenDrawerClosed = true;

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
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Time vs x-Position", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "Time vs x-Position", new TimeMarkerGraphAxis(tUnit),
                new XPositionMarkerGraphAxis(xUnit, sensorAnalysis.getXMinRangeGetter()))));
        graphSpinnerEntryList.add(new GraphSpinnerEntry("Time vs y-Position", new MarkerTimeGraphAdapter(markerDataModel,
                timeCalibration, "Time vs y-Position", new TimeMarkerGraphAxis(tUnit),
                new YPositionMarkerGraphAxis(yUnit, sensorAnalysis.getYMinRangeGetter()))));

        graphSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                graphSpinnerEntryList));
        graphSpinner.setSelection(0);
        graphSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectGraphAdapter(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                graphView.setAdapter(null);
            }
        });

        // setup the load/ unloading of the view data adapters
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (releaseAdaptersWhenDrawerClosed) {
                    selectGraphAdapter(graphSpinner.getSelectedItemPosition());
                    tableView.setAdapter(markerDataTableAdapter);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (releaseAdaptersWhenDrawerClosed) {
                    tableView.setAdapter((ITableAdapter)null);
                    selectGraphAdapter(-1);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        if (!releaseAdaptersWhenDrawerClosed) {
            tableView.setAdapter(markerDataTableAdapter);
            selectGraphAdapter(graphSpinner.getSelectedItemPosition());
        }
    }

    private void selectGraphAdapter(int i) {
        if (i < 0) {
            graphView.setFitPainter(null);
            graphView.setAdapter(null);
            return;
        }
        if (releaseAdaptersWhenDrawerClosed && !drawerLayout.isDrawerVisible(drawer))
            return;
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
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public void release() {
        markerDataTableAdapter.release();
        runContainerView.release();
        tableView.setAdapter((ITableAdapter)null);
        graphView.setAdapter(null);
        graphSpinner.setAdapter(null);
        for (GraphSpinnerEntry entry : graphSpinnerEntryList)
            entry.release();
        graphSpinnerEntryList.clear();
    }
}
