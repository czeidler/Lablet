package nz.ac.auckland.lablet.views.markers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ViewParent;

import java.util.ArrayList;

import nz.ac.auckland.lablet.data.RoiData;
import nz.ac.auckland.lablet.data.RoiDataList;
import nz.ac.auckland.lablet.views.plotview.PlotPainterContainerView;


/*
 *
 * Authors:
 *      Jamie Diprose <jdip004@aucklanduni.ac.nz>
 */


/**
 * Marker for region of interest.
 */
class RoiMarker implements IMarker<RoiData, RoiMarkerList> {

    // device independent sizes:
    private final float LINE_WIDTH_DP = 2f;

    // pixel sizes, set in the constructor
    private float LINE_WIDTH;
    private float MIN_WIDTH = 1;
    private float MIN_HEIGHT = 1;

    private PointMarker centreMarker = new PointMarker();
    private PointMarker topLeftMarker = new PointMarker();
    private PointMarker topRightMarker = new PointMarker();
    private PointMarker btmRightMarker = new PointMarker();
    private PointMarker btmLeftMarker = new PointMarker();
    ArrayList<PointMarker> markers;
    protected RoiMarkerList parent = null;
    private RoiData data;

    private DraggableMarker.IListener dragListener = new DraggableMarker.IListener() {

        @Override
        public void onDraggedTo(DraggableMarker marker, PointF newPosition) {
            PointF newPositionReal = new PointF();
            parent.getContainerView().fromScreen(newPosition, newPositionReal);

            if(marker == centreMarker)
            {
                float width = data.getWidth() / 2;
                float height = data.getHeight() / 2;

                data.getTopLeft().setPosition(new PointF(newPositionReal.x-width, newPositionReal.y + height));
                data.getTopRight().setPosition(new PointF(newPositionReal.x+width, newPositionReal.y + height));
                data.getBtmRight().setPosition(new PointF(newPositionReal.x+width, newPositionReal.y - height));
                data.getBtmLeft().setPosition(new PointF(newPositionReal.x-width, newPositionReal.y - height));
                data.setCentre(newPositionReal);

                for (IMarker m : markers) {
                    if(m != centreMarker) {
                        m.invalidate();
                    }
                }

            } else if (marker == topLeftMarker) {
                PointF btmRightScreen = btmRightMarker.getCachedScreenPosition();
                PointF btmRightReal = new PointF();
                parent.getContainerView().fromScreen(btmRightScreen, btmRightReal);

                data.setTopLeft(newPositionReal);
                data.setTopRight(new PointF(btmRightReal.x, newPositionReal.y));
                data.setBtmLeft(new PointF(newPositionReal.x, btmRightReal.y));
                //data.setCentre(getCentre());

                topRightMarker.invalidate();
                btmLeftMarker.invalidate();

                data.setCentre(new PointF(newPositionReal.x + data.getWidth() / 2, newPositionReal.y - data.getHeight() / 2));
                centreMarker.invalidate();

            } else if(marker == topRightMarker) {
                PointF btmLeftScreen = btmLeftMarker.getCachedScreenPosition();
                PointF btmLeftReal = new PointF();
                parent.getContainerView().fromScreen(btmLeftScreen, btmLeftReal);

                data.setTopRight(newPositionReal);
                data.setTopLeft(new PointF(btmLeftReal.x, newPositionReal.y));
                data.setBtmRight(new PointF(newPositionReal.x, btmLeftReal.y));

                topLeftMarker.invalidate();
                btmRightMarker.invalidate();

                data.setCentre(new PointF(data.getTopLeft().getPosition().x + data.getWidth() / 2, data.getTopLeft().getPosition().y - data.getHeight() / 2));
                centreMarker.invalidate();

            } else if(marker == btmLeftMarker) {
                PointF topRightScreen = topRightMarker.getCachedScreenPosition();
                PointF topRightReal = new PointF();
                parent.getContainerView().fromScreen(topRightScreen, topRightReal);

                data.setBtmLeft(newPositionReal);
                data.setTopLeft(new PointF(newPositionReal.x, topRightReal.y));
                data.setBtmRight(new PointF(topRightReal.x, newPositionReal.y));
                topLeftMarker.invalidate();
                btmRightMarker.invalidate();

                data.setCentre(new PointF(data.getTopLeft().getPosition().x + data.getWidth() / 2, data.getTopLeft().getPosition().y - data.getHeight() / 2));
                centreMarker.invalidate();

            } else if (marker == btmRightMarker) {
                PointF topLeftScreen = topLeftMarker.getCachedScreenPosition();
                PointF topLeftReal = new PointF();
                parent.getContainerView().fromScreen(topLeftScreen, topLeftReal);

                data.setBtmRight(newPositionReal);
                data.setTopRight(new PointF(newPositionReal.x, topLeftReal.y));
                data.setBtmLeft(new PointF(topLeftReal.x, newPositionReal.y));
                topRightMarker.invalidate();
                btmLeftMarker.invalidate();

                data.setCentre(new PointF(data.getTopLeft().getPosition().x + data.getWidth() / 2, data.getTopLeft().getPosition().y - data.getHeight() / 2));
                centreMarker.invalidate();
            }
        }

        @Override
        public void onSelectedForDrag(DraggableMarker marker, boolean isSelected) {
            if(isSelected) {
                for (IMarker marker2 : markers) {
                    if(marker2 != marker)
                    {
                        marker2.setSelectedForDrag(false);
                    }
                }
            }
            else
            {
                parent.getContainerView().invalidate();
            }
        }
    };

    @Override
    public void setTo(RoiMarkerList painter, final RoiData data) {
        this.parent = painter;
        this.data = data;

        topLeftMarker.setTo(painter.getContainerView(), data.getTopLeft());
        topRightMarker.setTo(painter.getContainerView(), data.getTopRight());
        btmRightMarker.setTo(painter.getContainerView(), data.getBtmRight());
        btmLeftMarker.setTo(painter.getContainerView(), data.getBtmLeft());
        centreMarker.setTo(painter.getContainerView(), data.getCentre());

        //btmRightMarker.data

        topLeftMarker.addListener(dragListener);
        topRightMarker.addListener(dragListener);
        btmRightMarker.addListener(dragListener);
        btmLeftMarker.addListener(dragListener);
        centreMarker.addListener(dragListener);

        markers = new ArrayList<>();
        markers.add(topLeftMarker);
        markers.add(topRightMarker);
        markers.add(btmRightMarker);
        markers.add(btmLeftMarker);
        markers.add(centreMarker);

        LINE_WIDTH = parent.getContainerView().toPixel(LINE_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        float left;
        float top;
        float right;
        float bottom;

        if(btmLeftMarker.isSelectedForDrag() || topRightMarker.isSelectedForDrag())
        {
            PointF topRight = topRightMarker.getCachedScreenPosition();
            PointF btmLeft = btmLeftMarker.getCachedScreenPosition();
            left = btmLeft.x;
            top = topRight.y;
            right = topRight.x;
            bottom = btmLeft.y;
        }
        else
        {
            PointF topLeft = topLeftMarker.getCachedScreenPosition();
            PointF btmRight = btmRightMarker.getCachedScreenPosition();
            left = topLeft.x;
            top = topLeft.y;
            right = btmRight.x;
            bottom = btmRight.y;
        }

        // Line settings
        Paint paint = new Paint();
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(LINE_WIDTH);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(left, top, right, bottom, paint); //See android.Graphics.Rect constructor for meaning of params

        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(left, top, 5, paint);
        canvas.drawCircle(left, bottom, 5, paint);
        canvas.drawCircle(right, top, 5, paint);
        canvas.drawCircle(right, bottom, 5, paint);

        for (IMarker marker : markers) {
            if(marker.isSelectedForDrag() || marker == centreMarker) {
                marker.onDraw(canvas, 1);
            }
        }
    }

    @Override
    public boolean handleActionDown(MotionEvent ev) {
        boolean handled = false;

        for (IMarker marker : markers) {
            if (marker.handleActionDown(ev)) {
                handled = true;
                parent.getContainerView().invalidate();
                break;
            }
        }

        if (handled) {
            ViewParent parent = this.parent.getContainerView().getParent();
            if (parent != null)
                parent.requestDisallowInterceptTouchEvent(true);
        }

        return handled;
    }

    @Override
    public boolean handleActionUp(MotionEvent ev) {
        boolean handled = false;

        for (IMarker marker : markers) {
            if (marker.handleActionUp(ev)) {
                handled = true;
                parent.getContainerView().invalidate();
                break;
            }
        }

        return handled;
    }

    @Override
    public boolean handleActionMove(MotionEvent ev) {
        boolean handled = false;

        for (IMarker marker : markers) {
            if (marker.handleActionMove(ev)) {
                handled = true;
                parent.getContainerView().invalidate();
                break;
            }
        }

        return handled;
    }

    @Override
    public void setSelectedForDrag(boolean selectedForDrag) {
        int i = 0;
    }

    @Override
    public boolean isSelectedForDrag() {
        return false;
    }

    @Override
    public void invalidate() {
        int i = 0;
    }
}

public class RoiMarkerList extends MarkerList<RoiDataList> {

    public RoiMarkerList(RoiDataList model) {
        super(model);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);
    }

    @Override
    protected RoiMarker createMarkerForFrame(int frameId) {
        return new RoiMarker();
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getDataList().isVisible())
        {
            int markerIndex = dataList.getSelectedData();

            if(markerIndex != -1) {
                IMarker marker = getMarker(markerIndex);
                marker.onDraw(canvas, 1);
            }
        }
    }
}
