/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import nz.ac.auckland.lablet.camera.ITimeCalibration;


public abstract class MarkerTimeGraphAxis extends MarkerGraphAxis {
    @Override
    public void setMarkerGraphAdapter(MarkerGraphAdapter adapter) {
        assert adapter instanceof MarkerTimeGraphAdapter;
        super.setMarkerGraphAdapter(adapter);
    }

    public ITimeCalibration getTimeCalibration() {
        return ((MarkerTimeGraphAdapter)markerGraphAdapter).getTimeCalibration();
    }
}
