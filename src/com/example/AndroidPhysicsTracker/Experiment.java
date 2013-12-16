package com.example.AndroidPhysicsTracker;


import android.os.Bundle;
import android.text.format.Time;

import java.io.File;


abstract public class Experiment {
    private int xUnit;
    private int yUnit;

    private String uid;
    private File storageDir;

    public Experiment(Bundle bundle, File storageDir) {
        uid = bundle.getString("uid");
        this.storageDir = storageDir;
    }

    public Experiment() {
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
    abstract public ExperimentRun getRunAt(int i);

}
