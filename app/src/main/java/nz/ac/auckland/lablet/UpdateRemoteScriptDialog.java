/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.misc.StreamHelper;
import nz.ac.auckland.lablet.script.ScriptMetaData;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class UpdateRemoteScriptDialog extends AlertDialog {
    private TextView statusView;
    final private List<ScriptMetaData> listToUpdate;

    public UpdateRemoteScriptDialog(Context context, List<ScriptMetaData> listToUpdate) {
        super(context);

        this.listToUpdate = listToUpdate;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.script_update_remotes, null);
        setTitle("Add Remote Lab Activity");

        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        statusView = (TextView)contentView.findViewById(R.id.statusTextView);

        // button bar
        Button cancelButton = (Button) contentView.findViewById(R.id.dismissButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        update();
    }

    private void setStatus(String text) {
        statusView.setText(text);
    }

    private void update() {
        setStatus("Updating...");

        final AsyncTask<Void, String, Boolean> uploadTask = new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg) {
                for (final ScriptMetaData metaData : listToUpdate) {
                    String remote = metaData.readRemote();
                    if (remote.equals(""))
                        continue;
                    try {
                        URL url = new URL(remote);
                        URLConnection connection = url.openConnection();
                        final int size = connection.getContentLength();
                        InputStream inputStream = url.openStream();
                        // TODO: only delete the old file on success
                        FileOutputStream outputStream = new FileOutputStream(metaData.file);
                        StreamHelper.copy(inputStream, outputStream, new StreamHelper.IProgressListener() {
                            @Override
                            public void onNewProgress(int totalProgress) {
                                publishProgress("Progress (" + metaData.file.getName() + "):" + totalProgress + " / "
                                        + size + "bytes");
                            }
                        });
                        outputStream.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return true;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                setStatus(progress[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                dismiss();
            }
        };
        uploadTask.execute();
    }
}
