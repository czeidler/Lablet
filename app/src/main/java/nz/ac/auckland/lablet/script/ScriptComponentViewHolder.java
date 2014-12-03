/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.script;

import android.content.Context;
import android.view.View;

/**
 * Abstract base class for a component that is visualized by a Fragment.
 */
abstract public class ScriptComponentViewHolder extends ScriptComponent implements ISheetLayoutItemParameters {
    private float weight = 1.f;

    /**
     * Creates the view that displays the script component.
     *
     * @param context of the view
     * @param parent the fragment the view will live in
     * @return the view that displays the component
     */
    abstract public View createView(Context context, android.support.v4.app.Fragment parent);

    @Override
    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public float getWeight() {
        return weight;
    }
}
