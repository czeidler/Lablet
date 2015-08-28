package nz.ac.auckland.lablet.views.markers;

import android.graphics.Point;
import android.graphics.PointF;

import nz.ac.auckland.lablet.data.PointDataList;

class HCursorMarker extends CursorMarker {
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

public class HCursorMarkerList extends CursorMarkerList {

    public HCursorMarkerList(PointDataList data) {
        super(data);
    }

    @Override
    public void sort() {
        dataList.sortYAscending();
    }

    @Override
    protected DraggableMarker createMarkerForFrame(int frameId) {
        return new HCursorMarker();
    }
}