package com.example.camerax

import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Matrix
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.CameraX.getContext
import kotlinx.android.synthetic.main.display_camera_activity.*
import java.io.File
import android.R
import android.widget.ToggleButton
import android.widget.CompoundButton
import android.R.id.toggle
import android.opengl.Visibility
import kotlinx.android.synthetic.main.activity_main.*


class DisplayCameraActivity : AppCompatActivity() {

    private lateinit var toggle: ToggleButton
    var checker:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.camerax.R.layout.display_camera_activity)
        toggle = findViewById<View>(com.example.camerax.R.id.simpleToggleButton) as ToggleButton
        checker=1
        startCameraForCapture()

    }

    @SuppressLint("RestrictedApi")
    private fun startCameraForCapture() {


        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(400, 640))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = textureView.parent as ViewGroup
            parent.removeView(textureView)
            parent.addView(textureView, 0)
            textureView.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        val videoCapture = VideoCapture(videoCaptureConfig)

        button.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
//                    msg.toast()
                    var intent = intent
                    intent.putExtra("path", file.absolutePath)
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

                @SuppressLint("RestrictedApi")
                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed: $message"
//                    msg.toast()
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }
            })
        }


        button2!!.setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")

            videoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {
                override fun onVideoSaved(file: File?) {
                    val msg = "Video capture succeeded: ${file!!.absolutePath}"
                    var intent = intent
                    intent.putExtra("path", file.absolutePath)
                    Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }

                @SuppressLint("RestrictedApi")
                override fun onError(useCaseError: VideoCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed: $message"
//                    msg.toast()
                    Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }
            })

        }

        button3!!.setOnClickListener {
            videoCapture.stopRecording()
        }

        toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The toggle is enabled
//                CameraX.unbindAll()
//                CameraX.bindToLifecycle(this, preview, imageCapture) // For Preview and capture
                checker=0
                button.visibility=View.VISIBLE
                button2.visibility = View.GONE;
                button3.visibility = View.GONE;
            }

            else {
                // The toggle is disabled
//                CameraX.unbindAll()
//                CameraX.bindToLifecycle(this, preview, imageCapture)
                checker=1
                button2.visibility=View.VISIBLE
                button3.visibility=View.VISIBLE
                button.visibility = View.GONE;
        }
        })

//        CameraX.bindToLifecycle(this, preview,videoCapture)

        if (checker==0) {
//            CameraX.unbindAll()
//            CameraX.bindToLifecycle(this, preview,imageCapture)
        }
        else
        {

//            CameraX.unbindAll()
            CameraX.bindToLifecycle(this, preview,videoCapture)
        }

    }


    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        // Correct preview output to account for display rotation
//        val rotationDegree=when(textureView.display.rotation){
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
//            else -> return
//        }
//
//        matrix.postRotate(rotationDegree.toFloat(),centerX,centerY)
//        matrix.postRotate(0 as Float,centerX,centerY)

        // Finally, apply transformations to our TextureView
        textureView.setTransform(matrix)
    }

}
