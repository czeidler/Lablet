/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import nz.ac.auckland.lablet.camera.CameraExperimentPlugin;
import nz.ac.auckland.lablet.microphone.MicrophoneExperimentPlugin;

import java.util.ArrayList;
import java.util.List;


/**
 * Singleton to manage a list of plugins.
 */
public class ExperimentPluginFactory {
    static ExperimentPluginFactory factory = null;

    private List<IExperimentPlugin> plugins;

    private ExperimentPluginFactory() {
        plugins = new ArrayList<IExperimentPlugin>();
        plugins.add(new CameraExperimentPlugin());
        //plugins.add(new AccelerometerExperimentPlugin());
        plugins.add(new MicrophoneExperimentPlugin());
    }

    /**
     * Get list of available plugins.
     *
     * @return list of plugins
     */
    public List<IExperimentPlugin> getPluginList() {
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
    public IExperimentPlugin findExperimentPlugin(String pluginName) {
        if (pluginName == null)
            return null;

        for (IExperimentPlugin plugin : plugins) {
            if (plugin.getName().equals(pluginName))
                return plugin;
        }
        return null;
    }
}
