/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.*;
import android.widget.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


class ExperimentDirectoryEntry {
    private boolean selected = false;
    private String name;

    public interface OnDirectoryListener {
        public void onSelected(ExperimentDirectoryEntry entry);
    }
    static private OnDirectoryListener listener = null;
    static void setListener(OnDirectoryListener l) {
        listener = l;
    }

    ExperimentDirectoryEntry(String name) {
        this.name = name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
        if (listener != null)
            listener.onSelected(this);
    }

    boolean getSelected() {
        return selected;
    }
}


public class AndroidPhysicsTracker extends Activity {
    private List<ExperimentPlugin> experimentPluginList = null;
    private ArrayList<ExperimentDirectoryEntry> experimentList = null;
    private CheckBoxAdapter experimentListAdaptor = null;
    private CheckBox selectAllCheckBox = null;
    private MenuItem deleteItem = null;
    private AlertDialog deleteExperimentAlertBox = null;
    private ExperimentDirObserver experimentDirObserver = null;

    static final int PERFORM_EXPERIMENT = 0;
    static final int ANALYSE_EXPERIMENT = 1;


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setTitle("Really delete the selected experiments?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSelectedExperiments();
            }
        });

        deleteExperimentAlertBox = builder.create();

        deleteItem = menu.findItem(R.id.action_delete);
        assert deleteItem != null;
        deleteItem.setVisible(false);
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAtLeastOneExperimentSelected())
                    return false;
                deleteExperimentAlertBox.show();
                return true;
            }
        });

        ExperimentDirectoryEntry.setListener(new ExperimentDirectoryEntry.OnDirectoryListener() {
            @Override
            public void onSelected(ExperimentDirectoryEntry entry) {
                if (isAtLeastOneExperimentSelected())
                    deleteItem.setVisible(true);
                else
                    deleteItem.setVisible(false);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private boolean isAtLeastOneExperimentSelected() {
        boolean itemSelected = false;
        for (ExperimentDirectoryEntry entry : experimentList) {
            if (entry.getSelected()) {
                itemSelected = true;
                break;
            }
        }
        return itemSelected;
    }

    private void deleteSelectedExperiments() {
        File experimentDir = Experiment.getMainExperimentDir(this);
        for (ExperimentDirectoryEntry entry : experimentList) {
            if (!entry.getSelected())
                continue;
            File experimentFile = new File(experimentDir, entry.getName());
            ExperimentActivity.recursiveDeleteFile(experimentFile);
        }
        selectAllCheckBox.setChecked(false);
        deleteItem.setVisible(false);
        updateExperimentList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // plugin list
        experimentPluginList = ExperimentPluginFactory.getFactory().getPluginList();

        int grey = 70;
        int listBackgroundColor = Color.rgb(grey, grey, grey);

        ListView newExperimentList = (ListView)findViewById(R.id.newExperiments);
        newExperimentList.setBackgroundColor(listBackgroundColor);
        newExperimentList.setAdapter(new ArrayAdapter<ExperimentPlugin>(this,
                android.R.layout.simple_list_item_1, experimentPluginList));

        newExperimentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ExperimentPlugin plugin = experimentPluginList.get(i);
                startExperiment(plugin);
            }
        });

        // experiment list
        ListView experimentListView = (ListView) findViewById(R.id.existingExperimentListView);
        experimentListView.setBackgroundColor(listBackgroundColor);
        experimentList = new ArrayList<ExperimentDirectoryEntry>();
        experimentListAdaptor = new CheckBoxAdapter(this, R.layout.check_box_list_item, experimentList);
        experimentListView.setAdapter(experimentListAdaptor);

        experimentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = experimentList.get(i).getName();
                startAnalyzeActivityById(id);
            }
        });

        selectAllCheckBox = (CheckBox)findViewById(R.id.checkBoxSelectAll);
        selectAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for (ExperimentDirectoryEntry entry : experimentList)
                    entry.setSelected(b);
                experimentListAdaptor.notifyDataSetChanged();
            }
        });

        File experimentDir = Experiment.getMainExperimentDir(this);
        if (experimentDir.exists()) {
            // TODO: Events are never received, check why. Manually call updateExperimentList in onResume for now.
            experimentDirObserver = new ExperimentDirObserver(experimentDir.getPath());
            experimentDirObserver.startWatching();
        }
    }

    private void startAnalyzeActivityById(String id) {
        File experimentDir = Experiment.getMainExperimentDir(this);
        File experimentPath = new File(experimentDir, id);
        startAnalyzeActivity(experimentPath.getPath());
    }

    private void startAnalyzeActivity(String experimentPath) {
        Intent intent = new Intent(this, ExperimentAnalyserActivity.class);
        intent.putExtra("experiment_path", experimentPath);
        startActivityForResult(intent, ANALYSE_EXPERIMENT);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateExperimentList();
    }

    @Override
    public void onDestroy() {
        if (experimentDirObserver != null)
            experimentDirObserver.stopWatching();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == PERFORM_EXPERIMENT) {
            if (data == null)
                return;
            if (!data.getBooleanExtra("start_analysis", false))
                return;
            if (data.hasExtra("experiment_path")) {
                String experimentPath = data.getStringExtra("experiment_path");
                startAnalyzeActivity(experimentPath);
            }
            return;
        }

        if (requestCode == ANALYSE_EXPERIMENT) {

        }
    }

    private void startExperiment(ExperimentPlugin plugin) {
        plugin.startExperimentActivity(this, PERFORM_EXPERIMENT);
    }

    private void updateExperimentList() {
        experimentList.clear();
        File experimentDir = Experiment.getMainExperimentDir(this);
        if (experimentDir.isDirectory()) {
            File[] children = experimentDir.listFiles();
            for (File child : children != null ? children : new File[0])
                experimentList.add(new ExperimentDirectoryEntry(child.getName()));
        }

        experimentListAdaptor.notifyDataSetChanged();
    }

    private class ExperimentDirObserver extends FileObserver {
        public ExperimentDirObserver(String path) {
            super(path);
        }

        @Override
        public void onEvent(int event, String path) {
            switch (event) {
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                case FileObserver.MOVED_FROM:
                case FileObserver.MOVED_TO:
                    updateExperimentList();
                    break;
            }
        }
    }

    private class CheckBoxAdapter extends ArrayAdapter<ExperimentDirectoryEntry> {
        private ArrayList<ExperimentDirectoryEntry> listItems;
        private int layoutId;

        public CheckBoxAdapter(Context context, int textViewResourceId, ArrayList<ExperimentDirectoryEntry> items) {
            super(context, textViewResourceId, items);

            listItems = items;
            layoutId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = layoutInflater.inflate(layoutId, null);
            }
            assert convertView != null;
            ExperimentDirectoryEntry entry = listItems.get(position);

            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
            assert checkBox != null;
            checkBox.setTag(entry);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    ExperimentDirectoryEntry cookie = (ExperimentDirectoryEntry)compoundButton.getTag();
                    cookie.setSelected(b);
                }
            });
            TextView textView = (TextView)convertView.findViewById(android.R.id.text1);
            assert textView != null;
            checkBox.setChecked(entry.getSelected());
            textView.setText(entry.getName());
            return convertView;
        }
    }

}
