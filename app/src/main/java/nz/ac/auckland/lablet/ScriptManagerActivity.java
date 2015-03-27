/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.script.ScriptMetaData;
import nz.ac.auckland.lablet.views.CheckBoxAdapter;
import nz.ac.auckland.lablet.views.CheckBoxListEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ScriptManagerActivity extends Activity {
    final private List<ScriptMetaData> scriptList = new ArrayList<>();
    final private List<ScriptMetaData> remoteScriptList = new ArrayList<>();
    final private ArrayList<CheckBoxListEntry> checkBoxList = new ArrayList<>();
    private CheckBoxAdapter checkBoxAdapter;
    private CheckBox allCheckBox;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.script_manager_actions, menu);

        final ScriptManagerActivity activity = this;
        // delete
        MenuItem deleteAction = menu.findItem(R.id.action_delete);
        assert (deleteAction != null);
        deleteAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Really want to DELETE the selected lab activities?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete();
                        updateScriptList();
                        allCheckBox.setChecked(false);
                        onCheckChanged();
                    }
                });
                builder.create().show();
                return true;
            }
        });
        deleteAction.setVisible(false);

        // update
        MenuItem updateAction = menu.findItem(R.id.action_update);
        assert(updateAction != null);
        updateAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Really want to update the selected remote lab activities?");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        List<ScriptMetaData> listToUpdate = new ArrayList<>();
                        for (ScriptMetaData scriptMetaData : scriptList) {
                            if (isRemote(scriptMetaData))
                                listToUpdate.add(scriptMetaData);
                        }
                        UpdateRemoteScriptDialog dialog = new UpdateRemoteScriptDialog(activity, listToUpdate);
                        dialog.show();
                    }
                });
                builder.create().show();
                return true;
            }
        });
        updateAction.setVisible(false);

        // add remote
        MenuItem addRemoteAction = menu.findItem(R.id.action_add_remote);
        assert(addRemoteAction != null);
        addRemoteAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AddRemoteScriptDialog dialog = new AddRemoteScriptDialog(activity);
                dialog.show();
                return true;
            }
        });

        // restore default
        MenuItem restoreAction = menu.findItem(R.id.action_restore_default_scripts);
        assert(restoreAction != null);
        restoreAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                resetDefaultActivities();
                return true;
            }
        });

        if (isAnySelected())
            deleteAction.setVisible(true);
        if (isAnyRemoteSelected())
            updateAction.setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Lab Activities Manager");
        setContentView(R.layout.script_manager);

        ListView listView = (ListView) findViewById(R.id.listView);

        checkBoxAdapter = new CheckBoxAdapter(this, R.layout.check_box_list_item, checkBoxList);
        listView.setAdapter(checkBoxAdapter);

        allCheckBox = (CheckBox)findViewById(R.id.selectAllCheckBox);
        allCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for (CheckBoxListEntry entry : checkBoxList)
                    entry.setSelected(b);
                checkBoxAdapter.notifyDataSetChanged();
                onCheckChanged();
            }
        });

        updateScriptList();
    }

    public void updateScriptList() {
        File remoteScriptDir = ScriptDirs.getRemoteScriptDir(this);
        remoteScriptList.clear();
        ScriptDirs.readScriptsFromDir(remoteScriptDir, remoteScriptList);
        scriptList.clear();
        ScriptDirs.readScriptList(scriptList, this);

        checkBoxList.clear();
        CheckBoxListEntry.OnCheckBoxListEntryListener onCheckBoxListEntryListener
                = new CheckBoxListEntry.OnCheckBoxListEntryListener() {
            @Override
            public void onSelected(CheckBoxListEntry entry) {
                onCheckChanged();
            }
        };
        for (ScriptMetaData metaData : scriptList) {
            String label = metaData.getLabel();
            if (isRemote(metaData)) {
                String remote = metaData.readRemote();
                if (!remote.equals(""))
                    label += " (" + remote + ")";
            }
            checkBoxList.add(new CheckBoxListEntry(label, onCheckBoxListEntryListener));
        }
        checkBoxAdapter.notifyDataSetChanged();
    }

    private boolean isRemote(ScriptMetaData metaData) {
        for (ScriptMetaData remote : remoteScriptList) {
            if (remote.file.equals(metaData.file))
                return true;
        }
        return false;
    }

    private void onCheckChanged() {
        invalidateOptionsMenu();
    }

    private boolean isAnySelected() {
        for (CheckBoxListEntry entry : checkBoxList) {
            if (entry.getSelected())
                return true;
        }
        return false;
    }

    private boolean isAnyRemoteSelected() {
        for (int i = 0; i < checkBoxList.size(); i++) {
            CheckBoxListEntry entry = checkBoxList.get(i);
            ScriptMetaData metaData = scriptList.get(i);
            if (entry.getSelected() && isRemote(metaData))
                return true;
        }
        return false;
    }

    private void resetDefaultActivities() {
        final Activity that = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset the pre-installed lab activities?");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ScriptDirs.copyResourceScripts(that, true);
                updateScriptList();

                allCheckBox.setChecked(false);
                onCheckChanged();
            }
        });
        builder.create().show();
    }

    private void delete() {
        for (int i = 0; i < checkBoxList.size(); i++) {
            CheckBoxListEntry entry = checkBoxList.get(i);
            if (!entry.getSelected())
                continue;

            ScriptMetaData metaData = scriptList.get(i);
            File scriptFile = metaData.file;
            String nameBase = StorageLib.removeExtension(scriptFile.getName());
            // if there is a remote file entry delete it
            File remoteFile = new File(scriptFile.getParent(), nameBase + "." + ScriptHomeActivity.REMOTE_TYPE);
            remoteFile.delete();
            scriptFile.delete();
        }
    }
}
