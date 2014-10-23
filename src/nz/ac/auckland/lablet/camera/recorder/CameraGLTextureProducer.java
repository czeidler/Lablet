/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera.recorder;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Sets up a texture for the camera stream and notifies listeners about new frames that arrived from the camera.
 */
public class CameraGLTextureProducer implements IGLContextHost.IChild {
    public interface IListener {
        void onNewFrame();
    }

    final List<IListener> listeners = new ArrayList<>();
    private int textureId;
    private SurfaceTexture surfaceTexture;
    private EGLContext sharedContext;
    private Camera camera;

    private IGLContextHost host;

    public CameraGLTextureProducer(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void onContextReady() throws IOException {
        textureId = TextureCreator.create();
        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(cameraInputListener);
        sharedContext = EGL14.eglGetCurrentContext();

        camera.stopPreview();
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    @Override
    public void setHost(IGLContextHost host) {
        this.host = host;
    }

    @Override
    public void onRequestedContextIsCurrent() {
        if (surfaceTexture != null)
            surfaceTexture.updateTexImage();
    }

    public EGLContext getSharedContext() {
        return sharedContext;
    }

    public int getGLTextureId() {
        return textureId;
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void addListener(IListener listener) {
        listeners.add(listener);
    }

    private SurfaceTexture.OnFrameAvailableListener cameraInputListener
            = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            host.requestContextCurrent();

            for (IListener listener : listeners)
                listener.onNewFrame();
        }
    };
}
