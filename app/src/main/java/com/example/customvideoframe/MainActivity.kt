package com.example.customvideoframe

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.twilio.video.CameraCapturer
import com.twilio.video.LocalVideoTrack
import com.twilio.video.VideoView

class MainActivity : AppCompatActivity() {

    private val CAMERA_MIC_PERMISSION_REQUEST_CODE = 1

    private var localVideoTrack: LocalVideoTrack? = null
    private var localVideoView: VideoView? = null
    private var img: ImageView? = null

    private var cameraCapturerCompat: CameraCapturerCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        localVideoView = findViewById(R.id.videoView)
        img = findViewById(R.id.img)

        requestPermissionForCameraAndMicrophone()

        cameraCapturerCompat = CameraCapturerCompat(this, getAvailableCameraSource(), img!!)
    }

    private fun getAvailableCameraSource(): CameraCapturer.CameraSource {
        return if (CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.FRONT_CAMERA))
            CameraCapturer.CameraSource.FRONT_CAMERA
        else
            CameraCapturer.CameraSource.BACK_CAMERA
    }

    override fun onResume() {
        super.onResume()
        localVideoTrack = if (localVideoTrack == null && checkPermissionForCameraAndMicrophone()) {
            LocalVideoTrack.create(
                this,
                true,
                cameraCapturerCompat?.videoCapturer!!
            )
        } else {
            localVideoTrack
        }
        localVideoView?.let { localVideoTrack?.addRenderer(it) }

    }


    private fun checkPermissionForCameraAndMicrophone(): Boolean {
        val resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

        return resultCamera == PackageManager.PERMISSION_GRANTED &&
                resultMic == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Toast.makeText(
                this,
                "R.string.permissions_needed",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                CAMERA_MIC_PERMISSION_REQUEST_CODE
            )
        }
    }

}
