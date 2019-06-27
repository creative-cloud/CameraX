package com.example.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Camera
import android.graphics.Matrix
import android.os.Build
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.CameraX.getContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import java.io.File

//import com.android.example.cameraxbasic.utils.FLAGS_FULLSCREEN


class MainActivity : AppCompatActivity(){

    private val REQUEST_CODE =100
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA) //TODO: add camera permissions and record audio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_main)
        val Clicked = findViewById<View>(R.id.button1) as Button

        if(allPermissionsGranted())
        {
            texture_view.post{
//                val intent = Intent(this, DisplayCameraActivity::class.java)
//                startActivity(intent)
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE)
        }

        Clicked.setOnClickListener {
            //add actions for cameraX module
            val intent = Intent(this, DisplayCameraActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE)
//            CameraX.bindToLifecycle(this)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    /*private fun startCameraForCapture() {
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(440, 640))
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {
            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = texture_view.parent as ViewGroup
            parent.removeView(texture_view)
            parent.addView(texture_view, 0)
            texture_view.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig= ImageCaptureConfig.Builder().apply {
            setTargetAspectRatio(Rational(1,1))
            // We don't set a resolution for image capture; instead, we
            // select a capture mode which will infer the appropriate
            // resolution based on aspect ration and requested mode
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        // Build the viewfinder use case
        val imageCapture = ImageCapture(imageCaptureConfig)

        button1.setOnClickListener {

            val intent = Intent(this, DisplayCameraActivity::class.java).apply {
//                putExtra(EXTRA_MESSAGE, message)
            }
            startActivity(intent)

            val file = File(externalMediaDirs.first(),"${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file,object : ImageCapture.OnImageSavedListener{
                @SuppressLint("RestrictedApi")
                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
//                    msg.toast()
                    Toast.makeText(getContext(),msg,Toast.LENGTH_SHORT).show()
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
        CameraX.bindToLifecycle(this,preview,imageCapture) // For Preview and capture

    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var path="testing"
//        path="/storage/emulated/0/Android/media/com.example.camerax/1561620646772.jpg"
//        if(requestCode==REQUEST_CODE && resultCode== Activity.RESULT_OK)
//        {
//             path= data!!.getStringExtra("path")!!
////            Log.e("test",path)
//        }
//        Log.v("test", "captured image")
//        var imagePath= "file://$path";
//        my_image_view.setImageURI(imagePath);
    }


    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }


/*
    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX =texture_view.width/2f
        val centerY =texture_view.height/2f

        // Correct preview output to account for display rotation
        val rotationDegree=when(texture_view.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegree.toFloat(),centerX,centerY)

        // Finally, apply transformations to our texture_view
        texture_view.setTransform(matrix)
    }
*/

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
                texture_view.post {
                    //startCameraForPreview()
                    val intent = Intent(this, DisplayCameraActivity::class.java)
                    startActivity(intent)       //TODO add for result and request code
                }
            } else {
//                "Permissions not granted by the user.".toast()
//                Toast.makeText(Activity(),"Permissions not granted by the user",Toast.LENGTH_SHORT)
                finish()
            }
        }
    }

}
