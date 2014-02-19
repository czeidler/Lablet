/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

abstract class ScriptComponent {
    abstract public void start();
    abstract Bundle getResults();
}

public class Script {
    private List<ScriptComponent> components = new ArrayList<ScriptComponent>();


    public void addComponent(ScriptComponent component) {
        components.add(component);
    }

    public void start(int componentIndex) {
        for (ScriptComponent component : components) {
            component.start();

        }
    }

    public void onComponentFinished(ScriptComponent component) {
        int index = components.indexOf(component);
        if (index < 0)
            return;

        Bundle bundle = component.getResults();
        if (bundle == null) {

        }

        index ++;
        if (index == components.size())
            return;
        components.get(index).start();
        return;
    }
}

