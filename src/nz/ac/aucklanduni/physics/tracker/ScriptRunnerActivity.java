/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.io.*;

public class ScriptRunnerActivity extends FragmentActivity implements Script.IScriptListener {
    private Script script = null;
    private ViewPager pager = null;
    private ScriptFragmentPagerAdapter pagerAdapter = null;
    private File scriptUserDataDir = null;
    private File scriptFile = null;

    final String SCRIPT_USER_DATA_FILENAME = "user_data.xml";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String userDataDir = savedInstanceState.getString("script_user_data_dir");
            if (userDataDir == null) {
                showErrorAndFinish("can't start script from saved instance state (user data directory is null)");
                return;
            }
            scriptUserDataDir = new File(Script.getScriptUserDataDir(this), userDataDir);
            if (!scriptUserDataDir.exists())
                scriptUserDataDir.mkdir();
            loadExistingScript(scriptUserDataDir);
        } else if (!createFormIntent())
            return;


        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.
        pager = (ViewPager)findViewById(R.id.pager);
        pagerAdapter = new ScriptFragmentPagerAdapter(getSupportFragmentManager(), script);
        pager.setAdapter(pagerAdapter);
    }

    private boolean createFormIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            showErrorAndFinish("can't start script (intent is null)");
            return false;
        }

        String scriptName = intent.getStringExtra("script_name");
        String userDataDir = intent.getStringExtra("script_user_data_dir");

        if (userDataDir == null) {
            showErrorAndFinish("can't start script (user data directory is null)");
            return false;
        }
        scriptUserDataDir = new File(Script.getScriptUserDataDir(this), userDataDir);
        if (!scriptUserDataDir.exists())
            scriptUserDataDir.mkdir();

        if (scriptName != null) {
            scriptFile = new File(Script.getScriptDirectory(this), scriptName);
            if (!loadScript(scriptFile))
                return false;
        } else if (!loadExistingScript(scriptUserDataDir)) {
            showErrorAndFinish("can't load existing script");
            return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        outState.putString("script_user_data_dir", scriptUserDataDir.getName());
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveScriptDataToFile();
    }

    protected boolean loadScript(File scriptFile) {
        ScriptComponentFragmentFactory factory = new ScriptComponentFragmentFactory();
        LuaScriptLoader loader = new LuaScriptLoader(factory);
        script = loader.load(scriptFile);
        script.setListener(this);
        if (script == null) {
            showErrorAndFinish("Error in script: ", loader.getLastError());
            return false;
        }
        script.start();
        return true;
    }

    protected boolean loadExistingScript(File scriptDir) {
        File userDataFile = new File(scriptDir, SCRIPT_USER_DATA_FILENAME);

        Bundle bundle;
        InputStream inStream;
        try {
            inStream = new FileInputStream(userDataFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        PersistentBundle persistentBundle = new PersistentBundle();
        try {
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        String scriptPath = bundle.getString("script_name");
        if (scriptPath == null)
            return false;
        scriptFile = new File(Script.getScriptDirectory(this), scriptPath);
        if (!loadScript(scriptFile))
            return false;
        return script.loadScript(bundle);
    }

    protected boolean saveScriptDataToFile() {
        Bundle bundle = new Bundle();
        bundle.putString("script_name", scriptFile.getName());
        if (!script.saveScript(bundle))
            return false;

        File projectFile = new File(scriptUserDataDir, SCRIPT_USER_DATA_FILENAME);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(projectFile);

            PersistentBundle persistentBundle = new PersistentBundle();
            persistentBundle.flattenBundle(bundle, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void showErrorAndFinish(String error) {
        showErrorAndFinish(error, null);
    }

    protected void showErrorAndFinish(String error, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(error);
        if (message != null)
            builder.setMessage(message);
        builder.setNeutralButton("Ok", null);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onCurrentComponentChanged(ScriptComponent current) {
        if (pagerAdapter == null)
            return;
        pagerAdapter.notifyDataSetChanged();
        pager.setCurrentItem(current.getStepsToRoot() - 1);
    }

    private class ScriptFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private Script script;

        public ScriptFragmentPagerAdapter(android.support.v4.app.FragmentManager fragmentManager, Script script) {
            super(fragmentManager);

            this.script = script;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            int i = 0;
            ScriptComponent component = script.getRoot();
            while (i != position) {
                i++;
                component = component.getNext();
            }
            ScriptComponentFragmentHolder fragmentCreator = (ScriptComponentFragmentHolder)component;
            return fragmentCreator.createFragment();
        }

        @Override
        public int getCount() {
            if (script.getCurrentComponent() != null)
                return script.getCurrentComponent().getStepsToRoot();
            return 0;
        }

        /*
        @Override
        public int getItemPosition(Object object)
        {
            View o = (View)object;
            int index = mMessages.indexOf(o.getTag());
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
            return POSITION_NONE;
        }
        */
    }
}
