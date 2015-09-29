/**
 * Camera experiment plugin.
 *
 * The Android MediaPlayer does not support reliable seeking to a certain frame. For this reason the decoder package
 * contains a {@link nz.ac.auckland.lablet.camera.decoder.SeekToFrameExtractor} class that extracts a frame using the
 * Android MediaCodec api.
 *
 * In order to allow very long experiments that don't require high recoding frame rates the recorder package contains a
 * {@link nz.ac.auckland.lablet.camera.recorder.VideoRecorder} class to record a video at a low frame rate.
 */
package nz.ac.auckland.lablet.camera;