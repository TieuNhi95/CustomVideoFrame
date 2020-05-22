package com.example.customvideoframe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoFrame;

import java.io.ByteArrayOutputStream;

public class CustomCameraCapturer extends CameraCapturer {

    private VideoCapturer.Listener videoCapturerListener;
    private VideoCapturer.Listener myListener = new VideoCapturer.Listener() {
        @Override
        public void onCapturerStarted(boolean success) {
            videoCapturerListener.onCapturerStarted(success);
        }

        @Override
        public void onFrameCaptured(VideoFrame videoFrame) {
            videoCapturerListener.onFrameCaptured(processFrame(videoFrame));
        }
    };

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
        this.videoCapturerListener = videoCapturerListener;
        super.startCapture(captureFormat, myListener);
    }

    private VideoFrame processFrame(VideoFrame videoFrame) {
        // TODO: Process camera frame

        if (frameListener != null) {
            byte[] data = videoFrame.imageBuffer;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, videoFrame.dimensions.width, videoFrame.dimensions.height, null);
            yuvImage.compressToJpeg(new Rect(0, 0,  videoFrame.dimensions.width, videoFrame.dimensions.height), 50, out);
            byte[] imageBytes = out.toByteArray();
            Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            frameListener.onFrameCaptured(image);
        }
        return videoFrame;
    }

    private FrameListener frameListener;

    public void setFrameListener(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    interface FrameListener {
        void onFrameCaptured(Bitmap bitmap);
    }
}