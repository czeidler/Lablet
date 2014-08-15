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
        List<IExperimentSensor> activeSensors = ((ExperimentActivity)activity).getActiveSensors();
        IExperimentSensor foundSensor = null;
        for (IExperimentSensor sensor : activeSensors) {
            String sensorName = sensor.getClass().getSimpleName();
            if (sensorName.equals(name)) {
                foundSensor = sensor;
                break;
            }
        }
        return foundSensor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        experimentRun = findExperimentFromArguments(getActivity());
        return experimentRun.createExperimentView(getActivity());
    }

    public IExperimentSensor getExperimentRun() {
        return experimentRun;
    }
}
