/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera.recorder;


import java.io.IOException;

public interface IGLContextHost {
    interface IChild {
        /**
         * Notifies the client that the context host is ready. (Called from the GL thread)
         */
        void onContextReady() throws IOException;

        void setHost(IGLContextHost host);
        /**
         * Notifies the client that the requested context is now the current context. (Called from the GL thread)
         */
        void onRequestedContextIsCurrent();
    }

    /**
     * Setup a mutual client-host connection by calling the IChild.setHost method on the child.
     * 
     * @param child
     */
    void setChild(IChild child);

    /**
     * Request to make the context current. To be called by the child.
     *
     * If the context becomes current the IChild.onRequestedContextIsCurrent child method is called.
     */
    void requestContextCurrent();
}
