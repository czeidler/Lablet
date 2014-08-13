/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.FileObserver;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.experiment.AbstractExperimentPlugin;
import nz.ac.auckland.lablet.experiment.IExperimentPlugin;
import nz.ac.auckland.lablet.experiment.ExperimentPluginFactory;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.views.CheckBoxAdapter;
import nz.ac.auckland.lablet.views.CheckBoxListEntry;
import nz.ac.auckland.lablet.views.InfoBarBackgroundDrawable;
import nz.ac.auckland.lablet.views.InfoSideBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


class InfoHelper {
    static private String getAuthorList() {
        String authors = "Authors:\n";
        authors += "\tClemens Zeidler <czei002@aucklanduni.ac.nz> (2013, 2014)\n";
        return authors;
    }

    static public AlertDialog createAlertInfoBox(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Lablet " + getVersionString(activity));
        builder.setNeutralButton("leave me alone", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("no thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast toast = Toast.makeText(activity.getApplicationContext(), "$%#@*!?", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        builder.setPositiveButton("love it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast toast = Toast.makeText(activity.getApplicationContext(), "+1", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        AlertDialog infoAlertBox = builder.create();
        final ScrollView scrollView = new ScrollView(activity.getApplicationContext());
        final TextView textView = new TextView(activity.getApplicationContext());
        textView.setPadding(10, 10, 10, 10);
        textView.setText(getAuthorList());
        textView.setTextSize(15);
        scrollView.addView(textView);
        infoAlertBox.setView(scrollView);
        return infoAlertBox;
    }

    static public String getVersionString(Activity activity) {
        String versionString = "Ver. ";
        try {
            versionString += activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionString = "?";
            e.printStackTrace();
        }
        return versionString;
    }
}


/**
 * Main or home activity to manage experiments.
 * <p>
 * The user is able to start a new experiment and resume or delete existing experiments.
 * </p>
 */
public class ExperimentHomeActivity extends Activity {
    private List<IExperimentPlugin> experimentPluginList = null;
    private ArrayList<CheckBoxListEntry> experimentList = null;
    private CheckBoxListEntry.OnCheckBoxListEntryListener checkBoxListEntryListener;
    private CheckBoxAdapter experimentListAdaptor = null;
    private CheckBox selectAllCheckBox = null;
    private MenuItem deleteItem = null;
    private MenuItem exportItem = null;
    private AlertDialog infoAlertBox = null;
    private AlertDialog deleteExperimentAlertBox = null;
    private ExperimentDirObserver experimentDirObserver = null;

    static final int PERFORM_EXPERIMENT = 0;
    static final int ANALYSE_EXPERIMENT = 1;

    public ExperimentHomeActivity() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);

        // script item
        MenuItem scriptItem = menu.findItem(R.id.action_scripts);
        assert(scriptItem != null);
        scriptItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startScriptActivity();
                return true;
            }
        });

        // info item
        MenuItem infoItem = menu.findItem(R.id.action_info);
        assert(infoItem != null);
        String versionString = InfoHelper.getVersionString(this);
        infoItem.setTitle(versionString);
        infoAlertBox = InfoHelper.createAlertInfoBox(this);
        infoItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                infoAlertBox.show();
                return true;
            }
        });

        // delete item
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

        exportItem = menu.findItem(R.id.action_mail);
        assert exportItem != null;
        exportItem.setVisible(false);
        exportItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAtLeastOneExperimentSelected())
                    return false;
                exportSelection();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean atLeastOneExperimentSelected = isAtLeastOneExperimentSelected();
        deleteItem.setVisible(atLeastOneExperimentSelected);
        exportItem.setVisible(atLeastOneExperimentSelected);

        return super.onPrepareOptionsMenu(menu);
    }

    private void exportSelection() {
        List<File> exportList = new ArrayList<>();

        File experimentBaseDir = ExperimentDataActivity.getDefaultExperimentBaseDir(this);
        for (CheckBoxListEntry entry : experimentList) {
            if (!entry.getSelected())
                continue;
            File experimentDir = new File(experimentBaseDir, entry.getName());
            exportList.add(experimentDir);
        }

        File[] fileArray = new File[exportList.size()];
        for (int i = 0; i < exportList.size(); i++)
            fileArray[i] = exportList.get(i);

        ExportDirDialog dirDialog = new ExportDirDialog(this, fileArray);
        dirDialog.show();
    }

    private boolean isAtLeastOneExperimentSelected() {
        boolean itemSelected = false;
        for (CheckBoxListEntry entry : experimentList) {
            if (entry.getSelected()) {
                itemSelected = true;
                break;
            }
        }
        return itemSelected;
    }

    private void deleteSelectedExperiments() {
        File experimentBaseDir = ExperimentDataActivity.getDefaultExperimentBaseDir(this);
        for (CheckBoxListEntry entry : experimentList) {
            if (!entry.getSelected())
                continue;
            File experimentDir = new File(experimentBaseDir, entry.getName());
            StorageLib.recursiveDeleteFile(experimentDir);
        }
        selectAllCheckBox.setChecked(false);
        deleteItem.setVisible(false);
        updateExperimentList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.experiment_home);

        // info side bar
        InfoSideBar infoSideBar = (InfoSideBar)findViewById(R.id.infoSideBar);
        assert infoSideBar != null;
        infoSideBar.setIcon(R.drawable.ic_lab);
        infoSideBar.setInfoText("Stand Alone Experiments");
        infoSideBar.setBackground(new InfoBarBackgroundDrawable(Color.argb(255, 75, 140, 20)));

        // plugin list
        experimentPluginList = ExperimentPluginFactory.getFactory().getPluginList();

        int grey = 70;
        int listBackgroundColor = Color.rgb(grey, grey, grey);

        ListView newExperimentList = (ListView)findViewById(R.id.newExperiments);
        newExperimentList.setBackgroundColor(listBackgroundColor);
        newExperimentList.setAdapter(new ArrayAdapter<IExperimentPlugin>(this,
                android.R.layout.simple_list_item_1, experimentPluginList));

        newExperimentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                List<IExperimentPlugin> pluginList = new ArrayList<IExperimentPlugin>();
                pluginList.add(experimentPluginList.get(i));
                startExperiment(pluginList);
            }
        });

        // experiment list
        ListView experimentListView = (ListView)findViewById(R.id.existingExperimentListView);
        experimentListView.setBackgroundColor(listBackgroundColor);
        experimentList = new ArrayList<CheckBoxListEntry>();
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
                for (CheckBoxListEntry entry : experimentList)
                    entry.setSelected(b);
                experimentListAdaptor.notifyDataSetChanged();
            }
        });

        checkBoxListEntryListener = new CheckBoxListEntry.OnCheckBoxListEntryListener() {
            @Override
            public void onSelected(CheckBoxListEntry entry) {
                updateSelectedMenuItem();
            }
        };

        File experimentDir = ExperimentDataActivity.getDefaultExperimentBaseDir(this);
        if (experimentDir.exists()) {
            // TODO: Events are never received, check why. Manually call updateExperimentList in onResume for now.
            experimentDirObserver = new ExperimentDirObserver(experimentDir.getPath());
            experimentDirObserver.startWatching();
        }
    }

    private void updateSelectedMenuItem() {
        invalidateOptionsMenu();
    }

    private void startAnalyzeActivityById(String id) {
        File experimentDir = ExperimentDataActivity.getDefaultExperimentBaseDir(this);
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

        selectAllCheckBox.setChecked(false);
        invalidateOptionsMenu();

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

    private void startExperiment(List<IExperimentPlugin> plugins) {
        Intent intent = new Intent(this, ExperimentActivity.class);
        AbstractExperimentPlugin.packStartExperimentIntent(intent, plugins, null);
        startActivityForResult(intent, PERFORM_EXPERIMENT);
    }

    private void startScriptActivity() {
        Intent intent = new Intent(this, ScriptHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void updateExperimentList() {
        experimentList.clear();
        File experimentDir = ExperimentDataActivity.getDefaultExperimentBaseDir(this);
        if (experimentDir.isDirectory()) {
            File[] children = experimentDir.listFiles();
            for (File child : children != null ? children : new File[0])
                experimentList.add(new CheckBoxListEntry(child.getName(), checkBoxListEntryListener));
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

}
