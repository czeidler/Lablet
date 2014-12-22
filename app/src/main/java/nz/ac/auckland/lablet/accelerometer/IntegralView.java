/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.accelerometer;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import nz.ac.auckland.lablet.R;
import nz.ac.auckland.lablet.experiment.MarkerData;
import nz.ac.auckland.lablet.experiment.MarkerDataModel;
import nz.ac.auckland.lablet.views.HCursorDataModelPainter;
import nz.ac.auckland.lablet.views.VCursorDataModelPainter;
import nz.ac.auckland.lablet.views.plotview.*;

import java.util.ArrayList;
import java.util.List;


public class IntegralView extends FrameLayout {
    final private AccelerometerAnalysis analysis;
    final private List<SpinnerEntry> spinnerEntryList = new ArrayList<>();
    private PlotView accelerometerPlotView;
    private PlotView velocityPlotView;
    private PlotView distancePlotView;
    private List<Number> timeValues;
    private SpinnerEntry currentEntry;
    int start;
    int end;

    class SpinnerEntry {
        final public String name;
        final public AccelerometerAnalysis.Calibration calibration;
        final public List<Number> data;
        final public String labelPrefix;

        public SpinnerEntry(String name, AccelerometerAnalysis.Calibration calibration, List<Number> data,
                            String labelPrefix) {
            this.name = name;
            this.calibration = calibration;
            this.data = data;
            this.labelPrefix = labelPrefix;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    public IntegralView(Context context, AccelerometerAnalysis analysis) {
        super(context);
        this.analysis = analysis;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.accelerometer_integral, null, false);
        addView(view);
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorBackground,});
        int backGroundColor = array.getColor(0, 0xFF00FF);
        view.setBackgroundColor(backGroundColor);

        AccelerometerExperimentData data = (AccelerometerExperimentData)analysis.getData();

        Spinner spinner = (Spinner)view.findViewById(R.id.spinner);
        spinnerEntryList.add(new SpinnerEntry("x-Acceleration", analysis.getXCalibration(), data.getXValues(), "x"));
        spinnerEntryList.add(new SpinnerEntry("y-Acceleration", analysis.getYCalibration(), data.getYValues(), "y"));
        spinnerEntryList.add(new SpinnerEntry("z-Acceleration", analysis.getZCalibration(), data.getZValues(), "z"));
        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item,
                spinnerEntryList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectData(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner.setSelection(2);

        accelerometerPlotView = (PlotView)view.findViewById(R.id.accelerometerPlotView);
        velocityPlotView = (PlotView)view.findViewById(R.id.velocityPlotView);
        distancePlotView = (PlotView)view.findViewById(R.id.distancePlotView);
    }

    private MarkerDataModel.IListener dataListener = new MarkerDataModel.IListener() {
        @Override
        public void onDataAdded(MarkerDataModel model, int index) {

        }

        @Override
        public void onDataRemoved(MarkerDataModel model, int index, MarkerData data) {

        }

        @Override
        public void onDataChanged(MarkerDataModel model, int index, int number) {
            updateIntegral();
        }

        @Override
        public void onAllDataChanged(MarkerDataModel model) {

        }

        @Override
        public void onDataSelected(MarkerDataModel model, int index) {

        }
    };

    private void selectData(int index) {
        accelerometerPlotView.clear();

        if (currentEntry != null)
            currentEntry.calibration.getBaseLineMarker().removeListener(dataListener);

        AccelerometerExperimentData data = (AccelerometerExperimentData)analysis.getData();
        currentEntry = spinnerEntryList.get(index);

        final AccelerometerAnalysis.Calibration calibration = currentEntry.calibration;
        final String prefix = currentEntry.labelPrefix;

        start = 0;
        end = data.getTimeValues().size() - 1;
        timeValues = data.getTimeValues().subList(start, end);
        setupPlot(accelerometerPlotView, timeValues, currentEntry.data, prefix + "-Acceleration", "a [m/s^2]");

        float baseLine = calibration.getBaseLine();
        NormRectF normRectF = new NormRectF(accelerometerPlotView.getRange());
        if (baseLine < normRectF.getTop() || baseLine > normRectF.getBottom())
            calibration.setBaseLine((normRectF.getBottom() + normRectF.getTop()) / 2);

        updateIntegral();

        // marker
        MarkerDataModel baseLineMarker = calibration.getBaseLineMarker();
        HCursorDataModelPainter hCursorDataModelPainter = new HCursorDataModelPainter(baseLineMarker);
        accelerometerPlotView.addPlotPainter(hCursorDataModelPainter);

        baseLineMarker.addListener(dataListener);
    }

    private void updateIntegral() {
        if (currentEntry == null)
            return;

        velocityPlotView.clear();
        distancePlotView.clear();

        final AccelerometerAnalysis.Calibration calibration = currentEntry.calibration;
        final String prefix = currentEntry.labelPrefix;

        List<Number> integral = integral(timeValues, currentEntry.data, start, end, calibration.getBaseLine());
        List<Number> integral2 = integral(timeValues, integral, start, end - 1, 0);

        setupPlot(velocityPlotView, timeValues, integral, prefix + "-Velocity", "v [m/s]");
        setupPlot(distancePlotView, timeValues, integral2, prefix + "-Distance", "s [m]");


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

        plotView.invalidate();
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
