/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.aucklanduni.physics.tracker.script;

import android.content.Context;
import android.view.View;

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

    public abstract View buildLayout(Context context, android.support.v4.app.Fragment parentFragment);
}
