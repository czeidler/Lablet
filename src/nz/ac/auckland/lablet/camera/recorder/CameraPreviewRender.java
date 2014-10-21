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


public class CameraPreviewRender implements GLSurfaceView.Renderer {
    private TextureRender textureRender;

    private VideoRecorder videoRecorder;

    public CameraPreviewRender(VideoRecorder videoRecorder) {
        this.videoRecorder = videoRecorder;


    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        textureRender = new TextureRender();
        videoRecorder.setInputSurfaceTexture(textureRender.getTextureId());
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i2) {

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (videoRecorder.getCameraSurfaceTexture() == null)
            return;
        textureRender.drawFrame(videoRecorder.getCameraSurfaceTexture());
    }
}