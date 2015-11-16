/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import nz.ac.auckland.lablet.views.marker.MarkerGroupTreePainter;
import nz.ac.auckland.lablet.views.plotview.IPlotPainter;
import nz.ac.auckland.lablet.vision.data.Data;
import nz.ac.auckland.lablet.vision.data.DataList;
import nz.ac.auckland.lablet.vision.data.RoiData;
import nz.ac.auckland.lablet.vision.data.RoiDataList;


public class RoiListPainter extends MarkerGroupTreePainter {
    final private RoiDataList dataList;

    final private RoiDataList.IListener<RoiDataList> listener = new DataList.IListener<RoiDataList>() {
        @Override
        public void onDataAdded(RoiDataList dataList, int index) {
            addRoiPainter(dataList.getDataAt(index));
        }

        @Override
        public void onDataRemoved(RoiDataList dataList, int index, Data data) {
            RoiData roiData = (RoiData)data;
            for (IPlotPainter child : childList) {
                RoiPainter roiPainter = (RoiPainter)child;
                if (roiPainter.getRoiData().getFrameId() == roiData.getFrameId()) {
                    removeChild(roiPainter);
                    break;
                }
            }
        }

        @Override
        public void onDataChanged(RoiDataList dataList, int index, int number) {

        }

        @Override
        public void onAllDataChanged(RoiDataList dataList) {

        }

        @Override
        public void onDataSelected(RoiDataList dataList, int index) {

        }
    };

    public RoiListPainter(RoiDataList dataList) {
        this.dataList = dataList;
        this.dataList.addListener(listener);

        for (int i = 0; i < dataList.size(); i++) {
            RoiData data = dataList.getDataAt(i);
            addRoiPainter(data);
        }
    }

    private void addRoiPainter(RoiData data) {
        addChild(new RoiPainter(new RoiModel(data)));
    }
}
