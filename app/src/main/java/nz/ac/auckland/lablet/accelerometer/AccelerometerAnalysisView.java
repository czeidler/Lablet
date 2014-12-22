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

        AccelerometerExperimentData data = (AccelerometerExperimentData)analysis.getData();

        // total data
        for (int i = 0 ; i < data.getTimeValues().size(); i++) {
            float total = (float) Math.sqrt(Math.pow(data.getXValues().get(i).floatValue(), 2)
                    + Math.pow(data.getYValues().get(i).floatValue(), 2)
                    + Math.pow(data.getZValues().get(i).floatValue(), 2));
            totalData.add(total);
        }

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

        XYDataAdapter totalDataAdapter = new XYDataAdapter(data.getTimeValues(), totalData);
        XYConcurrentPainter totalPainter = new XYConcurrentPainter(totalDataAdapter, getContext());
        Paint totalMarkerPaint = new Paint();
        totalMarkerPaint.setColor(Color.WHITE);
        totalPainter.getDrawConfig().setMarkerPaint(totalMarkerPaint);
        totalPainter.setPointRenderer(new CircleRenderer());
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
