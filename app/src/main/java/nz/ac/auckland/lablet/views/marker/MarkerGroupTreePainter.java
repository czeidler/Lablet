/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.marker;


public class MarkerGroupTreePainter extends TreePlotPainter<AbstractMarkerPainter> {
    private AbstractMarkerPainter.MarkerPainterGroup markerGroup;

    public void setMarkerGroup(AbstractMarkerPainter.MarkerPainterGroup markerGroup) {
        this.markerGroup = markerGroup;
        for (AbstractMarkerPainter child : childList)
            child.setMarkerPainterGroup(markerGroup);
    }

    @Override
    public void addChild(AbstractMarkerPainter child) {
        super.addChild(child);
        child.setMarkerPainterGroup(markerGroup);
    }
}
