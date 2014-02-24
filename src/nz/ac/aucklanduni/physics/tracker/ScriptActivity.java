/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptActivity extends Activity {
    private List<String> scriptList = null;
    private ArrayAdapter<String> scriptListAdaptor = null;
    private ArrayList<CheckBoxListEntry> existingScriptList = null;
    private CheckBoxAdapter existingScriptListAdaptor = null;

    final int START_SCRIPT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.script);

        int grey = 70;
        int listBackgroundColor = Color.rgb(grey, grey, grey);

        // experiment list
        ListView scriptListView = (ListView)findViewById(R.id.scriptList);
        assert(scriptListView != null);
        scriptListView.setBackgroundColor(listBackgroundColor);
        scriptList = new ArrayList<String>();
        scriptListAdaptor = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, scriptList);

        scriptListView.setAdapter(scriptListAdaptor);

        scriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = scriptList.get(i);
                startScript(id);
            }
        });

        // experiment list
        ListView existingScriptListView = (ListView)findViewById(R.id.existingScriptListView);
        existingScriptListView.setBackgroundColor(listBackgroundColor);
        existingScriptList = new ArrayList<CheckBoxListEntry>();
        existingScriptListAdaptor = new CheckBoxAdapter(this, R.layout.check_box_list_item, existingScriptList);
        existingScriptListView.setAdapter(existingScriptListAdaptor);

        existingScriptListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String id = existingScriptList.get(i).getName();
                loadPreviousScript(id);
            }
        });

        copyResourceScripts(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateScriptList();
        updateExistingScriptList();
    }

    private void startScript(String id) {
        String fileName = id + ".lua";

        Intent intent = new Intent(this, ScriptRunnerActivity.class);
        intent.putExtra("script_name", fileName);
        intent.putExtra("script_user_data_dir", Script.generateScriptUid(id));
        startActivityForResult(intent, START_SCRIPT);
    }

    private boolean loadPreviousScript(String scriptDir) {
        Intent intent = new Intent(this, ScriptRunnerActivity.class);
        intent.putExtra("script_user_data_dir", scriptDir);
        startActivityForResult(intent, START_SCRIPT);
        return true;
    }

    private void updateScriptList() {
        scriptList.clear();
        File scriptDir = Script.getScriptDirectory(this);
        if (scriptDir.isDirectory()) {
            File[] children = scriptDir.listFiles();
            for (File child : children != null ? children : new File[0]) {
                String name = child.getName();
                if (!isLuaFile(name))
                    continue;
                name = name.substring(0, name.length() - 4);
                scriptList.add(name);
            }
        }

        scriptListAdaptor.notifyDataSetChanged();
    }

    private void copyResourceScripts(boolean overwriteExisting) {
        File scriptDir = Script.getScriptDirectory(this);
        if (!scriptDir.exists()) {
            if (!scriptDir.mkdir())
                return;
        }
        try {
            String[] files = getAssets().list("");
            for (String file : files) {
                if (!isLuaFile(file))
                    continue;
                InputStream inputStream = getAssets().open(file);
                File scriptOutFile = new File(scriptDir, file);
                if (!overwriteExisting && scriptOutFile.exists())
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
    }

    private boolean isLuaFile(String name) {
        return name.lastIndexOf(".lua") == name.length() - 4;
    }

    private void updateExistingScriptList() {
        existingScriptList.clear();
        File experimentDir = Script.getScriptUserDataDir(this);
        if (experimentDir.isDirectory()) {
            File[] children = experimentDir.listFiles();
            for (File child : children != null ? children : new File[0])
                existingScriptList.add(new CheckBoxListEntry(child.getName()));
        }

        existingScriptListAdaptor.notifyDataSetChanged();
    }
}
