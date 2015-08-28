package nz.ac.auckland.lablet.views.markers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.data.PointDataList;
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
    private final int FONT_SIZE_DP = 20;
    private final float LINE_WIDTH_DP = 2f;
    private final float WING_LENGTH_DP = 10;

    // pixel sizes, set in the constructor
    private int FONT_SIZE;
    private float LINE_WIDTH;
    private float WING_LENGTH;
    private boolean isVisible = true;

    private PointMarker topLeftTag = new PointMarker();
    private PointMarker topRightTag = new PointMarker();
    private PointMarker btmRightTag = new PointMarker();
    private PointMarker btmLeftTag = new PointMarker();
    protected RoiMarkerList parent = null;
    private RoiData data;
    DraggableMarkerList dataListPainter;
    PointDataList dataList = new PointDataList();


    @Override
    public void setTo(RoiMarkerList painter, final RoiData data) {
        this.parent = painter;
        this.data = data;

        dataList.addData(data.getTopLeft());
        dataList.addData(data.getTopRight());
        dataList.addData(data.getBtmRight());
        dataList.addData(data.getBtmLeft());

        dataListPainter = new DraggableMarkerList(dataList) {
            @Override
            protected IMarker createMarkerForFrame(int frameId) {
                if(frameId == 0)
                {
                    return topLeftTag;
                }
                else if(frameId == 1)
                {
                    return topRightTag;
                }
                else if(frameId == 2)
                {
                    return btmRightTag;
                }
                else if(frameId == 3)
                {
                    return btmLeftTag;
                }

                return null;
            }

            @Override
            public void onDraw(Canvas canvas) {
                for (IMarker marker : painterList)
                    marker.onDraw(canvas, 1);
            }

            protected void onDraggedTo(DraggableMarker marker, PointF newPosition)
            {
                //int index = painterList.indexOf(marker);
                //PointDataList model  = this.getDataList();

                PointF newPositionReal = new PointF();
                containerView.fromScreen(newPosition, newPositionReal);

                if(marker == topLeftTag) {
                    PointF btmRightScreen = btmRightTag.getCachedScreenPosition();
                    PointF btmRightReal = new PointF();
                    containerView.fromScreen(btmRightScreen, btmRightReal);

                    data.setTopRight(new PointF(btmRightReal.x, newPositionReal.y));
                    data.setBtmLeft(new PointF(newPositionReal.x, btmRightReal.y));

                } else if(marker == topRightTag) {
                    PointF btmLeftScreen = btmLeftTag.getCachedScreenPosition();
                    PointF btmLeftReal = new PointF();
                    containerView.fromScreen(btmLeftScreen, btmLeftReal);

                    data.setTopLeft(new PointF(btmLeftReal.x, newPositionReal.y));
                    data.setBtmRight(new PointF(newPositionReal.x, btmLeftReal.y));
                } else if(marker == btmLeftTag) {
                    PointF topRightScreen = topRightTag.getCachedScreenPosition();
                    PointF topRightReal = new PointF();
                    containerView.fromScreen(topRightScreen, topRightReal);

                    data.setTopLeft( new PointF(newPositionReal.x, topRightReal.y));
                    data.setBtmRight(new PointF(topRightReal.x, newPositionReal.y));
                } else if (marker == btmRightTag) {
                    PointF topLeftScreen = topLeftTag.getCachedScreenPosition();
                    PointF topLeftReal = new PointF();
                    containerView.fromScreen(topLeftScreen, topLeftReal);

                    data.setTopRight(new PointF(newPositionReal.x, topLeftReal.y));
                    data.setBtmLeft(new PointF(topLeftReal.x, newPositionReal.y));
                }
            }
        };

//        topLeftTag.setTo(dataListPainter, data.getTopLeft());
//        topRightTag.setTo(dataListPainter, data.getTopRight());
//        btmRightTag.setTo(dataListPainter, data.getBtmRight());
//        btmLeftTag.setTo(dataListPainter, data.getBtmLeft());

        dataListPainter.setContainer(this.parent.getContainerView());

        FONT_SIZE = parent.toPixel(FONT_SIZE_DP);
        LINE_WIDTH = parent.toPixel(LINE_WIDTH_DP);
        WING_LENGTH = parent.toPixel(WING_LENGTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        //dataListPainter.onDraw(canvas);

        float left;
        float top;
        float right;
        float bottom;

        if(btmLeftTag.isSelectedForDrag() || topRightTag.isSelectedForDrag())
        {
            PointF topRight = topRightTag.getCachedScreenPosition();
            PointF btmLeft = btmLeftTag.getCachedScreenPosition();
            left = btmLeft.x;
            top = topRight.y;
            right = topRight.x;
            bottom = btmLeft.y;
        }
        else
        {
            PointF topLeft = topLeftTag.getCachedScreenPosition();
            PointF btmRight = btmRightTag.getCachedScreenPosition();
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
    }

    @Override
    public boolean handleActionDown(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean handleActionUp(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean handleActionMove(MotionEvent ev) {
        return false;
    }

    @Override
    public void setSelectedForDrag(boolean selectedForDrag) {

    }

    @Override
    public boolean isSelectedForDrag() {
        return false;
    }

    @Override
    public void invalidate() {

    }


}

public class RoiMarkerList extends MarkerList<RoiDataList> {

    public RoiMarkerList(RoiDataList model) {
        super(model);
    }

    @Override
    public void setContainer(PlotPainterContainerView view) {
        super.setContainer(view);

        if (view == null)
            return;
    }

    @Override
    protected RoiMarker createMarkerForFrame(int frameId) {
        return new RoiMarker();
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getDataList().isVisible())
        {
            for (IMarker marker : painterList)
                marker.onDraw(canvas, 1);
        }
    }
}
