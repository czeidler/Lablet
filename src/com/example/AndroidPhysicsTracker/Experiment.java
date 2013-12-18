package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;

import java.io.File;


interface IExperimentRunView {
    public void setCurrentRun(Bundle bundle);
}


abstract public class Experiment {
    private int xUnit;
    private int yUnit;

    private String uid;
    private File storageDir;
    protected Context context;

    public Experiment(Context experimentContext, Bundle bundle, File storageDir) {
        context = experimentContext;
        uid = bundle.getString("uid");
        this.storageDir = storageDir;
    }

    public Experiment(Context experimentContext) {
        context = experimentContext;

        String identifier = getIdentifier();

        Time now = new Time(Time.getCurrentTimezone());
        now.setToNow();
        uid = new String();
        uid += now;
        if (identifier != "") {
            uid += "_";
            uid += identifier;
        }
    }

    static public File getMainExperimentDir(Context context) {
        return context.getExternalFilesDir(null);
    }

    public void setStorageDir(File dir) {
        storageDir = dir;
    }

    public File getStorageDir() {
        return storageDir;
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);

        return bundle;
    }

    public String getUid() {
        return uid;
    }

    protected String getIdentifier() {
        return this.getClass().getSimpleName();
    }


    abstract public int getNumberOfRuns();
    abstract public Bundle getRunAt(int i);

}
