/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.HCursorDataModelPainter;
import nz.ac.auckland.lablet.views.VCursorDataModelPainter;
import nz.ac.auckland.lablet.views.plotview.*;


public class AccelerometerAnalysisView extends FrameLayout {
    public AccelerometerAnalysisView(Context context) {
        super(context);
    }

    public AccelerometerAnalysisView(final Context context, final AccelerometerAnalysis analysis) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.accelerometer_analysis, this, true);

        RectF savedRange = new RectF(analysis.getDisplaySettings().getRange());

        PlotView plotView = (PlotView)view.findViewById(R.id.plotView);
        plotView.getTitleView().setTitle("Accelerometer");
        plotView.getBackgroundPainter().setShowXGrid(true);
        plotView.getBackgroundPainter().setShowYGrid(true);
        StrategyPainter strategyPainter = new ThreadStrategyPainter();
        //StrategyPainter strategyPainter = new BufferedDirectStrategyPainter();

        plotView.setRangeListener(new RangeDrawingView.IRangeListener() {
            @Override
            public void onRangeChanged(RectF range) {
                analysis.getDisplaySettings().setRange(range);
            }
        });

        AccelerometerExperimentData data = (AccelerometerExperimentData)analysis.getData();

        XYDataAdapter xData = new XYDataAdapter(data.getTimeValues(), data.getXValues());
        XYConcurrentPainter xPainter = new XYConcurrentPainter(xData, getContext());
        strategyPainter.addChild(xPainter);

        XYDataAdapter yData = new XYDataAdapter(data.getTimeValues(), data.getYValues());
        XYConcurrentPainter yPainter = new XYConcurrentPainter(yData, getContext());
        yPainter.setPointRenderer(new CircleRenderer());
        Paint yMarkerPaint = new Paint();
        yMarkerPaint.setColor(Color.BLUE);
        yPainter.getDrawConfig().setMarkerPaint(yMarkerPaint);
        strategyPainter.addChild(yPainter);

        XYDataAdapter zData = new XYDataAdapter(data.getTimeValues(), data.getZValues());
        XYConcurrentPainter zPainter = new XYConcurrentPainter(zData, getContext());
        Paint zMarkerPaint = new Paint();
        zMarkerPaint.setColor(Color.RED);
        zPainter.getDrawConfig().setMarkerPaint(zMarkerPaint);
        zPainter.setPointRenderer(new BottomTriangleRenderer());
        strategyPainter.addChild(zPainter);

        plotView.addPlotPainter(strategyPainter);

        LegendPainter legend = new LegendPainter();
        legend.addEntry(xPainter, "x-acceleration");
        legend.addEntry(yPainter, "y-acceleration");
        legend.addEntry(zPainter, "z-acceleration");
        plotView.addForegroundPainter(legend);

        plotView.autoZoom();
        RectF range = plotView.getRange();
        plotView.setMaxRange(range);
        plotView.setDraggable(true);
        plotView.setZoomable(true);

        if (Math.abs(savedRange.width()) > 0 && Math.abs(savedRange.height()) > 0)
            plotView.setRange(savedRange);

        // marker
        MarkerDataModel baseLineMarker = analysis.getBaseLineMarker();
        HCursorDataModelPainter hCursorDataModelPainter = new HCursorDataModelPainter(baseLineMarker);
        plotView.addPlotPainter(hCursorDataModelPainter);

        MarkerDataModel rangeMarkers = analysis.getRangeMarkers();
        VCursorDataModelPainter vCursorDataModelPainter = new VCursorDataModelPainter(rangeMarkers);
        vCursorDataModelPainter.setMarkerPainterGroup(hCursorDataModelPainter.getMarkerPainterGroup());
        plotView.addPlotPainter(vCursorDataModelPainter);
    }

    private View integralView = null;

    public void toggleIntegralView() {
        if (integralView != null) {
            removeView(integralView);
            integralView = null;
            return;
        }

        integralView = new IntegralView(getContext());
        addView(integralView);
    }
}
