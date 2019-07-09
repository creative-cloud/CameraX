package com.example.camerax

import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Matrix
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.CameraX.getContext
import kotlinx.android.synthetic.main.display_camera_activity.*
import java.io.File
import android.widget.ToggleButton
import android.widget.CompoundButton
import androidx.lifecycle.LifecycleOwner


class DisplayCameraActivity : AppCompatActivity() {

    private lateinit var toggle: ToggleButton
    private lateinit var vidtoggle: ToggleButton
    lateinit var capture: UseCase
    var imageSize: Size? = null
    var jpegSizes: Array<Size>? = null
    var width = 0
    var height = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.camerax.R.layout.display_camera_activity)
        toggle = findViewById<View>(com.example.camerax.R.id.simpleToggleButton) as ToggleButton
        vidtoggle = findViewById<View>(com.example.camerax.R.id.vidToggleButton) as ToggleButton
        vidtoggle.isChecked = false
        getDisplaySize()
        startCameraForCapture()

    }

    private fun getDisplaySize() {
        val cm: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cc = cm.getCameraCharacteristics(0.toString())
        val streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        jpegSizes = streamConfigs!!.getOutputSizes(ImageFormat.JPEG)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x
        height = size.y
        imageSize = getOptimalPreviewSize(jpegSizes, width, height)

    }

    @SuppressLint("RestrictedApi")
    private fun startCameraForCapture() {

        CameraX.unbindAll()
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(4, 3))
            setTargetResolution(Size(imageSize!!.width, imageSize!!.height))
            Log.e("" + imageSize!!.height, "" + imageSize!!.width)
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
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setLensFacing(CameraX.LensFacing.BACK)
            Log.e("" + imageSize!!.height, "" + imageSize!!.width)
            setTargetResolution(Size(imageSize!!.width, imageSize!!.height))

        }.build()

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            Log.e("" + imageSize!!.height, "" + imageSize!!.width)
            setTargetResolution(Size(imageSize!!.width, imageSize!!.height))
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        val videoCapture = VideoCapture(videoCaptureConfig)
        var my_intent: Intent = intent
        var path = ""

        button.setOnClickListener {
            CameraX.bindToLifecycle(this, imageCapture)
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
                    var intent = intent
                    path = file.absolutePath
                    intent.putExtra("path", path)
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, my_intent)
                    CameraX.unbind(imageCapture)
                    finish()
                }

                @SuppressLint("RestrictedApi")
                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed: $message"
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }
            })
        }





        vidtoggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {        //start video-button
                val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")
                var filePath:String=" "
                CameraX.bindToLifecycle(this as LifecycleOwner, videoCapture)
                Log.e("testing", "start vid")
                videoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {
                    override fun onVideoSaved(file: File?) {
                        val msg = "Video capture succeeded: ${file!!.absolutePath}"
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                        my_intent = intent
                        path = file.absolutePath
                        my_intent.putExtra("path", path)

                        //COMPRESSION
//                        thread(start = true) {
//                            println("running from thread(): ${Thread.currentThread()}")
//                             filePath = SiliCompressor.with(applicationContext)
//                                .compressVideo(path, "/storage/emulated/0/Android/media/com.example.camerax/",480,320,450000)
//                        }

                        Log.e("Compressed", filePath)
                        setResult(Activity.RESULT_OK, my_intent)
                        finish()

                    }

                    @SuppressLint("RestrictedApi")
                    override fun onError(useCaseError: VideoCapture.UseCaseError, message: String, cause: Throwable?) {
                        val msg = "Photo capture failed: $message"
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                        cause?.printStackTrace()
                    }
                })
            } else {
                videoCapture.stopRecording()
            }
        })


        toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Log.e("TEST", "comes to toggle")
                capture = imageCapture
                button.visibility = View.VISIBLE
                vidtoggle.visibility = View.GONE
            } else {
                capture = videoCapture
                vidtoggle.visibility = View.VISIBLE
                button.visibility = View.GONE
            }

        })
        CameraX.bindToLifecycle(this, preview)
    }


    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = textureView.width / 2f
        val centerY = textureView.height / 2f

        textureView.setTransform(matrix)
    }

    private fun getOptimalPreviewSize(sizes: Array<Size>?, w: Int, h: Int): Size? {
        Log.e("getDisplaySize", " $sizes")
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w

        if (sizes == null) return null

        var optimalSize: Size? = null
        var minDiff = java.lang.Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        if (optimalSize == null) {
            minDiff = java.lang.Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize
    }

}