/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;
import nz.ac.auckland.lablet.misc.PersistentBundle;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.List;


/**
 * Helper class.
 */
public class ExperimentLoader {

    static public Bundle loadBundleFromFile(File file) {
        Bundle bundle;
        InputStream inStream;
        try {
            inStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        PersistentBundle persistentBundle = new PersistentBundle();
        try {
            bundle = persistentBundle.unflattenBundle(inStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }

        return bundle;
    }

    // Creates a new ExperimentAnalysis and tries to load an existing analysis.
    static public ISensorAnalysis setupSensorAnalysis(SensorData sensorData, IAnalysisPlugin plugin, File analysisDir) {
        ISensorAnalysis sensorAnalysis = plugin.createSensorAnalysis(sensorData);

        // try to load old analysis
        File projectFile = new File(analysisDir, ISensorAnalysis.EXPERIMENT_ANALYSIS_FILE_NAME);
        Bundle bundle = ExperimentLoader.loadBundleFromFile(projectFile);
        if (bundle == null)
            return sensorAnalysis;

        Bundle analysisDataBundle = bundle.getBundle("analysis_data");
        if (analysisDataBundle == null)
            return sensorAnalysis;

        sensorAnalysis.loadAnalysisData(analysisDataBundle, analysisDir);

        return sensorAnalysis;
    }
}

