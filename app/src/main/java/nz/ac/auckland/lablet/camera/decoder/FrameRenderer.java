/*
 * Copyright 2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera.decoder;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class FrameRenderer implements GLSurfaceView.Renderer {
    private TextureRender textureRender;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureID;
    private Surface mSurface;

    final private int videoRotation;

    final private Handler parentThreadHandler;
    final private Runnable surfaceReady;


    public FrameRenderer(int videoRotation, Runnable surfaceReady) {
        this.videoRotation = videoRotation;
        this.surfaceReady = surfaceReady;
        this.parentThreadHandler = new Handler();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // setup the texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkEglError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkEglError("glTexParameter");

        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurface = new Surface(mSurfaceTexture);

        parentThreadHandler.post(surfaceReady);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        gl10.glViewport(0, 0, width, height);
        // for a fixed camera, set the projection too
        float ratio = (float) width / height;
        gl10.glMatrixMode(GL10.GL_PROJECTION);
        gl10.glLoadIdentity();
        gl10.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

        textureRender = new TextureRender();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mSurfaceTexture.updateTexImage();

        textureRender.render(mTextureID, mSurfaceTexture, videoRotation);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public Surface getSurface() {
        return mSurface;
    }

    /**
     * Checks for EGL errors.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}
