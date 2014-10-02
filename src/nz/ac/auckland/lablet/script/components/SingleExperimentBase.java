/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import nz.ac.auckland.lablet.ExperimentActivity;
import nz.ac.auckland.lablet.experiment.ExperimentHelper;
import nz.ac.auckland.lablet.experiment.ISensorPlugin;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.script.ScriptComponentViewHolder;
import nz.ac.auckland.lablet.script.ScriptRunnerActivity;
import nz.ac.auckland.lablet.script.ScriptTreeNode;

import java.io.File;


abstract class SingleExperimentBase extends ScriptComponentViewHolder {
    protected ScriptExperimentRef experiment = new ScriptExperimentRef();
    protected String descriptionText = "Please take an experiment:";

    @Override
    public boolean initCheck() {
        return true;
    }

    public ScriptExperimentRef getExperiment() {
        return experiment;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        if (experiment.getExperimentPath() != null)
            bundle.putString("experiment_path", experiment.getExperimentPath());
    }

    public boolean fromBundle(Bundle bundle) {
        if (!super.fromBundle(bundle))
            return false;

        experiment.setExperimentPath(bundle.getString("experiment_path", ""));
        return true;
    }
}


/**
 * View to start a camera experiment activity.
 */
abstract class ScriptComponentSingleExperimentBaseView<ExperimentSensorPlugin extends ISensorPlugin>
        extends ActivityStarterView {
    static final int PERFORM_EXPERIMENT = 0;

    final protected SingleExperimentBase experimentComponent;

    public ScriptComponentSingleExperimentBaseView(Context context, IActivityStarterViewParent parent,
                                                   SingleExperimentBase experimentComponent) {
        super(context, parent);

        this.experimentComponent = experimentComponent;
    }

    protected void startExperimentActivity(ExperimentSensorPlugin sensorPlugin) {
        Intent intent = new Intent(getContext(), ExperimentActivity.class);

        Bundle options = getExperimentOptions();

        String[] pluginName = new String[] {sensorPlugin.getSensorName()};
        ExperimentHelper.packStartExperimentIntent(intent, pluginName, options);
        intent.putExtras(options);

        startActivityForResult(intent, PERFORM_EXPERIMENT);
    }

    protected Bundle getExperimentOptions() {
        Bundle options = new Bundle();
        options.putBoolean("show_analyse_menu", false);
        options.putBoolean("sensors_editable", false);
        options.putInt("max_number_of_runs", 1);
        options.putString("experiment_base_directory", getScriptExperimentsDir().getPath());

        return options;
    }

    private File getScriptExperimentsDir() {
        ScriptComponentSheetFragment sheetFragment = (ScriptComponentSheetFragment)parent;
        ScriptRunnerActivity activity = (ScriptRunnerActivity)sheetFragment.getActivity();
        File scriptUserDataDir = activity.getScriptUserDataDir();

        File scriptExperimentDir = new File(scriptUserDataDir, "experiments");
        if (!scriptExperimentDir.exists())
            scriptExperimentDir.mkdirs();
        return scriptExperimentDir;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            if (data == null)
                return;
            if (data.hasExtra("experiment_path")) {
                String oldExperiment = experimentComponent.getExperiment().getExperimentPath();
                if (!oldExperiment.equals(""))
                    StorageLib.recursiveDeleteFile(new File(oldExperiment));

                String experimentPath = data.getStringExtra("experiment_path");
                experimentComponent.getExperiment().setExperimentPath(experimentPath);
                experimentComponent.setState(ScriptTreeNode.SCRIPT_STATE_DONE);

                experimentComponent.getExperiment().setExperimentPath(experimentPath);

                onExperimentPerformed();
            }
        }
    }

    abstract protected void onExperimentPerformed();
}
