/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.script.Script;
import nz.ac.auckland.lablet.script.ScriptComponentViewHolder;
import nz.ac.auckland.lablet.script.ScriptRunnerActivity;
import nz.ac.auckland.lablet.script.ScriptTreeNode;
import nz.ac.auckland.lablet.views.ExportDirDialog;

import java.io.File;


public class Export extends ScriptComponentViewHolder {
    private String packageName = null;
    private String className = null;

    private boolean dataExported = false;

    public Export(Script script) {
        super(script);
        setState(ScriptTreeNode.SCRIPT_STATE_DONE);
    }

    @Override
    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);

        bundle.putBoolean("dataExported", dataExported);
    }

    @Override
    public boolean fromBundle(Bundle bundle) {
        dataExported = bundle.getBoolean("dataExported", false);
        return super.fromBundle(bundle);
    }

    private class ExportView extends ActivityStarterView {
        final private int EXPORT_REQUEST = 500;
        final CheckedTextView statusTextView;

        public ExportView(Context context, final Fragment parent) {
            super(context, (ScriptComponentSheetFragment)parent);

            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.script_component_export, null, true);
            assert view != null;

            OnClickListener onClickListener = new OnClickListener() {
                @Override
                public void onClick(View view) {
                    exportScriptUserData(parent);
                }
            };

            Button exportButton = (Button)view.findViewById(R.id.exportButton);
            assert exportButton != null;
            exportButton.setOnClickListener(onClickListener);

            ImageButton imageButton = (ImageButton)view.findViewById(R.id.imageButton);
            assert imageButton != null;
            imageButton.setOnClickListener(onClickListener);

            statusTextView = (CheckedTextView)view.findViewById(R.id.statusView);

            addView(view);

            updateViews();
        }

        private void updateViews() {
            if (dataExported) {
                statusTextView.setText("Data has been exported");
                statusTextView.setChecked(true);
                statusTextView.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode != Activity.RESULT_OK)
                return;

            if (requestCode != EXPORT_REQUEST)
                return;

            if (data == null)
                return;

            // This is UoAMailer specific, other mailer do not report the status!
            if (data.getStringExtra("status").equals("sent")) {
                dataExported = true;
                updateViews();
            }
        }

        private void exportScriptUserData(Fragment parent) {
            ScriptRunnerActivity activity = (ScriptRunnerActivity)parent.getActivity();
            if (!activity.saveScriptStateToFile())
                return;

            File[] fileArray = new File[1];
            fileArray[0] = activity.getScriptUserDataDir();

            ExportDirDialog dirDialog = new ExportDirDialog(parent.getActivity(), fileArray);
            dirDialog.setClassName(packageName, className);
            dirDialog.setActivityStarter(new ExportDirDialog.IActivityStarter() {
                @Override
                public void startActivity(Intent intent) {
                    startActivityForResult(intent, EXPORT_REQUEST);
                }
            });
            dirDialog.show();
        }
    }

    @Override
    public View createView(Context context, final Fragment parent) {
        return new ExportView(context, parent);
    }

    @Override
    public boolean initCheck() {
        return true;
    }

    public void setMailerClassName(String packageName, String className) {
        this.packageName = packageName;
        this.className = className;
    }
}
