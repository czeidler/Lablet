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
    ScriptComponentExperimentSheet experimentSheet;

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
            MarkerGraphAdapter adapter = new MarkerGraphAdapter(experimentAnalysis, "Position Data",
                    new XPositionMarkerGraphAxis(), new YPositionMarkerGraphAxis());
            graphView2D.setAdapter(adapter);
        }
        return view;
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

    public void addPositionGraph() {
        GraphItemViewHolder item = new GraphItemViewHolder(this);
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
