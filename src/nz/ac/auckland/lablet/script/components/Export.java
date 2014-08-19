/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script.components;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.script.ScriptComponentViewHolder;
import nz.ac.auckland.lablet.script.ScriptRunnerActivity;
import nz.ac.auckland.lablet.script.ScriptTreeNode;
import nz.ac.auckland.lablet.views.ExportDirDialog;

import java.io.File;


public class Export extends ScriptComponentViewHolder {
    public Export() {
        setState(ScriptTreeNode.SCRIPT_STATE_DONE);
    }

    @Override
    public View createView(Context context, final Fragment parent) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.script_component_export, null, true);
        assert view != null;

        Button exportButton = (Button)view.findViewById(R.id.exportButton);
        assert exportButton != null;
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportScriptUserData(parent);
            }
        });

        return view;
    }

    @Override
    public boolean initCheck() {
        return true;
    }

    private void exportScriptUserData(Fragment parent) {
        ScriptRunnerActivity activity = (ScriptRunnerActivity)parent.getActivity();
        if (!activity.saveScriptStateToFile())
            return;

        File[] fileArray = new File[1];
        fileArray[0] = activity.getScriptUserDataDir();

        ExportDirDialog dirDialog = new ExportDirDialog(parent.getActivity(), fileArray);
        dirDialog.show();
    }
}
