package nz.ac.auckland.lablet.camera.decoder;


import android.opengl.GLSurfaceView;
import nz.ac.auckland.lablet.camera.recorder.TextureRender;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class FrameRenderer implements GLSurfaceView.Renderer {
    private TextureRender textureRender;
    private CodecOutputSurface outputSurface;

    public FrameRenderer(CodecOutputSurface outputSurface) {
        this.outputSurface = outputSurface;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

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
        outputSurface.awaitNewImage();
        outputSurface.drawImage(true);

        textureRender.render(outputSurface.getTextureRender().getTextureId(), outputSurface.getSurfaceTexture(), 0);
    }
}
