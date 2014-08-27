/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;


abstract public class AbstractPluginExperimenter implements IExperimentPlugin.IExperimenter {
    final protected IExperimentPlugin plugin;

    public AbstractPluginExperimenter(IExperimentPlugin plugin) {
        this.plugin = plugin;
    }
}
