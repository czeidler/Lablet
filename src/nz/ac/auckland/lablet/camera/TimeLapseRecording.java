/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.*;
import android.opengl.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


// Fills the surfaceTexture with video frames
// the texture has to be create using GL
class CameraTextureSource {
    final private SurfaceTexture surfaceTexture;

    public CameraTextureSource(Camera camera, int targetGLTextureId,
                               SurfaceTexture.OnFrameAvailableListener inputListener) throws IOException {
        this.surfaceTexture = new SurfaceTexture(targetGLTextureId);
        this.surfaceTexture.setOnFrameAvailableListener(inputListener);

        camera.stopPreview();
        camera.setPreviewTexture(surfaceTexture);
        camera.startPreview();
    }

    public void updateTexImage() {
        surfaceTexture.updateTexImage();
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }
}


/**
 * Code for rendering a texture onto a surface using OpenGL ES 2.0.
 */
class STextureRender {
    private static final String TAG = "STextureRender";

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f,  1.0f, 0, 0.f, 1.f,
            1.0f,  1.0f, 0, 1.f, 1.f,
    };

    private FloatBuffer mTriangleVertices;

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
                    "    vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +      // highp here doesn't seem to matter
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID = -12345;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    public STextureRender() {
        mTriangleVertices = ByteBuffer.allocateDirect(
                mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTriangleVertices.put(mTriangleVerticesData).position(0);

        Matrix.setIdentityM(mSTMatrix, 0);

        surfaceCreated();
    }

    public int getTextureId() {
        return mTextureID;
    }

    public void drawFrame(SurfaceTexture st) {
        checkGlError("onDrawFrame start");
        st.getTransformMatrix(mSTMatrix);

        // (optional) clear to green so we can see if we're failing to set pixels
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);
        checkGlError("glUseProgram");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        checkGlError("glEnableVertexAttribArray maPositionHandle");

        mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
        checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        checkGlError("glEnableVertexAttribArray maTextureHandle");

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        checkGlError("glDrawArrays");

        // IMPORTANT: on some devices, if you are sharing the external texture between two
        // contexts, one context may not see updates to the texture unless you un-bind and
        // re-bind it.  If you're not using shared EGL contexts, you don't need to bind
        // texture 0 here.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
    }

    /**
     * Initializes GL state.  Call this after the EGL surface has been created and made current.
     */
    public void surfaceCreated() {
        mProgram = createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        if (mProgram == 0) {
            throw new RuntimeException("failed creating program");
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkLocation(maPositionHandle, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkLocation(maTextureHandle, "aTextureCoord");

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkLocation(muMVPMatrixHandle, "uMVPMatrix");
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkLocation(muSTMatrixHandle, "uSTMatrix");

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":");
            Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: ");
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    public static void checkLocation(int location, String label) {
        if (location < 0) {
            throw new RuntimeException("Unable to locate '" + label + "' in program");
        }
    }
}

/**
 * Holds state associated with a Surface used for MediaCodec encoder input.
 * <p>
 * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses
 * that to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to
 * be sent to the video encoder.
 * <p>
 * This object owns the Surface -- releasing this will release the Surface too.
 */
class CodecInputSurface {
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

    private Surface mSurface;

    /**
     * Creates a CodecInputSurface from a Surface.
     */
    public CodecInputSurface(Surface surface) {
        if (surface == null) {
            throw new NullPointerException();
        }
        mSurface = surface;

        eglSetup();
    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
     */
    private void eglSetup() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw new RuntimeException("unable to initialize EGL14");
        }

        // Configure EGL for recording and OpenGL ES 2.0.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                numConfigs, 0);
        checkEglError("eglCreateContext RGB888+recordable ES2");

        // Configure context for OpenGL ES 2.0.
        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                attrib_list, 0);
        checkEglError("eglCreateContext");

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface,
                surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
    }

    /**
     * Discards all resources held by this class, notably the EGL context.  Also releases the
     * Surface that was passed to our constructor.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }
        mSurface.release();

        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLSurface = EGL14.EGL_NO_SURFACE;

        mSurface = null;
    }

    /**
     * Makes our EGL context and surface current.
     */
    public void makeCurrent() {
        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
        checkEglError("eglMakeCurrent");
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     */
    public boolean swapBuffers() {
        boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        checkEglError("eglSwapBuffers");
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
        checkEglError("eglPresentationTimeANDROID");
    }

    /**
     * Checks for EGL errors.  Throws an exception if one is found.
     */
    private void checkEglError(String msg) {
        int error;
        if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
            throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
        }
    }
}


//camera draws to SurfaceTexture (assosiated with a gl texture) -> frame available listener
// -> that updates the gl texture -> glswap tells encoder to onNewFrame
public class TimeLapseRecording {
    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames

    private Camera camera;
    private CamcorderProfile cameraProfile;

    private STextureRender textureRender;
    private CameraTextureSource cameraTextureSource;
    private CodecInputSurface codecInputSurface;
    private MediaCodec encoder;
    private Surface encoderInputSurface;
    private MediaMuxer muxer;
    private boolean muxerStarted = false;
    private int trackIndex;
    private Looper looper = null;
    private Handler handler = null;

    private Object lock = new Object();

    private boolean isRecording = false;
    private boolean stopRecording = false;

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo bufferInfo;

    private SurfaceTexture.OnFrameAvailableListener cameraInputListener
            = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            synchronized (lock) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onNewFrame();
                        }
                    });
                }
            }
        }
    };

    public TimeLapseRecording(Camera camera, CamcorderProfile camcorderProfile) throws IOException {
        this.camera = camera;
        this.cameraProfile = camcorderProfile;

        synchronized (lock) {
            new Thread(recordRunnable, "time lapse recording").start();

            while (handler == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public SurfaceTexture getCameraSurfaceTexture() {
        return cameraTextureSource.getSurfaceTexture();
    }

    public void release() {
        synchronized (lock) {
            if (looper != null) {
                handler = null;
                looper.quit();
                looper = null;
            }
        }
    }

    private void prepareEncoder(int width, int height, int bitRate) {
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoderInputSurface = encoder.createInputSurface();
        encoder.start();
    }

    private Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                prepareEncoder(cameraProfile.videoFrameWidth, cameraProfile.videoFrameHeight, cameraProfile.videoBitRate);

                codecInputSurface = new CodecInputSurface(encoderInputSurface);
                codecInputSurface.makeCurrent();

                textureRender = new STextureRender();
                try {
                    cameraTextureSource = new CameraTextureSource(camera, textureRender.getTextureId(), cameraInputListener);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Looper.prepare();
                looper = Looper.myLooper();
                handler = new Handler(Looper.myLooper());
                lock.notify();
            }
            Looper.loop();

            // clean up
            if (encoder != null) {
                encoder.stop();
                encoder.release();
                encoder = null;
            }
            if (codecInputSurface != null) {
                codecInputSurface.release();
                codecInputSurface = null;
            }
            if (muxer != null) {
                if (muxerStarted) {
                    muxer.stop();
                    muxerStarted = false;
                }
                muxer.release();
                muxer = null;
            }
        }
    };


    private void onNewFrame() {
        cameraTextureSource.updateTexImage();

        codecInputSurface.makeCurrent();

        if (isRecording) {
            if (stopRecording) {
                isRecording = false;
                // send end-of-stream to encoder, and drain remaining output
                drainEncoder(true);

                if (muxerStarted) {
                    muxer.stop();
                    muxerStarted = false;
                    muxer.release();
                    muxer = null;
                }
                return;
            }

            // Feed any pending encoder output into the muxer.
            drainEncoder(false);

            int frameCount = 0;


            // Switch up the colors every 15 frames.  Besides demonstrating the use of
            // fragment shaders for video editing, this provides a visual indication of
            // the frame rate: if the camera is capturing at 15fps, the colors will change
            // once per second.
            if ((frameCount % 15) == 0) {

            }
            frameCount++;

            SurfaceTexture surfaceTexture = cameraTextureSource.getSurfaceTexture();
            textureRender.drawFrame(surfaceTexture);

            // Set the presentation time stamp from the SurfaceTexture's time stamp.  This
            // will be used by MediaMuxer to set the PTS in the video.
            codecInputSurface.setPresentationTime(surfaceTexture.getTimestamp());

            // Submit it to the encoder.  The eglSwapBuffers call will block if the input
            // is full, which would be bad if it stayed full until we dequeued an output
            // buffer (which we can't do, since we're stuck here).  So long as we fully drain
            // the encoder before supplying additional input, the system guarantees that we
            // can supply another frame without blocking.
            codecInputSurface.swapBuffers();
        }
    }

    public void startRecording(String outputPath) {
        synchronized (lock) {
            // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
            // because our MediaFormat doesn't have the Magic Goodies.  These can only be
            // obtained from the encoder after it has started processing data.
            //
            // We're not actually interested in multiplexing audio.  We just want to convert
            // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
            try {
                muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException ioe) {
                throw new RuntimeException("MediaMuxer creation failed", ioe);
            }

            trackIndex = -1;

            isRecording = true;
        }
    }

    public void stopRecording() throws InterruptedException {
        synchronized (lock) {
            if (isRecording)
                stopRecording = true;
        }
    }

    /**
     * Extracts all pending data from the encoder and forwards it to the muxer.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     * <p>
     * We're just using the muxer to get a .mp4 file (instead of a raw H.264 stream).  We're
     * not recording audio.
     */
    private void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;

        if (endOfStream)
            encoder.signalEndOfInputStream();

        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        while (true) {
            int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = encoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (muxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = encoder.getOutputFormat();
                Log.d("drainEncoder", "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                trackIndex = muxer.addTrack(newFormat);
                muxer.start();
                muxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w("drainEncoder", "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    bufferInfo.size = 0;
                }

                if (bufferInfo.size != 0) {
                    if (!muxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(bufferInfo.offset);
                    encodedData.limit(bufferInfo.offset + bufferInfo.size);

                    muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                }

                encoder.releaseOutputBuffer(encoderStatus, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w("drainEncoder", "reached end of stream unexpectedly");
                    }
                    break;      // out of while
                }
            }
        }
    }
}
