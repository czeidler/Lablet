package nz.ac.aucklanduni.physics.tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lec on 16/12/13.
 */
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
