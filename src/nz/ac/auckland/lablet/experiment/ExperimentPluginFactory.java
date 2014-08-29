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


/**
 * Singleton to manage a list of analysisPlugins.
 */
public class ExperimentPluginFactory {
    static ExperimentPluginFactory factory = null;

    private List<ISensorPlugin> sensorPlugins = new ArrayList<>();
    private List<IAnalysisPlugin> analysisPlugins = new ArrayList<>();

    private ExperimentPluginFactory() {
        //analysisPlugins.add(new CameraExperimentPlugin());
        //analysisPlugins.add(new AccelerometerExperimentPlugin());
        //analysisPlugins.add(new MicrophoneExperimentPlugin());
    }

    /**
     * Get list of available sensor plugins.
     *
     * @return list of sensor plugins
     */
    public List<ISensorPlugin> getSensorPlugins() {
        return sensorPlugins;
    }

    public List<IAnalysisPlugin> getAnalysisPlugins() {
        return analysisPlugins;
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
     * Find a sensor plugin by name.
     *
     * @param pluginName the plugin name
     * @return the plugin if found otherwise null
     */
    public ISensorPlugin findSensorPlugin(String pluginName) {
        if (pluginName == null)
            return null;

        for (ISensorPlugin plugin : sensorPlugins) {
            if (plugin.getSensorName().equals(pluginName))
                return plugin;
        }
        return null;
    }

    public IAnalysisPlugin findAnalysisPlugin(String pluginName) {
        if (pluginName == null)
            return null;

        for (IAnalysisPlugin plugin : analysisPlugins) {
            if (plugin.getName().equals(pluginName))
                return plugin;
        }
        return null;
    }

    public List<IAnalysisPlugin> analysisPluginsFor(SensorData sensorData) {
        List<IAnalysisPlugin> foundPlugins = new ArrayList<>();
        String dataType = sensorData.getDataType();

        for (IAnalysisPlugin plugin : analysisPlugins) {
            String supportedDataType = plugin.supportedDataType();
            if (dataType.startsWith(supportedDataType))
                foundPlugins.add(plugin);
        }

        return foundPlugins;
    }
}
