/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import nz.ac.auckland.lablet.experiment.ExperimentData;
import nz.ac.auckland.lablet.experiment.IDataAnalysis;

import java.util.List;


/**
 * Fragment that displays a run view container and a tag data graph/table.
 */
public class ExperimentAnalysisFragment extends android.support.v4.app.Fragment {
    protected IDataAnalysis sensorAnalysis;
    protected ExperimentAnalysis.AnalysisRef analysisRef;

    public ExperimentAnalysisFragment() {
        super();
    }

    private void findExperimentFromArguments(Activity activity) {
        ExperimentAnalysisActivity experimentActivity = (ExperimentAnalysisActivity)activity;
        if (!experimentActivity.ensureExperimentDataLoaded())
            return;

        final ExperimentAnalysis experimentAnalysis = experimentActivity.getExperimentAnalysis();
        analysisRef = new ExperimentAnalysis.AnalysisRef(getArguments());
        ExperimentAnalysis.AnalysisRunEntry runEntry = experimentAnalysis.getCurrentAnalysisRun();
        sensorAnalysis = runEntry.analysisDataList.get(analysisRef.sensor).getAnalysisEntry(
                analysisRef.analysisId).analysis;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        findExperimentFromArguments(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        setupStandardMenu(menu, inflater);
    }

    protected void setupStandardMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.standard_analysis_actions, menu);

        // done menu item
        final MenuItem doneItem = menu.findItem(R.id.action_done);
        assert doneItem != null;
        doneItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
                return true;
            }
        });

        // data menu item
        ExperimentAnalysisActivity experimentActivity = (ExperimentAnalysisActivity)getActivity();
        final ExperimentAnalysis experimentAnalysis = experimentActivity.getExperimentAnalysis();
        final MenuItem dataItem = menu.findItem(R.id.action_data);
        assert dataItem != null;
        dataItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showDataMenu(experimentAnalysis);
                return true;
            }
        });
        // hide item if there is no more than one data
        if (experimentAnalysis.getAnalysisRunAt(analysisRef.run).analysisDataList.size() == 1)
            dataItem.setVisible(false);
    }

    private void showDataMenu(ExperimentAnalysis experimentAnalysis) {
        final View menuView = getActivity().findViewById(R.id.action_data);
        final List<ExperimentAnalysis.AnalysisDataEntry> dataList = experimentAnalysis.getAnalysisRunAt(
                analysisRef.run).analysisDataList;
        PopupMenu popup = new PopupMenu(getActivity(), menuView);
        final ViewPager pager = ((ExperimentAnalysisActivity) getActivity()).getViewPager();
        int i = 0;
        for (ExperimentAnalysis.AnalysisDataEntry entry : dataList) {
            for (ExperimentAnalysis.AnalysisEntry analysisEntry : entry.analysisList) {
                MenuItem item = popup.getMenu().add(0, i, Menu.NONE, analysisEntry.analysis.getDisplayName());
                item.setCheckable(true);

                if (pager.getCurrentItem() == i)
                    item.setChecked(true);
                i++;
            }
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int item = menuItem.getItemId();
                pager.requestLayout();
                pager.setCurrentItem(item, true);
                return false;
            }
        });
        popup.show();
    }

    public ExperimentData getExperimentData() {
        return ((ExperimentAnalysisActivity)getActivity()).getExperimentAnalysis().getExperimentData();
    }
}
