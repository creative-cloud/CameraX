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
import kotlinx.android.synthetic.main.activity_main.texture_view
import kotlinx.android.synthetic.main.display_camera_activity.*
import java.io.File

class DisplayCameraActivity :AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_camera_activity)
        startCameraForCapture()
    }

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

        val imageCaptureConfig= ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1,1))
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

//val videoCaptureConfig= VideoCaptureConfig.Builder().apply {
//            setTargetAspectRatio(Rational(1,1))
//        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
//        val videoCapture= VideoCapture(videoCaptureConfig)

        button.setOnClickListener {
            val file = File(externalMediaDirs.first(),"${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file,object : ImageCapture.OnImageSavedListener{
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
//                    msg.toast()
                    var intent= intent
                    intent.putExtra("path",file.absolutePath)
                    Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK,intent)
                    finish()
                }

                @SuppressLint("RestrictedApi")
                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                    val msg = "Photo capture failed: $message"
//                    msg.toast()
                    Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show()
                    cause?.printStackTrace()
                }
            })
        }


//        button2.setOnClickListener {            //video
//
//            val file = File(externalMediaDirs.first(),"${System.currentTimeMillis()}.mp4")
//            videoCapture.startRecording(file, object: VideoCapture.OnVideoSavedListener {
//                override fun onVideoSaved(file: File?) {
//                    Log.i(tag, "Video File : $file")
//
//        }

        CameraX.bindToLifecycle(this,preview,imageCapture) // For Preview and capture
    }






    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX =textureView.width/2f
        val centerY =textureView.height/2f

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
