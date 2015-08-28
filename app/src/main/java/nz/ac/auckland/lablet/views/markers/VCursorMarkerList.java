package nz.ac.auckland.lablet.views.markers;

import android.graphics.Point;
import android.graphics.PointF;

import nz.ac.auckland.lablet.data.PointDataList;

/**
 * Created by Jamie on 26/08/2015.
 */
class VCursorMarker extends CursorMarker {
    @Override
    protected float getDirection(PointF point) {
        return point.x;
    }

    @Override
    protected void offsetPoint(Point point, float offset) {
        point.x += offset;
    }

    @Override
    protected PointF getStartPoint() {
        return new PointF((int)getCachedScreenPosition().x, parent.getScreenRect().height());
    }

    @Override
    protected PointF getEndPoint() {
        return new PointF(getCachedScreenPosition().x, 0);
    }
}

public class VCursorMarkerList extends CursorMarkerList {

    public VCursorMarkerList(PointDataList data) {
        super(data);
    }

    @Override
    public void sort() {
        dataList.sortXAscending();
    }

    @Override
    protected DraggableMarker createMarkerForFrame(int frameId) {
        return new VCursorMarker();
    }
}
