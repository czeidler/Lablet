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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import nz.ac.auckland.lablet.misc.StorageLib;
import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Entry for a remote Lab Activity.
 */
class ActivityRemoteEntry {
    final public String name;
    final public URL url;

    public ActivityRemoteEntry(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    /**
     * Reads remote Lab Activity entries from a directory.
     *
     * @param dir location of the remote Lab Activities entries
     * @return a list of ActivityRemoteEntries
     */
    static public List<ActivityRemoteEntry> getActivityRemoteEntries(File dir) {
        List<ActivityRemoteEntry> list = new ArrayList<>();

        File[] files = dir.listFiles();
        if (files == null)
            return list;

        for (File file : files) {
            if (!file.isFile())
                continue;
            String name = file.getName();
            if (name.lastIndexOf("." + ScriptHomeActivity.REMOTE_TYPE)
                    != name.length() - (ScriptHomeActivity.REMOTE_TYPE.length() + 1))
                continue;
            name = StorageLib.removeExtension(name);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                URL url = new URL(line);
                list.add(new ActivityRemoteEntry(name, url));
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}


/**
 * Dialog to add remote Lab Activities.
 *
 * Downloads a Lab Activity from a remote web server.
 */
public class AddRemoteScriptDialog extends AlertDialog {
    static final String HISTORY_DIR = "history";

    private ScriptManagerActivity activity;

    private URL url = null;
    private TextView statusView;

    private List<ActivityRemoteEntry> existingRemoteEntries;

    public AddRemoteScriptDialog(ScriptManagerActivity activity) {
        super(activity);

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        existingRemoteEntries = ActivityRemoteEntry.getActivityRemoteEntries(ScriptDirs.getRemoteScriptDir(
                getContext()));
        List<ActivityRemoteEntry> historyRemoteEntries = ActivityRemoteEntry.getActivityRemoteEntries(
                new File(ScriptDirs.getRemoteScriptDir(getContext()), HISTORY_DIR));
        List<String> remoteHistory = new ArrayList<>();
        for (ActivityRemoteEntry entry : historyRemoteEntries)
            remoteHistory.add(entry.url.toString());
        ArrayAdapter<String> historyAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_dropdown_item_1line, remoteHistory);

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.script_add_remote, null);
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

        final Button applyButton = (Button)contentView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });
        applyButton.setEnabled(false);

        final AutoCompleteTextView urlEditText = (AutoCompleteTextView)contentView.findViewById(R.id.editText);
        urlEditText.setSelection(urlEditText.getText().length());
        urlEditText.setAdapter(historyAdapter);
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setStatus("");
                boolean validUrl = true;
                try {
                    url = new URL(urlEditText.getText().toString());
                } catch (MalformedURLException e) {
                    validUrl = false;
                }

                if (validUrl) {
                    String name = new File(url.getPath()).getName();
                    if (!ScriptDirs.isLuaFile(name))
                        validUrl = false;
                }

                for (ActivityRemoteEntry entry : existingRemoteEntries) {
                    if (entry.url.equals(url)) {
                        validUrl = false;
                        setStatus("This URL already exist.");
                        break;
                    }
                }

                if (validUrl) {
                    applyButton.setEnabled(true);
                } else {
                    url = null;
                    applyButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setStatus(String text) {
        statusView.setText(text);
    }

    private String getFileName() {
        String name = new File(url.getPath()).getName();
        if (name.indexOf(".") > 0)
            name = name.substring(0, name.lastIndexOf("."));
        return name;
    }

    private void download() {
        setStatus("Downloading...");
        final File remoteDir = ScriptDirs.getRemoteScriptDir(getContext());
        remoteDir.mkdirs();
        String target = getFileName();
        File temp;
        for (int i = 0; ; i++) {
            if (i > 0)
                target = getFileName() + i;
            temp = new File(remoteDir, target + ".lua");
            if (!temp.exists())
                break;
        }
        final File targetFile = temp;
        final File targetRemoteFile = new File(remoteDir, target + "." + ScriptHomeActivity.REMOTE_TYPE);

        final AsyncTask<Void, String, Boolean> downloadTask = new AsyncTask<Void, String, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... arg) {
                try {
                    URLConnection connection = url.openConnection();
                    final int size = connection.getContentLength();
                    InputStream inputStream = url.openStream();
                    FileOutputStream outputStream = new FileOutputStream(targetFile);
                    StreamHelper.copy(inputStream, outputStream, new StreamHelper.IProgressListener() {
                        @Override
                        public void onNewProgress(long totalProgress) {
                            publishProgress("Progress: " + totalProgress + " / " + size + "bytes");
                        }
                    });
                    outputStream.close();
                    inputStream.close();

                    // create target file
                    Writer writer = new FileWriter(targetRemoteFile);
                    writer.write(url.toString());
                    writer.close();

                    // copy to success history
                    File historyDir = new File(remoteDir, HISTORY_DIR);
                    historyDir.mkdirs();
                    StorageLib.copyFile(targetRemoteFile, new File(historyDir, targetRemoteFile.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                setStatus(progress[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    activity.updateScriptList();
                    Toast.makeText(activity, "Lab Activity downloaded!", Toast.LENGTH_LONG).show();
                    dismiss();
                } else {
                    targetFile.delete();
                    setStatus("Failed to download file.");
                }
            }
        };
        downloadTask.execute();
    }
}

