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
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


public class AccelerometerAnalysisView extends FrameLayout {
    final private AccelerometerAnalysis analysis;
    final private ViewGroup mainView;
    final private List<Number> totalData = new ArrayList<>();

    final public static int X_MARKER_COLOR = Color.GREEN;
    final public static int Y_MARKER_COLOR = Color.BLUE;
    final public static int Z_MARKER_COLOR = Color.RED;
    final public static int TOTAL_MARKER_COLOR = Color.WHITE;

    final public static IPointRenderer X_POINT_RENDERER = new CrossRenderer();
    final public static IPointRenderer Y_POINT_RENDERER = new CircleRenderer();
    final public static IPointRenderer Z_POINT_RENDERER = new BottomTriangleRenderer();
    final public static IPointRenderer TOTAL_POINT_RENDERER = new TopTriangleRenderer();

    public AccelerometerAnalysisView(final Context context, AccelerometerAnalysis analysis) {
        super(context);

        this.analysis = analysis;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Don't attach the view to the root view to make it switchable with the integral view.
        // (Android seems to reuse the root view otherwise)
        mainView = (ViewGroup)inflater.inflate(R.layout.accelerometer_analysis, this, false);
        addView(mainView);

        RectF savedRange = new RectF(analysis.getDisplaySettings().getRange());

        PlotView plotView = (PlotView)mainView.findViewById(R.id.plotView);
        plotView.getTitleView().setTitle("Accelerometer");
        plotView.getBackgroundPainter().setShowXGrid(true);
        plotView.getBackgroundPainter().setShowYGrid(true);
        StrategyPainter strategyPainter = new ThreadStrategyPainter();
        //StrategyPainter strategyPainter = new BufferedDirectStrategyPainter();

        plotView.setRangeListener(new RangeDrawingView.IRangeListener() {
            @Override
            public void onRangeChanged(RectF range) {
                AccelerometerAnalysisView.this.analysis.getDisplaySettings().setRange(range);
            }
        });

        AccelerometerSensorData data = analysis.getAccelerometerData();

        // total data
        for (int i = 0 ; i < data.getTimeValues().size(); i++) {
            float total = (float) Math.sqrt(Math.pow(data.getXValues().get(i).floatValue(), 2)
                    + Math.pow(data.getYValues().get(i).floatValue(), 2)
                    + Math.pow(data.getZValues().get(i).floatValue(), 2));
            totalData.add(total);
        }

        XYDataAdapter xData = new XYDataAdapter(data.getTimeValues(), data.getXValues());
        XYConcurrentPainter xPainter = new XYConcurrentPainter(xData, getContext());
        xPainter.setPointRenderer(X_POINT_RENDERER);
        Paint xMarkerPaint = new Paint();
        xMarkerPaint.setColor(X_MARKER_COLOR);
        xPainter.getDrawConfig().setMarkerPaint(xMarkerPaint);
        strategyPainter.addChild(xPainter);

        XYDataAdapter yData = new XYDataAdapter(data.getTimeValues(), data.getYValues());
        XYConcurrentPainter yPainter = new XYConcurrentPainter(yData, getContext());
        yPainter.setPointRenderer(Y_POINT_RENDERER);
        Paint yMarkerPaint = new Paint();
        yMarkerPaint.setColor(Y_MARKER_COLOR);
        yPainter.getDrawConfig().setMarkerPaint(yMarkerPaint);
        strategyPainter.addChild(yPainter);

        XYDataAdapter zData = new XYDataAdapter(data.getTimeValues(), data.getZValues());
        XYConcurrentPainter zPainter = new XYConcurrentPainter(zData, getContext());
        Paint zMarkerPaint = new Paint();
        zMarkerPaint.setColor(Z_MARKER_COLOR);
        zPainter.getDrawConfig().setMarkerPaint(zMarkerPaint);
        zPainter.setPointRenderer(Z_POINT_RENDERER);
        strategyPainter.addChild(zPainter);

        XYDataAdapter totalDataAdapter = new XYDataAdapter(data.getTimeValues(), totalData);
        XYConcurrentPainter totalPainter = new XYConcurrentPainter(totalDataAdapter, getContext());
        Paint totalMarkerPaint = new Paint();
        totalMarkerPaint.setColor(TOTAL_MARKER_COLOR);
        totalPainter.getDrawConfig().setMarkerPaint(totalMarkerPaint);
        totalPainter.setPointRenderer(TOTAL_POINT_RENDERER);
        strategyPainter.addChild(totalPainter);

        plotView.addPlotPainter(strategyPainter);

        LegendPainter legend = new LegendPainter();
        legend.addEntry(xPainter, "x-acceleration");
        legend.addEntry(yPainter, "y-acceleration");
        legend.addEntry(zPainter, "z-acceleration");
        legend.addEntry(totalPainter, "total-acceleration");
        plotView.addForegroundPainter(legend);

        plotView.autoZoom();
        RectF range = plotView.getRange();
        plotView.setMaxRange(range);
        plotView.setDraggable(true);
        plotView.setZoomable(true);

        if (Math.abs(savedRange.width()) > 0 && Math.abs(savedRange.height()) > 0)
            plotView.setRange(savedRange);
    }

    private View integralView = null;

    public void toggleIntegralView() {
        if (integralView != null) {
            removeView(integralView);
            integralView = null;
            mainView.setVisibility(VISIBLE);
            return;
        }

        mainView.setVisibility(INVISIBLE);
        integralView = new IntegralView(getContext(), analysis, totalData);
        addView(integralView);
    }
}
