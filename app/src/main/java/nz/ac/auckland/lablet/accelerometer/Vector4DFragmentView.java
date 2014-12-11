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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.views.plotview.*;


public class Vector4DFragmentView extends FrameLayout {
    public Vector4DFragmentView(Context context) {
        super(context);
    }

    public Vector4DFragmentView(final Context context, final Vector4DAnalysis analysis) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.vector4d_analysis, this, true);

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

        AccelerometerSensorData data = (AccelerometerSensorData)analysis.getData();

        XYDataAdapter xData = new XYDataAdapter(data.getTimeValues(), data.getXValues());
        XYConcurrentPainter xPainter = new XYConcurrentPainter(xData);
        strategyPainter.addChild(xPainter);

        XYDataAdapter yData = new XYDataAdapter(data.getTimeValues(), data.getYValues());
        XYConcurrentPainter yPainter = new XYConcurrentPainter(yData);
        yPainter.setPointRenderer(new CircleRenderer());
        Paint yMarkerPaint = new Paint();
        yMarkerPaint.setColor(Color.BLUE);
        yPainter.getDrawConfig().setMarkerPaint(yMarkerPaint);
        strategyPainter.addChild(yPainter);

        XYDataAdapter zData = new XYDataAdapter(data.getTimeValues(), data.getZValues());
        XYConcurrentPainter zPainter = new XYConcurrentPainter(zData);
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
    }
}
