/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.experiment;

import nz.ac.aucklanduni.physics.lablet.camera.CameraExperimentPlugin;

import java.util.ArrayList;
import java.util.List;


/**
 * Singleton to manage a list of plugins.
 */
public class ExperimentPluginFactory {
    static ExperimentPluginFactory factory = null;

    private List<ExperimentPlugin> plugins;

    private ExperimentPluginFactory() {
        plugins = new ArrayList<ExperimentPlugin>();
        plugins.add(new CameraExperimentPlugin());
    }

    /**
     * Get list of available plugins.
     *
     * @return list of plugins
     */
    public List<ExperimentPlugin> getPluginList() {
        return plugins;
    }

    /**
     * Singleton get method.
     *
     * @return the one and only factory instance
     */
    public static ExperimentPluginFactory getFactory() {
        if (factory == null)
            factory = new ExperimentPluginFactory();
        return factory;
    }

    /**
     * Find a plugin by name.
     *
     * @param pluginName the plugin name
     * @return the plugin if found otherwise null
     */
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
