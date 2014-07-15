/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import java.util.ArrayList;
import java.util.List;


public class ExperimentData {
    public static class SensorDataRef {
        final ExperimentData experimentData;
        final public int run;
        final public int sensor;

        public SensorDataRef(ExperimentData experimentData, int run, int sensor) {
            this.experimentData = experimentData;
            this.run = run;
            this.sensor = sensor;
        }
    }
    public static class SensorEntry {
        public IExperimentPlugin plugin;
        public SensorData sensorData;
    }

    public static class RunEntry {
        public ExperimentRunData runData;
        public List<SensorEntry> runs = new ArrayList<>();
    }

    public List<RunEntry> runs = new ArrayList<>();
}
