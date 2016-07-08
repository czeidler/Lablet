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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import nz.ac.auckland.lablet.misc.NaturalOrderComparator;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.script.LuaScriptLoader;
import nz.ac.auckland.lablet.script.ScriptMetaData;
import nz.ac.auckland.lablet.views.*;
import nz.ac.auckland.lablet.script.Script;
import nz.ac.auckland.lablet.script.ScriptRunnerActivity;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Helper class to manage Lab Activities (scripts).
 */
class ScriptDirs {
    final static private String SCRIPTS_COPIED_KEY = "scripts_copied_v1";
    final static private String PREFERENCES_NAME = "lablet_preferences";

    /**
     * The script directory is the directory the stores the script files, i.e., the lua files.
     * @param context the context
     * @return the script directory File
     */
    static public File getScriptDirectory(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        File scriptDir = new File(baseDir, "scripts");
        if (!scriptDir.exists() && !scriptDir.mkdir())
            return null;
        return scriptDir;
    }

    static public File getResourceScriptDir(Context context) {
        return new File(getScriptDirectory(context), "demo");
    }

    static public File getRemoteScriptDir(Context context) {
        return new File(getScriptDirectory(context), "remotes");
    }

    /**
     * Copies the default Lab Activities from the app resources.
     *
     * @param activity the current activity
     * @param forceCopy overwrite existing Lab Activities
     */
    static public void copyResourceScripts(Activity activity, boolean forceCopy) {
        SharedPreferences settings = activity.getSharedPreferences(PREFERENCES_NAME, 0);
        if (!forceCopy && settings.getBoolean(SCRIPTS_COPIED_KEY, false))
            return;

        File scriptDir = getResourceScriptDir(activity);
        if (!scriptDir.exists()) {
            if (!scriptDir.mkdir())
                return;
        }
        try {
            String[] files = activity.getAssets().list("");
            for (String file : files) {
                if (!isLuaFile(file))
                    continue;
                InputStream inputStream = activity.getAssets().open(file);
                File scriptOutFile = new File(scriptDir, file);
                if (!forceCopy && scriptOutFile.exists())
                    continue;

                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(scriptOutFile, false));
                byte[] buffer = new byte[16384];
                while(true) {
                    int n = inputStream.read(buffer);
                    if (n <= -1)
                        break;
                    outputStream.write(buffer, 0, n);
                }

                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        settings.edit().putBoolean(SCRIPTS_COPIED_KEY, true).apply();
    }

    static public boolean isLuaFile(String name) {
        if (name.length() < 5)
            return false;
        return name.lastIndexOf(".lua") == name.length() - 4;
    }


    /**
     * Read available Lab Activities.
     *
     * @param scriptList found Lab Activities are places here
     * @param context current context
     */
    static public void readScriptList(List<ScriptMetaData> scriptList, Context context) {
        File[] scriptDirs = {
                getScriptDirectory(context),
                getResourceScriptDir(context),
                getRemoteScriptDir(context)
        };

        for (File scriptDir : scriptDirs) {
            if (scriptDir.isDirectory())
                readScriptsFromDir(scriptDir, scriptList);
        }
    }

    /**
     * Read available script from a certain directory.
     *
     * @param scriptDir the directory that should be searched
     * @param scripts found Lab Activities are places here
     */
    static public void readScriptsFromDir(File scriptDir, List<ScriptMetaData> scripts) {
        File[] children = scriptDir.listFiles();
        for (File child : children != null ? children : new File[0]) {
            String name = child.getName();
            if (!isLuaFile(name))
                continue;
            ScriptMetaData metaData = LuaScriptLoader.getScriptMetaData(child);
            if (metaData == null)
                continue;
            scripts.add(metaData);
        }
    }
}

/**
 * Main or home activity to manage scripts (lab activities).
 * <p>
 * The user is able to start a new script and resume or delete existing scripts.
 * </p>
 */
public class ScriptHomeActivity extends Activity {
    static final public String REMOTE_TYPE = "remote";

    private List<ScriptMetaData> scriptList = null;
    private ArrayAdapter<ScriptMetaData> scriptListAdaptor = null;
    private ArrayList<CheckBoxListEntry> existingScriptList = null;
    private CheckBoxListEntry.OnCheckBoxListEntryListener checkBoxListEntryListener;
    private CheckBoxAdapter existingScriptListAdaptor = null;
    private MenuItem deleteItem = null;
    private MenuItem exportItem = null;
    private AlertDialog deleteScriptDataAlertBox = null;
    private AlertDialog infoAlertBox = null;
    private CheckBox selectAllCheckBox = null;

    final static private int START_SCRIPT = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.script_activity_actions, menu);

        // script options
        MenuItem scriptOptions = menu.findItem(R.id.action_script_options);
        assert (scriptOptions != null);
        scriptOptions.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                showScriptMenu();
                return true;
            }
        });

        // to stand alone experiment screen
        MenuItem standAlone = menu.findItem(R.id.action_stand_alone);
        assert(standAlone != null);
        standAlone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                startStandAloneExperimentActivity();
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
        builder.setTitle("Really delete the selected script data?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteSelectedExistingScript();
            }
        });

        deleteScriptDataAlertBox = builder.create();

        deleteItem = menu.findItem(R.id.action_delete);
        assert deleteItem != null;
        deleteItem.setVisible(false);
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAtLeastOneExistingScriptSelected())
                    return false;
                deleteScriptDataAlertBox.show();
                return true;
            }
        });

        exportItem = menu.findItem(R.id.action_mail);
        assert exportItem != null;
        exportItem.setVisible(false);
        exportItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAtLeastOneExistingScriptSelected())
                    return false;
                exportSelection();
                return true;
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean atLeastOneSelected = isAtLeastOneExistingScriptSelected();
        deleteItem.setVisible(atLeastOneSelected);
        exportItem.setVisible(atLeastOneSelected);

        return super.onPrepareOptionsMenu(menu);
    }

    private void exportSelection() {
        List<File> exportList = new ArrayList<>();

        File scriptBaseDir = getScriptUserDataDir(this);
        for (CheckBoxListEntry entry : existingScriptList) {
            if (!entry.getSelected())
                continue;
            File experimentDir = new File(scriptBaseDir, entry.getName());
            exportList.add(experimentDir);
        }

        File[] fileArray = new File[exportList.size()];
        for (int i = 0; i < exportList.size(); i++)
            fileArray[i] = exportList.get(i);

        ExportDirDialog dirDialog = new ExportDirDialog(this, fileArray);
        dirDialog.show();
    }

    private void startStandAloneExperimentActivity() {
        Intent intent = new Intent(this, ExperimentHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.script_home);

        int grey = 70;
        int listBackgroundColor = Color.rgb(grey, grey, grey);

        // info side bar
        InfoSideBar infoSideBar = (InfoSideBar)findViewById(R.id.infoSideBar);
        assert infoSideBar != null;
        infoSideBar.setIcon(R.drawable.ic_console);
        infoSideBar.setInfoText("Lab Activities");
        infoSideBar.setBackground(new InfoBarBackgroundDrawable(Color.argb(255, 22, 115, 155)));

        // experiment list
        scriptList = new ArrayList<>();
        scriptListAdaptor = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, scriptList);
        ListView scriptListView = (ListView)findViewById(R.id.scriptList);
        scriptListView.setBackgroundColor(listBackgroundColor);
        scriptListView.setAdapter(scriptListAdaptor);
        scriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ScriptMetaData metaData = scriptList.get(i);
                startScript(metaData);
            }
        });

        // existing experiment list
        selectAllCheckBox = (CheckBox)findViewById(R.id.checkBoxSelectAll);
        selectAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                for (CheckBoxListEntry entry : existingScriptList)
                    entry.setSelected(b);
                existingScriptListAdaptor.notifyDataSetChanged();
            }
        });

        ListView existingScriptListView = (ListView)findViewById(R.id.existingScriptListView);
        existingScriptListView.setBackgroundColor(listBackgroundColor);
        existingScriptList = new ArrayList<>();
        existingScriptListAdaptor = new CheckBoxAdapter(this, R.layout.check_box_list_item, existingScriptList);
        existingScriptListView.setAdapter(existingScriptListAdaptor);

        existingScriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = existingScriptList.get(i).getName();
                loadPreviousScript(id);
            }
        });

        checkBoxListEntryListener = new CheckBoxListEntry.OnCheckBoxListEntryListener() {
            @Override
            public void onSelected(CheckBoxListEntry entry) {
                updateSelectedMenuItem();
            }
        };

        ScriptDirs.copyResourceScripts(this, false);
    }

    public void showScriptMenu() {
        final View parent = findViewById(R.id.action_script_options);
        final ScriptHomeActivity that = this;

        PopupMenu popup = new PopupMenu(this, parent);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.script_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.scriptManager:
                        Intent intent = new Intent(that, ScriptManagerActivity.class);
                        startActivity(intent);
                        return true;
                    default:
                        return false;
                }

            }
        });
        popup.show();
    }

    private void updateSelectedMenuItem() {
        invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();

        selectAllCheckBox.setChecked(false);
        invalidateOptionsMenu();

        updateScriptList();
        updateExistingScriptList();
    }

    /**
     * The script user data is the directory that contains the stored script state, i.e., the results.
     * @param context the context
     * @return the script user data
     */
    static public File getScriptUserDataDir(Context context) {
        File baseDir = context.getExternalFilesDir(null);
        File scriptDir = new File(baseDir, "script_user_data");
        if (!scriptDir.exists() && !scriptDir.mkdir())
            return null;
        return scriptDir;
    }

    private boolean isAtLeastOneExistingScriptSelected() {
        boolean itemSelected = false;
        for (CheckBoxListEntry entry : existingScriptList) {
            if (entry.getSelected()) {
                itemSelected = true;
                break;
            }
        }
        return itemSelected;
    }

    private void deleteSelectedExistingScript() {
        File scriptDir = getScriptUserDataDir(this);
        for (CheckBoxListEntry entry : existingScriptList) {
            if (!entry.getSelected())
                continue;
            File file = new File(scriptDir, entry.getName());
            StorageLib.recursiveDeleteFile(file);
        }
        selectAllCheckBox.setChecked(false);
        deleteItem.setVisible(false);
        updateExistingScriptList();
    }

    private void startScript(ScriptMetaData metaData) {
        String scriptId = metaData.getScriptFileName();
        File scriptUserDataDir = new File(getScriptUserDataDir(this), Script.generateScriptUid(scriptId));
        Intent intent = new Intent(this, ScriptRunnerActivity.class);
        intent.putExtra("script_path", metaData.file.getPath());
        intent.putExtra("script_user_data_dir", scriptUserDataDir.getPath());
        startActivityForResult(intent, START_SCRIPT);
    }

    private boolean loadPreviousScript(String scriptDir) {
        File scriptUserDataDir = new File(getScriptUserDataDir(this), scriptDir);

        Intent intent = new Intent(this, ScriptRunnerActivity.class);
        intent.putExtra("script_user_data_dir", scriptUserDataDir.getPath());
        startActivityForResult(intent, START_SCRIPT);
        return true;
    }

    public void updateScriptList() {
        scriptList.clear();
        ScriptDirs.readScriptList(scriptList, this);

        Collections.sort(scriptList, new Comparator<ScriptMetaData>() {
            @Override
            public int compare(ScriptMetaData metaData, ScriptMetaData metaData2) {
                return metaData.getTitle().compareTo(metaData2.getTitle());
            }
        });

        scriptListAdaptor.notifyDataSetChanged();
    }

    private void updateExistingScriptList() {
        existingScriptList.clear();
        File scriptDir = getScriptUserDataDir(this);
        if (scriptDir.isDirectory()) {
            List<String> children = new ArrayList<>();
            for (File file : scriptDir.listFiles())
                children.add(file.getName());
            Collections.sort(children, Collections.reverseOrder(new NaturalOrderComparator()));
            for (String child : children)
                existingScriptList.add(new CheckBoxListEntry(child, checkBoxListEntryListener));
        }

        existingScriptListAdaptor.notifyDataSetChanged();
    }
}
