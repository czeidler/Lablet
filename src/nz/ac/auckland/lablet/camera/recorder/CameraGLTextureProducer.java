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
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Sets up a texture for the camera stream and notifies listeners about new frames that arrived from the camera.
 */
public class CameraGLTextureProducer {
    public interface IListener {
        void onNewFrame();
    }

    final List<IListener> listeners = new ArrayList<>();
    final private int textureId;
    final private SurfaceTexture surfaceTexture;
    final private EGLContext sharedContext;


    public CameraGLTextureProducer(Camera camera) throws IOException {
        this.textureId = TextureCreator.create();
        this.surfaceTexture = new SurfaceTexture(textureId);
        this.surfaceTexture.setOnFrameAvailableListener(cameraInputListener);
        this.sharedContext = EGL14.eglGetCurrentContext();

        camera.stopPreview();
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
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
            for (IListener listener : listeners)
                listener.onNewFrame();
        }
    };
}
