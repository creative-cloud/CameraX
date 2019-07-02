package com.example.camerax

import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Matrix
import android.hardware.Camera
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
    var checker: Int = 0
    lateinit var capture: UseCase
    var size: Camera.Size? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.camerax.R.layout.display_camera_activity)
        toggle = findViewById<View>(com.example.camerax.R.id.simpleToggleButton) as ToggleButton
        vidtoggle = findViewById<View>(com.example.camerax.R.id.vidToggleButton) as ToggleButton
        vidtoggle.isChecked=false
        checker = 1
        var camera=Camera.open(0)
        var list = camera.parameters.supportedPreviewSizes

        size=getOptimalPreviewSize(list,5,5)
        startCameraForCapture()


    }

    @SuppressLint("RestrictedApi")
    private fun startCameraForCapture() {

        CameraX.unbindAll()
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(4,3))
            setTargetResolution(Size(size!!.width,size!!.height))
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
            setTargetResolution(Size(size!!.width,size!!.height))

        }.build()

        val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            setTargetResolution(Size(size!!.width,size!!.height))
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        val videoCapture = VideoCapture(videoCaptureConfig)
        var my_intent:Intent=intent
        var path =""

        button.setOnClickListener {
            CameraX.bindToLifecycle(this,imageCapture)
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file, object : ImageCapture.OnImageSavedListener {
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
//                    msg.toast()
                    var intent = intent
                    path=file.absolutePath
                    intent.putExtra("path", path)
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK, my_intent)
                    CameraX.unbind(imageCapture)
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





        vidtoggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {        //start video-button 2
                val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.mp4")
                CameraX.bindToLifecycle(this as LifecycleOwner, videoCapture)
                Log.e("testing","start vid")
                videoCapture.startRecording(file, object : VideoCapture.OnVideoSavedListener {
                    override fun onVideoSaved(file: File?) {
                        val msg = "Video capture succeeded: ${file!!.absolutePath}"
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
//                        var intent: Intent = intent
//                        intent.putExtra("path", file.absolutePath)
                        my_intent=intent
                        path=file.absolutePath
                        my_intent.putExtra("path",path)
                        setResult(Activity.RESULT_OK, my_intent)
                        finish()
//                        my_intent.putExtra("path",file.absolutePath)
                    }


                    @SuppressLint("RestrictedApi")
                    override fun onError(useCaseError: VideoCapture.UseCaseError, message: String, cause: Throwable?) {
                        val msg = "Photo capture failed: $message"
//                    msg.toast()
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                        cause?.printStackTrace()
                    }
                })
            } else {   //stop video button 3
                videoCapture.stopRecording()
            }
        })


        toggle.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The toggle is enabled
//                CameraX.unbindAll()
//                CameraX.bindToLifecycle(this, preview, imageCapture) // For Preview and capture
                Log.e("TEST", "comes to toggle")
                checker = 0
                capture = imageCapture
                button.visibility = View.VISIBLE
//                button2.visibility = View.GONE;
//                button3.visibility = View.GONE;
                vidtoggle.visibility=View.GONE
            } else {
                // The toggle is disabled
//                CameraX.unbindAll()
//                CameraX.bindToLifecycle(this, preview, imageCapture)
                checker = 1
                capture = videoCapture
//                button2.visibility = View.VISIBLE
//                button3.visibility = View.VISIBLE
                vidtoggle.visibility=View.VISIBLE
                button.visibility = View.GONE;
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

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>?, w: Int, h: Int): Camera.Size? {
        Log.e("test", " $sizes")
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w

        if (sizes == null) return null

        var optimalSize: Camera.Size? = null
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
        Log.e("" + optimalSize!!.height, "" + optimalSize!!.width)
        return optimalSize
    }

}
