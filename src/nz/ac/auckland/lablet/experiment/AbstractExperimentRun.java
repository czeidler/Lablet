/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;


abstract public class AbstractExperimentRun implements IExperimentRun {
    private ExperimentRunGroup experimentRunGroup;

    @Override
    public ExperimentRunGroup getExperimentRunGroup() {
        return experimentRunGroup;
    }

    @Override
    public void setExperimentRunGroup(ExperimentRunGroup experimentRunGroup) {
        this.experimentRunGroup = experimentRunGroup;
    }
}
