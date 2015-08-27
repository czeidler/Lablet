package nz.ac.auckland.lablet.views.painters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import nz.ac.auckland.lablet.data.RectData;
import nz.ac.auckland.lablet.data.RectDataList;


/*
 *
 * Authors:
 *      Jamie Diprose <jdip004@aucklanduni.ac.nz>
 */


class RectDataPainter implements IDataPainter<RectData, RectDataListPainter> {

    private final float LINE_WIDTH_DP = 2f;
    private float LINE_WIDTH;
    protected RectDataListPainter parent = null;
    RectData data;

    @Override
    public void setTo(RectDataListPainter painter, RectData data) {
        this.parent = painter;
        this.data = data;
        LINE_WIDTH = parent.toPixel(LINE_WIDTH_DP);
    }

    @Override
    public void onDraw(Canvas canvas, float priority) {
        //RectData data = (RectData) this.data;

        if(data.isVisible())
        {
            // Line settings
            Paint paint = new Paint();
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStrokeWidth(LINE_WIDTH);
            paint.setAntiAlias(true);
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            int alpha = (int)(priority * 255.);
            paint.setAlpha(alpha);

            //Draw rotated rectangle
            canvas.save();
            canvas.rotate(data.getAngle());

            //Convert centre to screen position
            PointF centreScreen = new PointF();
            parent.getContainerView().toScreen(data.getCentre(), centreScreen);

            //Convert width and height to screen size, kinda hacky
            PointF size = new PointF();
            parent.getContainerView().toScreen(new PointF(data.getWidth(), data.getHeight()), size);

            float left = centreScreen.x -  size.x / 2;
            float right = centreScreen.x + size.x / 2;
            float top = centreScreen.y + size.y / 2;
            float bottom = centreScreen.y - size.y / 2;
            canvas.drawRect(left, top, right, bottom, paint);
            canvas.restore();
        }
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

public class RectDataListPainter extends DataListPainter<RectDataList> {

    public final int MAX_DISPLAYED_MARKERS = 100;

    public RectDataListPainter(RectDataList model) {
        super(model);
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getDataList().isVisible())
        {
            int currentFrame = dataList.getSelectedData();

            int start = currentFrame - MAX_DISPLAYED_MARKERS / 2 + 1;
            if (start < 0)
                start = 0;
            int end = currentFrame + MAX_DISPLAYED_MARKERS / 2 + 1;
            if (end > painterList.size())
                end = painterList.size();

            for (int i = start; i < end; i++) {
                IDataPainter marker = painterList.get(i);

                float runDistance = Math.abs(currentFrame - i);
                float currentPriority = (float)(0.35 - 0.1 * runDistance);
                if (currentPriority > 1.0)
                    currentPriority = (float)1.0;
                if (currentPriority < 0.1)
                    currentPriority = (float)0.1;

                marker.onDraw(canvas, currentPriority);
            }
        }
    }

    @Override
    protected IDataPainter createPainterForFrame(int frameId) {
        return new RectDataPainter();
    }
}