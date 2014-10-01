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
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;


public interface ISensorAnalysis {
    final public static String EXPERIMENT_ANALYSIS_FILE_NAME = "experiment_analysis.xml";

    public String getIdentifier();
    public ISensorData getData();
    public boolean loadAnalysisData(Bundle bundle, File storageDir);

    public Bundle exportAnalysisData(File additionalStorageDir) throws IOException;
    public void exportTagMarkerCSVData(Writer writer) throws IOException;
}
