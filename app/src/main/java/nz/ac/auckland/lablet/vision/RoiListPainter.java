/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.vision;

import nz.ac.auckland.lablet.experiment.FrameDataModel;
import nz.ac.auckland.lablet.views.marker.AbstractPointDataModel;
import nz.ac.auckland.lablet.views.marker.MarkerDataModel;
import nz.ac.auckland.lablet.views.marker.MarkerGroupTreePainter;
import nz.ac.auckland.lablet.views.plotview.IPlotPainter;
import nz.ac.auckland.lablet.vision.data.RoiData;
import nz.ac.auckland.lablet.vision.data.RoiDataList;


public class RoiListPainter extends MarkerGroupTreePainter {
    final private RoiDataList dataList;
    final private MarkerDataModel markerDataModel;
    final private FrameDataModel frameDataModel;

    final private RoiDataList.IListener<RoiDataList, RoiData> listener
            = new AbstractPointDataModel.IListener<RoiDataList, RoiData>() {
        @Override
        public void onDataAdded(RoiDataList dataList, int index) {
            addRoiPainter(dataList.getAt(index));
        }

        @Override
        public void onDataRemoved(RoiDataList model, int index, RoiData data) {
            for (IPlotPainter child : childList) {
                RoiPainter roiPainter = (RoiPainter)child;
                if (roiPainter.getRoiData().getFrameId() == data.getFrameId()) {
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

    public RoiListPainter(RoiDataList dataList, MarkerDataModel markerDataModel, FrameDataModel frameDataModel) {
        this.dataList = dataList;
        this.dataList.addListener(listener);
        this.markerDataModel = markerDataModel;
        this.frameDataModel = frameDataModel;

        for (int i = 0; i < dataList.size(); i++) {
            RoiData data = dataList.getAt(i);
            addRoiPainter(data);
        }
    }

    private void addRoiPainter(RoiData data) {
        addChild(new RoiPainter(new RoiModel(data), markerDataModel, frameDataModel));
    }
}

