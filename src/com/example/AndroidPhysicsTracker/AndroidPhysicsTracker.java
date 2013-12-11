package com.example.AndroidPhysicsTracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class AndroidPhysicsTracker extends Activity {
    private List<ExperimentPlugin> experimentPluginList = new ArrayList<ExperimentPlugin>();

    static final int PERFORM_EXPERIMENT = 0;
    static final int ANALYSE_EXPERIMENT = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // add experiments
        experimentPluginList.add(new CameraExperimentPlugin());

        ListView newExperimentList = (ListView)findViewById(R.id.newExperiments);
        newExperimentList.setAdapter(new ArrayAdapter<ExperimentPlugin>(this,
                android.R.layout.simple_list_item_1, experimentPluginList));

        newExperimentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ExperimentPlugin plugin = experimentPluginList.get(i);
                startExperiment(plugin);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            Intent intent = new Intent(this, ExperimentAnalyserActivity.class);
            startActivityForResult(intent, ANALYSE_EXPERIMENT);
            return;
        }

        if (requestCode == ANALYSE_EXPERIMENT) {

        }
    }

    private void startExperiment(ExperimentPlugin plugin) {
        plugin.startExperimentActivity(this, PERFORM_EXPERIMENT);
    }
}
