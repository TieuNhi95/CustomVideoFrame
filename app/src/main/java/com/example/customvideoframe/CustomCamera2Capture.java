package com.example.customvideoframe;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.twilio.video.Camera2Capturer;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoFrame;

public class CustomCamera2Capture extends Camera2Capturer {

    private static final String TAG = "CustomCamera2Capture";

    private VideoCapturer.Listener yourListener;

    private VideoCapturer.Listener myVideoCapturerListener = new VideoCapturer.Listener() {
        @Override
        public void onCapturerStarted(boolean success) {
            Log.d(TAG, "onCapturerStarted: ");
        }

        @Override
        public void onFrameCaptured(@NonNull VideoFrame videoFrame) {
            Log.d(TAG, "onFrameCaptured: ");
            //todo process videoFrame
//            videoFrame.imageBuffer == null

            yourListener.onFrameCaptured(videoFrame);
        }
    };

    /**
     * Constructs a Camera2Capturer instance.
     *
     * <p><b>Note</b>: It is possible to construct multiple instances with different camera IDs, but
     * there are often device limitations on how many camera2 sessions can be open.
     *
     * @param context  application context
     * @param cameraId unique identifier of the camera device to open that must be specified in
     *                 {@link CameraManager#getCameraIdList()}.
     * @param listener listener of camera 2 capturer events
     */
    public CustomCamera2Capture(@NonNull Context context, @NonNull String cameraId, @NonNull Listener listener) {
        super(context, cameraId, listener);
    }

    @Override
    public void startCapture(@NonNull VideoFormat captureFormat, @NonNull VideoCapturer.Listener videoCapturerListener) {
        yourListener = videoCapturerListener;
        super.startCapture(captureFormat, myVideoCapturerListener);
    }
}
