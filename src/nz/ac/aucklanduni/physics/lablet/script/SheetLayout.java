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


/**
 * Interface for sheet layout parameters.
 */
interface ISheetLayoutItemParameters {
    public void setWeight(float weight);
    public float getWeight();
}

/**
 * Abstract base class for the layout of an script component.
 * <p>
 * The idea is to specify the layout in the script file and load it into a SheetLayout. By calling
 * {@link SheetLayout.buildLayout} a real view with the layout can be created when needed.
 * </p>
 * <p>
 * Also see {@link nz.ac.aucklanduni.physics.lablet.script.SheetGroupLayout}.
 * </p>
 */
abstract public class SheetLayout {
    protected ISheetLayoutItemParameters parameters;

    public SheetLayout() {
        this.parameters = new SheetGroupLayoutParameters();
    }

    public SheetLayout(ISheetLayoutItemParameters parameters) {
        this.parameters = parameters;
    }
    public ISheetLayoutItemParameters getParameters() {
        return parameters;
    }

    /**
     * Build the layout.
     *
     * @param context of the layout
     * @param parentFragment the fragment the layout will live in
     * @return a the layout view
     */
    public abstract View buildLayout(Context context, android.support.v4.app.Fragment parentFragment);
}
