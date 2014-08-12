/*
 * Copyright 2014.
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import nz.ac.auckland.lablet.misc.LabletDataProvider;
import nz.ac.auckland.lablet.misc.StreamHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ExportDirDialog extends AlertDialog {
    private Activity activity;
    private File[] directories;
    private List<String> outputFiles = new ArrayList<>();
    private boolean canceled = false;

    protected ExportDirDialog(Activity activity, File[] directories) {
        super(activity);

        setCancelable(false);

        this.activity = activity;
        this.directories = directories;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View contentView = inflater.inflate(R.layout.export_dir_dialog, null);
        setTitle("Sending...");
        addContentView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        // scale length
        final TextView statusView = (TextView)contentView.findViewById(R.id.statusTextView);
        String text = "";
        statusView.setText(text);

        // button bar
        final Button cancelButton = (Button)contentView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canceled = true;
                dismiss();
            }
        });

        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                canceled = true;
                dismiss();
            }
        });

        final AsyncTask<File, String, Boolean> zipTask = new AsyncTask<File, String, Boolean>() {
            @Override
            protected Boolean doInBackground(File... files) {
                for (int i = 0; i < files.length; i++) {
                    if (canceled) {
                        dismiss();
                        return false;
                    }
                    final File dir = files[i];

                    String status = "Zipping: " + dir.getName();
                    publishProgress(status);

                    final long totalSize = DirSizeCalculator.getSize(files[i]);

                    File tempDir = LabletDataProvider.getProviderDir(getContext());
                    File outFile = new File(tempDir, dir.getName() + ".zip");
                    try {
                        ZipHelper helper = new ZipHelper(outFile);
                        helper.addDir(dir, new StreamHelper.IProgressListener() {
                            int totalProgress = 0;

                            @Override
                            public int getReportingStep() {
                                return 10 * 1024;
                            }

                            @Override
                            public void onNewProgress(int progress) {
                                totalProgress += progress;
                                String status = "Zipping: " + dir.getName() + " " + totalProgress / 1000 + "/"
                                        + totalSize / 1000 + "kBytes";
                                publishProgress(status);
                            }
                        });
                        helper.close();
                        outputFiles.add(outFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }

                }
                return true;
            }

            @Override
            protected void onProgressUpdate(String... progress) {
                statusView.setText(progress[0]);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (!result)
                    statusView.setText("Something went wrong!");
                else {
                    LabletDataProvider.mailData(activity, "Experiment", "", getOutputFiles());
                    dismiss();
                }
            }
        };
        zipTask.execute(directories);
    }

    static class DirSizeCalculator {
        static public long getSize(File dir) {
            List<File> dirs = new ArrayList<>();
            dirs.add(dir);
            return getSize(dirs);
        }

        static public long getSize(List<File> dirs) {
            long size = 0;
            while (dirs.size() > 0) {
                File dir = dirs.remove(0);
                if (!dir.isDirectory())
                    continue;

                for (File file : dir.listFiles()) {
                    if (file.isDirectory()) {
                        dirs.add(file);
                        continue;
                    }
                    size += file.length();
                }
            }
            return size;
        }
    }

    public List<String> getOutputFiles() {
        return outputFiles;
    }
}
