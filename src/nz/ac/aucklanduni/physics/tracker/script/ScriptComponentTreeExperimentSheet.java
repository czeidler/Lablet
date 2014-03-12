/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import nz.ac.aucklanduni.physics.tracker.*;
import nz.ac.aucklanduni.physics.tracker.views.graph.*;

import java.util.HashMap;
import java.util.Map;


class GraphViewHolder extends ScriptComponentViewHolder {
    private ScriptComponentTreeExperimentSheet experimentSheet;
    private ScriptComponentExperiment experiment;
    private MarkerGraphAdapter adapter;
    private String xAxisContentId = "x-position";
    private String yAxisContentId = "y-position";
    private String title = "Position Data";

    public GraphViewHolder(ScriptComponentTreeExperimentSheet experimentSheet, ScriptComponentExperiment experiment) {
        this.experimentSheet = experimentSheet;
        this.experiment = experiment;
        setState(ScriptComponentTree.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context, android.support.v4.app.Fragment parentFragment) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.script_component_graph_view, null);
        assert view != null;

        GraphView2D graphView2D = (GraphView2D)view.findViewById(R.id.graphView);
        assert graphView2D != null;

        ExperimentAnalysis experimentAnalysis = experimentSheet.getExperimentAnalysis(context, experiment);
        if (experimentAnalysis != null) {
            MarkerGraphAxis xAxis = createAxis(xAxisContentId);
            if (xAxis == null)
                xAxis = new XPositionMarkerGraphAxis();
            MarkerGraphAxis yAxis = createAxis(yAxisContentId);
            if (yAxis == null)
                yAxis = new XPositionMarkerGraphAxis();

            adapter = new MarkerGraphAdapter(experimentAnalysis, title, xAxis, yAxis);
            graphView2D.setAdapter(adapter);
        }
        return view;
    }

    public boolean setXAxisContent(String axis) {
        if (createAxis(axis) == null)
            return false;
        xAxisContentId = axis;
        return true;
    }

    public boolean setYAxisContent(String axis) {
        if (createAxis(axis) == null)
            return false;
        yAxisContentId = axis;
        return true;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private MarkerGraphAxis createAxis(String id) {
        if (id.equalsIgnoreCase("x-position"))
            return new XPositionMarkerGraphAxis();
        if (id.equalsIgnoreCase("y-position"))
            return new YPositionMarkerGraphAxis();
        if (id.equalsIgnoreCase("x-speed"))
            return new XSpeedMarkerGraphAxis();
        if (id.equalsIgnoreCase("y-speed"))
            return new YSpeedMarkerGraphAxis();
        if (id.equalsIgnoreCase("time"))
            return new TimeMarkerGraphAxis();
        if (id.equalsIgnoreCase("time-speed"))
            return new SpeedTimeMarkerGraphAxis();
        return null;
    }

    public MarkerGraphAdapter getAdapter() {
        return adapter;
    }

    @Override
    public boolean initCheck() {
        if (experiment == null) {
            lastErrorMessage = "no experiment data";
            return false;
        }
        return true;
    }
}

public class ScriptComponentTreeExperimentSheet extends ScriptComponentTreeSheet {
    private Map<String, ExperimentAnalysis> experimentAnalysisMap = new HashMap<String, ExperimentAnalysis>();

    public ScriptComponentTreeExperimentSheet(Script script) {
        super(script);
    }

    public GraphViewHolder addGraph(ScriptComponentExperiment experiment, SheetGroupLayout parent) {
        GraphViewHolder item = new GraphViewHolder(this, experiment);
        addItemViewHolder(item, parent);
        return item;
    }

    public void addPositionGraph(ScriptComponentExperiment experiment, SheetGroupLayout parent) {
        GraphViewHolder item = new GraphViewHolder(this, experiment);
        addItemViewHolder(item, parent);
    }

    public void addXSpeedGraph(ScriptComponentExperiment experiment, SheetGroupLayout parent) {
        GraphViewHolder item = new GraphViewHolder(this, experiment);
        item.setTitle("X-Speed vs. Time");
        item.setXAxisContent("time-speed");
        item.setYAxisContent("x-speed");
        addItemViewHolder(item, parent);
    }

    public void addYSpeedGraph(ScriptComponentExperiment experiment, SheetGroupLayout parent) {
        GraphViewHolder item = new GraphViewHolder(this, experiment);
        item.setTitle("Y-Speed vs. Time");
        item.setXAxisContent("time-speed");
        item.setYAxisContent("y-speed");
        addItemViewHolder(item, parent);
    }

    public ExperimentAnalysis getExperimentAnalysis(Context context, ScriptComponentExperiment experiment) {
        ExperimentAnalysis experimentAnalysis = experimentAnalysisMap.get(experiment.getExperimentPath());
        if (experimentAnalysis != null)
            return experimentAnalysis;
        experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(context,
                experiment.getExperimentPath());
        experimentAnalysisMap.put(experiment.getExperimentPath(), experimentAnalysis);
        return experimentAnalysis;
    }
}
