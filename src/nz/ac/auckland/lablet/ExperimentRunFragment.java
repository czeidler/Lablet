/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import nz.ac.auckland.lablet.experiment.IExperimentSensor;

import java.util.List;


public class ExperimentRunFragment extends android.support.v4.app.Fragment {
    private IExperimentSensor experimentRun;

    public ExperimentRunFragment(String experimentRunName) {
        super();

        Bundle args = new Bundle();
        args.putString("experiment_name", experimentRunName);
        setArguments(args);
    }

    public ExperimentRunFragment() {
        super();
    }

    private IExperimentSensor findExperimentFromArguments(Activity activity) {
        String name = getArguments().getString("experiment_name", "");
        ExperimentActivity experimentActivity = (ExperimentActivity)activity;
        List<IExperimentSensor> list = experimentActivity.getActiveSensors();
        for (IExperimentSensor experimentRun : list) {
            if (experimentRun.getClass().getSimpleName().equals(name))
                return experimentRun;
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return experimentRun.createExperimentView(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        experimentRun = findExperimentFromArguments(activity);
    }

    public IExperimentSensor getExperimentRun() {
        return experimentRun;
    }
}
