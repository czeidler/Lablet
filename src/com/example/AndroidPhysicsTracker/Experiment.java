package com.example.AndroidPhysicsTracker;


import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.format.Time;

import java.io.File;


interface IExperimentRunView {
    public void setCurrentRun(int run);
    public int getNumberOfRuns();

    // convert a coordinate on the screen to the real value of the measurement
    public void fromScreen(PointF screen, PointF real);
    public void toScreen(PointF real, PointF screen);
}


abstract public class Experiment {
    private int xUnit;
    private int yUnit;

    private String uid;
    private File storageDir;
    protected Context context;

    private MarkersDataModel tagMarkers;

    public Experiment(Context experimentContext, Bundle bundle, File storageDir) {
        context = experimentContext;
        uid = bundle.getString("uid");
        this.storageDir = storageDir;

        init();
    }

    public Experiment(Context experimentContext) {
        context = experimentContext;
        uid = generateNewUid();

        init();
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

    public void onSaveAdditionalData(File dir) {}

    public MarkersDataModel getTagMarkers() {
        return tagMarkers;
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

    protected String generateNewUid() {
        String identifier = getIdentifier();

        Time now = new Time(Time.getCurrentTimezone());
        android.text.format.DateFormat dateFormat = new android.text.format.DateFormat();
        CharSequence dateString = dateFormat.format("yyyy-MM-dd_hh:mm:ss", new java.util.Date());

        now.setToNow();
        String newUid = new String();
        if (identifier != "") {
            newUid += identifier;
            newUid += "_";
        }
        newUid += dateString;
        return newUid;
    }

    private void init() {
        tagMarkers = new MarkersDataModel();
    }
}
