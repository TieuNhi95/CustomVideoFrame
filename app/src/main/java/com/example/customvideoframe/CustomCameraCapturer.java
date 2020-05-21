package com.example.customvideoframe;

import android.content.Context;

import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoFrame;

public class CustomCameraCapturer extends CameraCapturer {
    public CustomCameraCapturer(Context context, CameraSource cameraSource) {
        super(context, cameraSource);
    }

    public CustomCameraCapturer(Context context,
                                CameraSource cameraSource,
                                Listener listener) {
        super(context, cameraSource, listener);
    }

    @Override
    public void startCapture(VideoFormat captureFormat,
                             VideoCapturer.Listener videoCapturerListener) {
        super.startCapture(captureFormat, new CameraFrameProxy(videoCapturerListener));
    }

    private static class CameraFrameProxy implements VideoCapturer.Listener {
        private final VideoCapturer.Listener videoCapturerListener;

        public CameraFrameProxy(VideoCapturer.Listener videoCapturerListener) {
            this.videoCapturerListener = videoCapturerListener;
        }

        @Override
        public void onCapturerStarted(boolean success) {
            videoCapturerListener.onCapturerStarted(success);
        }

        @Override
        public void onFrameCaptured(VideoFrame videoFrame) {
            VideoFrame processedFame = processFrame(videoFrame);

            videoCapturerListener.onFrameCaptured(processedFame);
        }

        private VideoFrame processFrame(VideoFrame videoFrame) {
            // TODO: Process camera frame

            return videoFrame;
        }
    }
}