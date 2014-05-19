/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.lablet.script;

import android.content.Context;
import android.view.View;

abstract public class ScriptComponentViewHolder extends ScriptComponent implements ISheetLayoutItemParameters {
    private float weight = 1.f;

    abstract public View createView(Context context, android.support.v4.app.Fragment parent);

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getWeight() {
        return weight;
    }
}
