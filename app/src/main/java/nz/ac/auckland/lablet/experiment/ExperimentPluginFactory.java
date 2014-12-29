/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.experiment;

import nz.ac.auckland.lablet.accelerometer.AccelerometerAnalysisPlugin;
import nz.ac.auckland.lablet.accelerometer.AccelerometerDataTypePlugin;
import nz.ac.auckland.lablet.accelerometer.AccelerometerSensorPlugin;
import nz.ac.auckland.lablet.camera.MotionAnalysisPlugin;
import nz.ac.auckland.lablet.camera.CameraSensorPlugin;
import nz.ac.auckland.lablet.camera.VideoDataTypePlugin;
import nz.ac.auckland.lablet.camera.VideoImportPlugin;
import nz.ac.auckland.lablet.microphone.AudioDataTypePlugin;
import nz.ac.auckland.lablet.microphone.FrequencyAnalysisPlugin;
import nz.ac.auckland.lablet.microphone.MicrophoneSensorPlugin;
import nz.ac.auckland.lablet.microphone.WavImportPlugin;

import java.util.ArrayList;
import java.util.List;


/**
 * Singleton to manage a list of analysisPlugins.
 */
public class ExperimentPluginFactory {
    static ExperimentPluginFactory factory = null;

    private List<IDataTypePlugin> dataTypePlugins = new ArrayList<>();
    private List<ISensorPlugin> sensorPlugins = new ArrayList<>();
    private List<IAnalysisPlugin> analysisPlugins = new ArrayList<>();
    private List<IImportPlugin> importPlugins = new ArrayList<>();

    private ExperimentPluginFactory() {
        dataTypePlugins.add(new VideoDataTypePlugin());
        dataTypePlugins.add(new AudioDataTypePlugin());
        dataTypePlugins.add(new AccelerometerDataTypePlugin());

        sensorPlugins.add(new CameraSensorPlugin());
        sensorPlugins.add(new MicrophoneSensorPlugin());
        sensorPlugins.add(new AccelerometerSensorPlugin());

        analysisPlugins.add(new MotionAnalysisPlugin());
        analysisPlugins.add(new FrequencyAnalysisPlugin());
        analysisPlugins.add(new AccelerometerAnalysisPlugin());

        importPlugins.add(new VideoImportPlugin());
        importPlugins.add(new WavImportPlugin());
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

    public List<IImportPlugin> getImportPlugins() {
        return importPlugins;
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

    public ISensorData instantiateSensorData(String dataType) {
        for (IDataTypePlugin plugin : dataTypePlugins) {
            if (!plugin.getDataType().equals(dataType))
                continue;
            return plugin.instantiateData();
        }
        return null;
    }

    public IAnalysisPlugin findAnalysisPlugin(String pluginName) {
        if (pluginName == null)
            return null;

        for (IAnalysisPlugin plugin : analysisPlugins) {
            if (plugin.getIdentifier().equals(pluginName))
                return plugin;
        }
        return null;
    }

    public IAnalysisPlugin findAnalysisPluginForData(String dataType) {
        for (IAnalysisPlugin plugin : analysisPlugins) {
            if (plugin.supportedDataType().startsWith(dataType))
                return plugin;
        }
        return null;
    }

    public List<IAnalysisPlugin> analysisPluginsFor(ISensorData sensorData) {
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
