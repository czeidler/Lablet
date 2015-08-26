package nz.ac.auckland.lablet.views.painters;

import android.graphics.Point;
import android.graphics.PointF;

import nz.ac.auckland.lablet.data.PointDataList;

class HCursorDataPainter extends CursorDataPainter {
    @Override
    protected float getDirection(PointF point) {
        return point.y;
    }

    @Override
    protected void offsetPoint(Point point, float offset) {
        point.y += offset;
    }

    @Override
    protected PointF getStartPoint() {
        return new PointF(0, getCachedScreenPosition().y);
    }

    @Override
    protected PointF getEndPoint() {
        return new PointF(parent.getScreenRect().width(), getCachedScreenPosition().y);
    }
}

public class HCursorDataListPainter extends CursorDataListPainter {

    public HCursorDataListPainter(PointDataList data) {
        super(data);
    }

    @Override
    public void sort() {
        dataList.sortYAscending();
    }

    @Override
    protected DraggableDataPainter createPainterForFrame(int frameId) {
        return new HCursorDataPainter();
    }
}