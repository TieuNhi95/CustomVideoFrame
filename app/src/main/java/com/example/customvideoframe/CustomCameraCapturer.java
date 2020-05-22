package com.example.customvideoframe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoFrame;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CustomCameraCapturer extends CameraCapturer {

    BlurEngine blurEngine = new BlurStackOptimized();

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

        int inputWidth = videoFrame.dimensions.width;
        int inputHeight = videoFrame.dimensions.height;
        Matrix matrix = new Matrix();

        matrix.postRotate(videoFrame.orientation.getValue());


        if (frameListener != null) {
            byte[] data = videoFrame.imageBuffer;
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, inputWidth, inputHeight, null);
//            yuvImage.compressToJpeg(new Rect(0, 0, videoFrame.dimensions.width, videoFrame.dimensions.height), 100, out);
//            byte[] imageBytes = out.toByteArray();
//            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//            frameListener.onFrameCaptured(bitmap);
//
//            byte[] byteArray = out.toByteArray();


            int[] data2 = convertYUV420_NV21toRGB8888(data, inputWidth, inputHeight);

            Bitmap bm = Bitmap.createBitmap(data2, inputWidth, inputHeight, Bitmap.Config.ARGB_8888);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            Bitmap bitmapBlur = blurEngine.blur(rotatedBitmap, 100);
            frameListener.onFrameCaptured(bitmapBlur);

            int size = bitmapBlur.getRowBytes() * bitmapBlur.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            bitmapBlur.copyPixelsToBuffer(byteBuffer);

//            encodeYUV420SP(data,data2,videoFrame.dimensions.width, videoFrame.dimensions.height);

            byte[] yuv = colorconvertRGB_IYUV_I420(data2,inputWidth,inputHeight);

//            encodeYV12(yuv, data2, inputWidth, inputHeight);

            videoFrame = new VideoFrame(yuv, videoFrame.dimensions, videoFrame.orientation, videoFrame.timestamp);

        }
        return videoFrame;
    }

    public static byte[] colorconvertRGB_IYUV_I420(int[] aRGB, int width, int height) {
        final int frameSize = width * height;
        final int chromasize = frameSize / 4;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + chromasize;
        byte [] yuv = new byte[width*height*3/2];

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                //a = (aRGB[index] & 0xff000000) >> 24; //not using it right now
                R = (aRGB[index] & 0xff0000) >> 16;
                G = (aRGB[index] & 0xff00) >> 8;
                B = (aRGB[index] & 0xff) >> 0;

                Y = ((66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = (( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = (( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                yuv[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));

                if (j % 2 == 0 && index % 2 == 0)
                {
                    yuv[uIndex++] = (byte)((U < 0) ? 0 : ((U > 255) ? 255 : U));
                    yuv[vIndex++] = (byte)((V < 0) ? 0 : ((V > 255) ? 255 : V));
                }

                index ++;
            }
        }
        return yuv;
    }

    public static int[] convertYUV420_NV21toRGB8888(byte[] data, int width, int height) {
        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];
        int u, v, y1, y2, y3, y4;

        // i percorre os Y and the final pixels
        // k percorre os pixles U e V
        for (int i = 0, k = 0; i < size; i += 2, k += 2) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            u = data[offset + k] & 0xff;
            v = data[offset + k + 1] & 0xff;
            u = u - 128;
            v = v - 128;

            pixels[i] = convertYUVtoRGB(y1, u, v);
            pixels[i + 1] = convertYUVtoRGB(y2, u, v);
            pixels[width + i] = convertYUVtoRGB(y3, u, v);
            pixels[width + i + 1] = convertYUVtoRGB(y4, u, v);

            if (i != 0 && (i + 2) % width == 0)
                i += width;
        }

        return pixels;
    }

    private static int convertYUVtoRGB(int y, int u, int v) {
        int r, g, b;

        r = y + (int) (1.402f * v);
        g = y - (int) (0.344f * u + 0.714f * v);
        b = y + (int) (1.772f * u);
        r = r > 255 ? 255 : Math.max(r, 0);
        g = g > 255 ? 255 : Math.max(g, 0);
        b = b > 255 ? 255 : Math.max(b, 0);
        return 0xff000000 | (b << 16) | (g << 8) | r;
    }

//    void yourFunction(Bitmap mBitmap){
//
//        // mBitmap is your bitmap
//
//        int mWidth = mBitmap.getWidth();
//        int mHeight = mBitmap.getHeight();
//
//        int[] mIntArray = new int[mWidth * mHeight];
//
//        // Copy pixel data from the Bitmap into the 'intArray' array
//        mBitmap.getPixels(mIntArray, 0, mWidth, 0, 0, mWidth, mHeight);
//
//        // Call to encoding function : convert intArray to Yuv Binary data
//        encodeYUV420SP(data, intArray, mWidth, mHeight);
//
//    }


    private void encodeYV12(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uIndex = frameSize;
        int vIndex = frameSize + (frameSize / 4);

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // YV12 has a plane of Y and two chroma plans (U, V) planes each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[vIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    private FrameListener frameListener;

    public void setFrameListener(FrameListener frameListener) {
        this.frameListener = frameListener;
    }

    interface FrameListener {
        void onFrameCaptured(Bitmap bitmap);
    }
}