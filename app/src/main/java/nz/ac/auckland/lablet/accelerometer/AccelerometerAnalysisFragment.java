/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.os.Bundle;
import android.view.*;
import nz.ac.auckland.lablet.ExperimentAnalysisFragment;
import nz.ac.auckland.lablet.R;


public class AccelerometerAnalysisFragment extends ExperimentAnalysisFragment {
    private AccelerometerAnalysisView analysisView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (sensorAnalysis == null)
            return;

        menu.clear();
        inflater.inflate(R.menu.accelerometer_analysis_actions, menu);

        final MenuItem viewItem = menu.findItem(R.id.action_view);
        assert viewItem != null;
        viewItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                analysisView.toggleIntegralView();
                return true;
            }
        });

        setupStandardMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        analysisView = new AccelerometerAnalysisView(getActivity(), getSensorAnalysis());
        return analysisView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        analysisView = null;
    }

    private AccelerometerAnalysis getSensorAnalysis() {
        return (AccelerometerAnalysis)sensorAnalysis;
    }
}
