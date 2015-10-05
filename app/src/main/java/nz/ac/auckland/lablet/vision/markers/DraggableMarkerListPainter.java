package nz.ac.auckland.lablet.vision.markers;

import android.graphics.PointF;

import nz.ac.auckland.lablet.views.marker.MarkerView;
import nz.ac.auckland.lablet.vision.data.Data;
import nz.ac.auckland.lablet.vision.data.DataList;
import nz.ac.auckland.lablet.vision.data.PointDataList;

/**
 * Abstract base class to draw a in a
 * {@link MarkerView}.
 */


public abstract class DraggableMarkerListPainter extends MarkerListPainter<PointDataList> {

    private DataList.IListener<PointDataList> dataListener = new DataList.IListener<PointDataList>() {

        @Override
        public void onDataAdded(PointDataList list, int index) {
            addMarker(index);
            containerView.invalidate();
        }

        @Override
        public void onDataRemoved(PointDataList list, int index, Data data) {
            removeMarker(index);
            containerView.invalidate();
        }

        @Override
        public void onDataChanged(PointDataList list, int index, int number) {
            rebuildPainterList(); //TODO: bug, if I don't call this then view doesn't update when setMarkerPosition called.
            containerView.invalidate();
        }

        @Override
        public void onAllDataChanged(PointDataList list) {
            rebuildPainterList();
            containerView.invalidate();
        }

        @Override
        public void onDataSelected(PointDataList list, int index) {
            if (getMarkerPainterGroup().selectOnDrag && index >= 0)
                painterList.get(index).setSelectedForDrag(true);

            containerView.invalidate();
        }
    };


    public class MarkerPainterGroup {
        private DraggableMarkerListPainter selectedDataListPainter = null;
        private IMarker selectedDataPainter = null;
        private boolean inSelectForDragMethod = false;
        private boolean selectOnDrag = false;

        public IMarker getSelectedDataPainter() {
            return selectedDataPainter;
        }

        public void deselect() {
            if (selectedDataPainter == null || selectedDataListPainter == null)
                return;
            selectedDataListPainter.containerView.invalidate();
            if (selectOnDrag)
                selectedDataListPainter.dataList.selectData(-1);

            selectedDataPainter.setSelectedForDrag(false);
            selectedDataListPainter = null;
            selectedDataPainter = null;
        }

        /**
         * Set if the Data should be marked as selected if the IDraggableDataPainter is selected for drag.
         *
         * @param selectOnDrag
         */
        public void setSelectOnDrag(boolean selectOnDrag) {
            this.selectOnDrag = selectOnDrag;
        }

        public void selectForDrag(IMarker marker, DraggableMarkerListPainter painter) {
            if (inSelectForDragMethod)
                return;
            inSelectForDragMethod = true;
            try {
                if (!marker.isSelectedForDrag()) {
                    // already deselected?
                    if (selectedDataListPainter == null)
                        return;

                    if (selectedDataListPainter == painter && selectedDataPainter == marker) {
                        if (selectedDataListPainter.containerView != null)
                            selectedDataListPainter.containerView.invalidate();
                        if (selectOnDrag)
                            selectedDataListPainter.dataList.selectData(-1);
                        selectedDataListPainter = null;
                        selectedDataPainter = null;
                    }
                    return;
                }
                // marker has been selected; deselect old marker
                if (selectedDataPainter != null && selectedDataPainter != marker) {
                    selectedDataPainter.setSelectedForDrag(false);
                    if (selectOnDrag)
                        selectedDataListPainter.dataList.selectData(-1);
                    if (selectedDataListPainter.containerView != null)
                        selectedDataListPainter.containerView.invalidate();
                }

                selectedDataListPainter = painter;
                selectedDataPainter = marker;
                if (selectOnDrag) {
                    int selectedIndex = selectedDataListPainter.getSelectableMarkerList().indexOf(selectedDataPainter);
                    selectedDataListPainter.dataList.selectData(selectedIndex);
                }
            } finally {
                inSelectForDragMethod = false;
            }
        }
    }

    private MarkerPainterGroup markerPainterGroup = new MarkerPainterGroup();

    public DraggableMarkerListPainter(PointDataList list) {
        super(list);
    }

    public MarkerPainterGroup getMarkerPainterGroup() {
        return markerPainterGroup;
    }

    public void setMarkerPainterGroup(MarkerPainterGroup markerPainterGroup) {
        this.markerPainterGroup = markerPainterGroup;
    }

    /**
     * Is called by a child marker.
     * <p>
     * Default implementation directly returns if marker is still be dragged, i.e., only on touch up events the marker
     * is moved. This circumvents some performance problems.
     * </p>
     *
     * @param marker that has been moved
     * @param newPosition the marker has been moved too
     */
    public void markerMoveRequest(DraggableMarker marker, PointF newPosition, boolean isDragging) {
        if (isDragging)
            return;

        int row = painterList.lastIndexOf(marker);
        if (row < 0)
            return;

        newPosition = containerView.sanitizeScreenPoint(newPosition);

        PointF newReal = new PointF();
        containerView.fromScreen(newPosition, newReal);
        dataList.setMarkerPosition(newReal, row);
    }


}
