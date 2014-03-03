/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;


class GraphItemViewHolder extends ScriptComponentItemViewHolder {
    private ScriptComponentExperimentSheet experimentSheet;
    private MarkerGraphAdapter adapter;
    private String xAxisContentId = "x-position";
    private String yAxisContentId = "y-position";
    private String title = "Position Data";

    public GraphItemViewHolder(ScriptComponentExperimentSheet experimentSheet) {
        this.experimentSheet = experimentSheet;
        setState(ScriptComponent.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.script_component_graph_view, null);
        assert view != null;

        GraphView2D graphView2D = (GraphView2D)view.findViewById(R.id.graphView);
        assert graphView2D != null;

        ExperimentAnalysis experimentAnalysis = experimentSheet.getExperimentAnalysis(context);
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
        return null;
    }

    public MarkerGraphAdapter getAdapter() {
        return adapter;
    }

    @Override
    public boolean initCheck() {
        return true;
    }
}

public class ScriptComponentExperimentSheet extends ScriptComponentSheet {
    private ScriptComponentExperiment experiment;
    private ExperimentAnalysis experimentAnalysis;

    public ScriptComponentExperimentSheet(Script script) {
        super(script);
    }

    @Override
    public boolean initCheck() {
        if (experiment == null) {
            lastErrorMessage = "no experiment given";
            return false;
        }
        return super.initCheck();
    }

    public void setExperiment(ScriptComponentExperiment experiment) {
        this.experiment = experiment;
    }
    public ScriptComponentExperiment getExperiment() {
        return experiment;
    }

    public GraphItemViewHolder addGraph() {
        GraphItemViewHolder item = new GraphItemViewHolder(this);
        addItemViewHolder(item);
        return item;
    }

    public void addPositionGraph() {
        GraphItemViewHolder item = new GraphItemViewHolder(this);
        addItemViewHolder(item);
    }

    public void addXSpeedGraph() {
        GraphItemViewHolder item = new GraphItemViewHolder(this);
        item.setTitle("X-Speed vs. Time");
        item.setXAxisContent("time");
        item.setYAxisContent("x-speed");
        addItemViewHolder(item);
    }

    public void addYSpeedGraph() {
        GraphItemViewHolder item = new GraphItemViewHolder(this);
        item.setTitle("Y-Speed vs. Time");
        item.setXAxisContent("time");
        item.setYAxisContent("y-speed");
        addItemViewHolder(item);
    }

    public ExperimentAnalysis getExperimentAnalysis(Context context) {
        if (experimentAnalysis != null)
            return experimentAnalysis;
        experimentAnalysis = ExperimentLoader.loadExperimentAnalysis(context,
                experiment.getExperimentPath());
        return experimentAnalysis;
    }
}
