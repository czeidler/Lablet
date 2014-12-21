/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


public class IntegralView extends FrameLayout {

    public IntegralView(Context context, AccelerometerAnalysis analysis) {
        super(context);

        setBackgroundColor(Color.BLACK);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.accelerometer_integral, null, false);
        addView(view);

        AccelerometerExperimentData data = (AccelerometerExperimentData)analysis.getData();

        float baseLine = analysis.getBaseLineMarker().getMarkerDataAt(0).getPosition().y;

        int start = 0;
        int end = data.getTimeValues().size() - 1;
        List<Number> timeValues = data.getTimeValues().subList(start, end);
        List<Number> xIntegral = integral(timeValues, data.getXValues(), start, end, baseLine);
        List<Number> xIntegral2 = integral(timeValues, xIntegral, start, end - 1, 0);

        List<Number> yIntegral = integral(timeValues, data.getYValues(), start, end, baseLine);
        List<Number> yIntegral2 = integral(timeValues, yIntegral, start, end - 1, 0);

        List<Number> zIntegral = integral(timeValues, data.getZValues(), start, end, baseLine);
        List<Number> zIntegral2 = integral(timeValues, zIntegral, start, end - 1, 0);

        PlotView xAccelerometerPlotView = (PlotView)view.findViewById(R.id.xAccelerometerPlotView);
        PlotView xVelocityPlotView = (PlotView)view.findViewById(R.id.xVelocityPlotView);
        PlotView xDistancePlotView = (PlotView)view.findViewById(R.id.xDistancePlotView);

        PlotView yAccelerometerPlotView = (PlotView)view.findViewById(R.id.yAccelerometerPlotView);
        PlotView yVelocityPlotView = (PlotView)view.findViewById(R.id.yVelocityPlotView);
        PlotView yDistancePlotView = (PlotView)view.findViewById(R.id.yDistancePlotView);

        PlotView zAccelerometerPlotView = (PlotView)view.findViewById(R.id.zAccelerometerPlotView);
        PlotView zVelocityPlotView = (PlotView)view.findViewById(R.id.zVelocityPlotView);
        PlotView zDistancePlotView = (PlotView)view.findViewById(R.id.zDistancePlotView);

        setupPlot(xAccelerometerPlotView, timeValues, data.getXValues(), "x-Acceleration", "a [m/s^2]");
        setupPlot(xVelocityPlotView, timeValues, xIntegral, "x-Velocity", "v [m/s]");
        setupPlot(xDistancePlotView, timeValues, xIntegral2, "x-Distance", "s [m]");

        setupPlot(yAccelerometerPlotView, timeValues, data.getYValues(), "y-Acceleration", "a [m/s^2]");
        setupPlot(yVelocityPlotView, timeValues, yIntegral, "y-Velocity", "v [m/s]");
        setupPlot(yDistancePlotView, timeValues, yIntegral2, "y-Distance", "s [m]");

        setupPlot(zAccelerometerPlotView, timeValues, data.getZValues(), "z-Acceleration", "a [m/s^2]");
        setupPlot(zVelocityPlotView, timeValues, zIntegral, "z-Velocity", "v [m/s]");
        setupPlot(zDistancePlotView, timeValues, zIntegral2, "z-Distance", "s [m]");
    }

    private void setupPlot(PlotView plotView, List<Number> x, List<Number> y, String title, String yAxisTitle) {
        plotView.getTitleView().setTitle(title);
        plotView.getYAxisView().setTitle(yAxisTitle);

        StrategyPainter strategyPainter = new BufferedDirectStrategyPainter();

        XYDataAdapter zData = new XYDataAdapter(x, y);
        XYConcurrentPainter painter = new XYConcurrentPainter(zData, getContext());
        strategyPainter.addChild(painter);

        plotView.addPlotPainter(strategyPainter);

        plotView.autoZoom();
    }

    private List<Number> integral(List<Number> xValues, List<Number> yValues, int start, int count, float c) {
        List<Number> integral = new ArrayList<>();
        float sum = 0;
        for (int i = start; i < start + count - 1; i++) {
            float deltaX = (xValues.get(i + 1).floatValue() - xValues.get(i).floatValue()) / 1000;
            float y = (yValues.get(i + 1).floatValue() + yValues.get(i).floatValue()) / 2 - c;
            sum += deltaX * y;
            integral.add(sum);
        }
        return integral;
    }
}
