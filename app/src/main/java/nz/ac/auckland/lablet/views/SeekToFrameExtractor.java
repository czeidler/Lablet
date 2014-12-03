/*
 * Copyright 2013-2014.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.lablet.views;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;


/**
 * Reads a video file and displays it at given time position on the surface. The surface must be fully initialized.
 */
class SeekToFrameExtractor {
    SeekToThread seekToThread;
    private final Semaphore threadReadySemaphore = new Semaphore(0);

    public SeekToFrameExtractor(File mediaFile, Surface surface) throws IOException {
        seekToThread = new SeekToThread(mediaFile, surface);
        seekToThread.start();
        // wait till thread is up and running
        try {
            threadReadySemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        seekToThread.quit();
    }

    public boolean seekToFrame(long positionMicroSeconds) {
        Handler seekHandler = seekToThread.getHandler();
        seekHandler.removeMessages(SeekToThread.SEEK_MESSAGE);
        Message message = new Message();
        message.what = SeekToThread.SEEK_MESSAGE;
        Bundle bundle = new Bundle();
        bundle.putLong("position", positionMicroSeconds);
        message.setData(bundle);
        return seekHandler.sendMessage(message);
    }

    class SeekToThread extends Thread {
        final static int SEEK_MESSAGE = 1;

        private MediaExtractor extractor;
        private MediaCodec decoder;
        private MediaCodec.BufferInfo bufferInfo;
        ByteBuffer[] inputBuffers;

        Handler seekHandler;

        public SeekToThread(File mediaFile, Surface surface) throws IOException {
            extractor = new MediaExtractor();
            extractor.setDataSource(mediaFile.getPath());

            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    decoder = MediaCodec.createDecoderByType(mime);
                    decoder.configure(format, surface, null, 0);

                    break;
                }
            }
            if (decoder == null)
                throw new IOException();

            decoder.start();

            bufferInfo = new MediaCodec.BufferInfo();
        }

        // thread safe
        public Handler getHandler() {
            return seekHandler;
        }

        // thread safe
        public void quit() {
            seekHandler.getLooper().quit();
            try {
                seekToThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            decoder.stop();
            decoder.release();
            extractor.release();
        }

        public void run() {
            Looper.prepare();
            seekHandler = new Handler() {
                @Override
                public void handleMessage(Message message)
                {
                    if (message.what != SEEK_MESSAGE)
                        return;
                    Bundle data = message.peekData();
                    assert data != null;

                    long positionMicroSeconds = data.getLong("position");
                    performSeekTo(positionMicroSeconds);
                }

            };
            threadReadySemaphore.release();
            Looper.loop();
        }

        private void performSeekTo(long seekTarget) {
            final int DEQUE_TIMEOUT = 1000;

            decoder.flush();
            inputBuffers = decoder.getInputBuffers();

            // coarse seek
            extractor.seekTo(seekTarget, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            // fine manual seek
            boolean positionReached = false;
            while (!positionReached) {
                int inIndex = decoder.dequeueInputBuffer(DEQUE_TIMEOUT);
                if (inIndex >= 0) {
                    ByteBuffer buffer = inputBuffers[inIndex];

                    int sampleSize = extractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        positionReached = true;
                    } else {
                        decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                        extractor.advance();
                    }
                }

                int outIndex = decoder.dequeueOutputBuffer(bufferInfo, DEQUE_TIMEOUT);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    case MediaCodec.INFO_TRY_AGAIN_LATER:
                        break;
                    default:
                        boolean render = false;
                        if (bufferInfo.presentationTimeUs - seekTarget >= 0) {
                            positionReached = true;
                            render = true;
                        }

                        decoder.releaseOutputBuffer(outIndex, render);
                        if (render)
                            decoder.flush();
                        break;
                }
            }
        }
    }
}
