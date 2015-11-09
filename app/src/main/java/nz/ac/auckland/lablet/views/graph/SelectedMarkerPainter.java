/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.graph;

import android.graphics.Canvas;
import android.graphics.PointF;
import nz.ac.auckland.lablet.views.marker.MarkerData;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.views.plotview.AbstractPlotPainter;
import nz.ac.auckland.lablet.views.plotview.CircleRenderer;
import nz.ac.auckland.lablet.views.plotview.DrawConfig;
import nz.ac.auckland.lablet.views.plotview.IPointRenderer;


public class SelectedMarkerPainter extends AbstractPlotPainter {
    final private MarkerDataModel dataModel;
    final private AbstractGraphAdapter.IGraphDataAxis xAxis;
    final private AbstractGraphAdapter.IGraphDataAxis yAxis;
    private DrawConfig config;
    private IPointRenderer pointRenderer = new CircleRenderer();
    final private PointF selectedMarkerPoint = new PointF();

    final private MarkerDataModel.IListener listener = new MarkerDataModel.IListener() {
        @Override
        public void onDataAdded(MarkerDataModel model, int index) {

        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {

        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {
            invalidate();
        }
    };

    public SelectedMarkerPainter(MarkerDataModel data, AbstractGraphAdapter.IGraphDataAxis xAxis,
                                 AbstractGraphAdapter.IGraphDataAxis yAxis, DrawConfig config) {
        this.dataModel = data;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.config = config;

        dataModel.addListener(listener);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        int selected = dataModel.getSelectedMarkerData();
        if (selected >= 0 && selected < xAxis.size() && selected < yAxis.size()) {
            Number x = xAxis.getValue(selected);
            Number y = yAxis.getValue(selected);

            selectedMarkerPoint.set(containerView.toScreenX(x.floatValue()), containerView.toScreenY(y.floatValue()));
            pointRenderer.drawPoint(canvas, selectedMarkerPoint, config);
        }
    }
}
