/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;

import nz.ac.aucklanduni.physics.tracker.camera.CameraExperimentPlugin;

import java.util.ArrayList;
import java.util.List;


public class ExperimentPluginFactory {
    static ExperimentPluginFactory factory = null;

    private List<ExperimentPlugin> plugins;

    private ExperimentPluginFactory() {
        plugins = new ArrayList<ExperimentPlugin>();
        plugins.add(new CameraExperimentPlugin());
    }

    public List<ExperimentPlugin> getPluginList() {
        return plugins;
    }

    public static ExperimentPluginFactory getFactory() {
        if (factory == null)
            factory = new ExperimentPluginFactory();
        return factory;
    }

    public ExperimentPlugin findExperimentPlugin(String pluginName) {
        if (pluginName == null)
            return null;

        for (ExperimentPlugin plugin : plugins) {
            if (plugin.getName().equals(pluginName))
                return plugin;
        }
        return null;
    }
}
