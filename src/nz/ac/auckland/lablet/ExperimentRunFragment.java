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
    private IExperimentSensor sensor;

    public ExperimentRunFragment(IExperimentSensor sensor) {
        super();

        this.sensor = sensor;
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
        if (savedInstanceState != null) {
            ExperimentActivity activity = (ExperimentActivity)getActivity();
            int index = savedInstanceState.getInt("component", -1);
            if (index < 0)
                return null;
            sensor = activity.getCurrentSensors().get(index);
        }
        return sensor.createExperimentView(getActivity());
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        ExperimentActivity activity = (ExperimentActivity)getActivity();
        outState.putInt("component", activity.getCurrentSensors().indexOf(sensor));
    }

    public IExperimentSensor getSensor() {
        return sensor;
    }
}
