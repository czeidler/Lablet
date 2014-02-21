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

import java.io.File;

public class ScriptRunnerActivity extends FragmentActivity {
    private Script script = null;
    private ScriptFragmentPagerAdapter pagerAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.experiment_analyser);
        // Instantiate a ViewPager and a PagerAdapter.

        script = new Script();
        if (!loadScript(getIntent()))
            return;

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pagerAdapter = new ScriptFragmentPagerAdapter(getSupportFragmentManager(), script);
        pager.setAdapter(pagerAdapter);
    }

    protected boolean loadScript(Intent intent) {
        if (intent == null) {
            showErrorAndFinish("can't load experiment (Intent is null)");
            return false;
        }

        String scriptPath = intent.getStringExtra("script_path");
        if (scriptPath != null) {
            ScriptComponentFragmentFactory factory = new ScriptComponentFragmentFactory();
            LuaScriptLoader loader = new LuaScriptLoader(factory);
            script = loader.load(new File(scriptPath));
            if (script == null) {
                showErrorAndFinish("Error in script: ", loader.getLastError());
                return false;
            }
            script.start();
            return true;
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

    private class ScriptFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private Script script;

        public ScriptFragmentPagerAdapter(android.support.v4.app.FragmentManager fragmentManager, Script script) {
            super(fragmentManager);

            this.script = script;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            if (position == 0) {
                ScriptComponentFragment fragmentCreator = (ScriptComponentFragment)script.getCurrentComponent();
                return fragmentCreator.createFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            if (script.getCurrentComponent() != null)
                return 1;
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
