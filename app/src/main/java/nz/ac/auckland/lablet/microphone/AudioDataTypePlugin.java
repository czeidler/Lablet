/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.microphone;

import nz.ac.auckland.lablet.experiment.IDataTypePlugin;
import nz.ac.auckland.lablet.experiment.ISensorData;


public class AudioDataTypePlugin implements IDataTypePlugin {
    @Override
    public String getDataType() {
        return AudioData.DATA_TYPE;
    }

    @Override
    public ISensorData instantiateData() {
        return new AudioData();
    }
}
