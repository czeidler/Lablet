/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera.recorder;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.IOException;


public class CameraPreviewRender implements GLSurfaceView.Renderer, IGLContextHost{
    private CameraGLTextureProducer producer;
    private TextureRender textureRender;
    private GLSurfaceView parent;
    private IChild child;

    public CameraPreviewRender(GLSurfaceView view, CameraGLTextureProducer producer) {
        this.producer = producer;
        this.parent = view;

        setChild(producer);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {
        try {
            child.onContextReady();
        } catch (IOException e) {
            e.printStackTrace();
        }
        textureRender = new TextureRender();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        child.onRequestedContextIsCurrent();

        textureRender.render(producer.getGLTextureId(), producer.getSurfaceTexture());
    }

    @Override
    public void setChild(IChild child) {
        this.child = child;
        child.setHost(this);
    }

    @Override
    public void requestContextCurrent() {
        parent.requestRender();
    }
}
