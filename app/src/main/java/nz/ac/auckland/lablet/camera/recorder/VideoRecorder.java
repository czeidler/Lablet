/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Most code is take from http://bigflake.com/mediacodec/CameraToMpegTest.java.txt.
 * To get the video preview working looking at https://github.com/Kickflip helped a lot.
 */
package nz.ac.auckland.lablet.camera.recorder;

import android.media.*;
import android.opengl.*;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;


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
    public CodecInputSurface(Surface surface, EGLContext sharedContext) {
        if (surface == null) {
            throw new NullPointerException();
        }
        mSurface = surface;

        eglSetup(sharedContext);
    }

    /**
     * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
     */
    private void eglSetup(EGLContext sharedContext) {
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
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], sharedContext, attrib_list, 0);
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


/**
 * Custom video recording class.
 *
 * Brief description how the recording works:
 * 1) From a GL texture a SurfaceTexture is created. (This happens in the GLSurfaceView)
 * 2) Set the camera preview to that SurfaceTexture (setPreviewTexture)
 * 3) A MediaCodec is created and createInputSurface is used to setup a EGLSurface (using a shared EGLContext from the
 * GLSurfaceView)
 * 4) The recorder listens for new frames on the preview SurfaceTexture and draws the SurfaceTexture to the encoder
 * input surface (EGLSurface).
 * 5) By calling eglSwapBuffers for the EGLSurface the frame is send to the encoder.
 * 6) Processed frames from the encoder are fed to a muxer to generate a mp4.
 */
public class VideoRecorder {
    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int IFRAME_INTERVAL = 1;

    private CameraGLTextureProducer cameraGLTextureProducer;

    private CodecInputSurface codecInputSurface;
    private TextureRender textureRender;

    private MediaCodec encoder;
    private Surface encoderInputSurface;
    private int orientationHintDegrees = 0;
    private MediaMuxer muxer;
    private boolean muxerStarted = false;
    private int trackIndex;
    private Looper looper = null;
    private Handler handler = null;

    final private Object lock = new Object();

    private boolean isRecording = false;
    private boolean stopRecording = false;

    private float recordingFrameRate = 30f;
    private int recordedFrames = 0;

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo bufferInfo;

    public VideoRecorder() {
        reset();
    }

    private CameraGLTextureProducer.IListener frameListener = new CameraGLTextureProducer.IListener() {
        @Override
        public void onNewFrame() {
            synchronized (lock) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            handleNewFrame();
                        }
                    });
                }
            }
        }
    };

    private void handleNewFrame() {
        if (isRecording) {
            if (stopRecording) {
                // send end-of-stream to encoder, and drain remaining output
                drainEncoder(true);
                // stop recording
                cleanUpRecording();

                stopRecording = false;
                isRecording = false;
                return;
            }
            // Feed any pending encoder output into the muxer.
            drainEncoder(false);

            int nthFrameToRecord = (int)(30f / recordingFrameRate);
            if ((recordedFrames % nthFrameToRecord) == 0)
                sendFrameToEncoder();

            recordedFrames++;
        }
    }

    private void sendFrameToEncoder() {
        if (codecInputSurface == null)
            return;
        codecInputSurface.makeCurrent();

        textureRender.render(cameraGLTextureProducer.getGLTextureId(), cameraGLTextureProducer.getSurfaceTexture(),
                orientationHintDegrees);

        // Set the presentation time stamp from the SurfaceTexture's time stamp.  This
        // will be used by MediaMuxer to set the PTS in the video.
        codecInputSurface.setPresentationTime(cameraGLTextureProducer.getSurfaceTexture().getTimestamp());
        // Submit it to the encoder.  The eglSwapBuffers call will block if the input
        // is full, which would be bad if it stayed full until we dequeued an output
        // buffer (which we can't do, since we're stuck here).  So long as we fully drain
        // the encoder before supplying additional input, the system guarantees that we
        // can supply another frame without blocking.
        codecInputSurface.swapBuffers();
    }

    public void reset() {
        synchronized (lock) {
            release();

            recordedFrames = 0;

            // start recording thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        Looper.prepare();
                        looper = Looper.myLooper();
                        handler = new Handler(Looper.myLooper());
                        lock.notify();
                    }
                    Looper.loop();

                    cleanUpRecording();
                }
            }, "VideoRecorder").start();

            while (handler == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (cameraGLTextureProducer != null)
                setCameraSource(cameraGLTextureProducer);
        }
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

    private void prepareEncoder(int width, int height, int videoBitRate) {
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
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

    private void cleanUpRecording() {
        // clean up
        if (encoder != null) {
            encoder.stop();
            encoder.release();
            encoder = null;
        }

        if (muxer != null) {
            if (muxerStarted) {
                muxer.stop();
                muxerStarted = false;
            }
            muxer.release();
            muxer = null;
        }

        if (codecInputSurface != null) {
            codecInputSurface.release();
            codecInputSurface = null;
        }
        isRecording = false;
    }

    public void startRecording(int width, int height, int videoBitRate, int frameRate, String outputPath) {
        synchronized (lock) {
            try {
                recordedFrames = 0;
                prepareEncoder(width, height, videoBitRate);
                muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                muxer.setOrientationHint(orientationHintDegrees);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            codecInputSurface = new CodecInputSurface(encoderInputSurface,
                                    cameraGLTextureProducer.getSharedContext());
                            codecInputSurface.makeCurrent();
                            textureRender = new TextureRender();
                        }
                    }
                });
            } catch (IOException ioe) {
                throw new RuntimeException("MediaMuxer creation failed", ioe);
            }

            trackIndex = -1;

            isRecording = true;
        }
    }

    public void stopRecording() {
        if (!isRecording)
            return;

        final Object stoppingSem = new Object();
        synchronized (stoppingSem) {
            stopRecording = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (stoppingSem) {
                        // make sure that the stopRecording event is received (we are blocking the looper that receives
                        // onNewFrameAvailable events)
                        handleNewFrame();

                        stoppingSem.notifyAll();
                    }
                }
            });

            while (isRecording) {
                try {
                    stoppingSem.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            reset();
        }
    }

    public void setCameraSource(final CameraGLTextureProducer producer) {
        cameraGLTextureProducer = producer;
        cameraGLTextureProducer.addListener(frameListener);
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

    public void setRotation(int orientationHintDegrees) {
        this.orientationHintDegrees = orientationHintDegrees;
    }

    public void setRecordingFrameRate(float recordingFrameRate) {
        this.recordingFrameRate = recordingFrameRate;
    }
}
