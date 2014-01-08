package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;


public class ExperimentAnalyserActivity extends ExperimentActivity {

    static final int PERFORM_RUN_SETTINGS = 0;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.experiment_analyser_activity_actions, menu);

        MenuItem backItem = menu.findItem(R.id.action_back);
        backItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        });
        MenuItem settingsItem = menu.findItem(R.id.action_run_settings);
        StringBuilder settingsName = new StringBuilder();
        if (plugin.hasRunEditActivity(settingsName)) {
            settingsItem.setTitle(settingsName);
            settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    startRunSettingsActivity();
                    return true;
                }
            });
        } else {
            settingsItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void startRunSettingsActivity() {
        plugin.startRunSettingsActivity(experiment, this, PERFORM_RUN_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_RUN_SETTINGS) {
            // TODO reload settings
            return;
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadExperiment(getIntent());

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        ViewPager mPager = (ViewPager)findViewById(R.id.pager);
        ScreenSlidePagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        //setupViews();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0)
                return new AnalysisRunViewFragment(plugin, experiment);
            else if (position == 1)
                return new AnalysisMixedDataFragment(plugin, experiment);
            else if (position == 2)
                return new AnalysisTableGraphDataFragment(plugin, experiment);
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}


