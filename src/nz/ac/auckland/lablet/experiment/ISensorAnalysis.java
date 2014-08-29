/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import android.os.Bundle;

import java.io.File;


public interface ISensorAnalysis {
    final public static String EXPERIMENT_ANALYSIS_FILE_NAME = "experiment_analysis.xml";

    public boolean loadAnalysisData(Bundle bundle, File storageDir);
}
