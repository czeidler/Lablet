/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views.plotview;

import android.graphics.*;


public class LinearFitPainter extends AbstractPlotDataPainter {
    class Fit {
        private double b = 0;
        private double m = 0;

        public void fit(AbstractXYDataAdapter data) {
            double sumX = 0;
            double sumY = 0;
            double sumXX = 0;
            double sumXY = 0;
            int n = data.getSize();

            for (int i = 0; i < n; i++) {
                double x = data.getX(i).doubleValue();
                double y = data.getY(i).doubleValue();

                sumX += x;
                sumY += y;
                sumXX += x * x;
                sumXY += x * y;
            }

            m = (n * sumXY - sumX * sumY) / (n * sumXX - (sumX * sumX));
            b = (sumY - m * sumX) / n;
        }

        public double getB() {
            return b;
        }

        public double getM() {
            return m;
        }

        public String getLabel() {
            return "Linear Fit: b = " + String.format("%.2f", b) + ", m = " + String.format("%.2f", m);
        }
    }

    private Fit fit = new Fit();
    private Paint fitPaint = new Paint();
    private PointF lineStart = new PointF();
    private PointF lineEnd = new PointF();
    private PointF buffer = new PointF();
    private Paint labelPaint = new Paint();
    private Paint labelBackgroundPaint = new Paint();

    public LinearFitPainter() {
        fitPaint.setColor(Color.BLUE);
        labelPaint.setColor(Color.BLUE);
        labelBackgroundPaint.setColor(Color.argb(200, 200, 200, 200));
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        float labelMargin = 5;
        String label = fit.getLabel();
        float labelHeight = labelPaint.descent() - labelPaint.ascent();
        canvas.drawRect(labelMargin, labelMargin, labelMargin + labelPaint.measureText(label),
                labelMargin + labelHeight, labelBackgroundPaint);
        canvas.drawText(label, labelMargin, labelMargin - labelPaint.ascent(), labelPaint);

        RectF range = containerView.getRange();

        float yLeft = (float)(fit.getB() + fit.getM() * range.left);
        float yRight = (float)(fit.getB() + fit.getM() * range.right);

        buffer.set(range.left, yLeft);
        containerView.toScreen(buffer, lineStart);
        buffer.set(range.right, yRight);
        containerView.toScreen(buffer, lineEnd);

        canvas.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y, fitPaint);
    }

    private void fit() {
        fit.fit((AbstractXYDataAdapter)dataAdapter);
        invalidate();
    }

    @Override
    public void setDataAdapter(AbstractPlotDataAdapter adapter) {
        super.setDataAdapter(adapter);
        if (adapter != null)
            fit();
    }

    @Override
    protected AbstractPlotDataAdapter.IListener createListener() {
        return new AbstractPlotDataAdapter.IListener() {
            @Override
            public void onDataAdded(AbstractPlotDataAdapter plot, int index, int number) {
                fit();
            }

            @Override
            public void onDataRemoved(AbstractPlotDataAdapter plot, int index, int number) {
                fit();
            }

            @Override
            public void onDataChanged(AbstractPlotDataAdapter plot, int index, int number) {
                fit();
            }

            @Override
            public void onAllDataChanged(AbstractPlotDataAdapter plot) {
                fit();
            }
        };
    }
}


