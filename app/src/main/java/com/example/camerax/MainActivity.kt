package com.example.camerax

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco


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
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS,REQUEST_CODE)
        }

        Clicked.setOnClickListener {
            val intent = Intent(this, DisplayCameraActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var path="testing"
        path="/storage/emulated/0/Android/media/com.example.camerax/1561627301639.jpg"
        if(requestCode==REQUEST_CODE)  //TODO: add resultcode == OK
        {
             path= data!!.getStringExtra("path")!!
            Log.e("test",path)
        }
        Log.e("test", "display capture")
        var imagePath= "file://$path"
        my_image_view.setImageURI(imagePath)
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


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE) {
            if (allPermissionsGranted()) {
                texture_view.post {
                    //startCameraForPreview()
                    val intent = Intent(this, DisplayCameraActivity::class.java)
                    startActivityForResult(intent,requestCode)       //TODO add for result and request code
                }
            } else {
//                "Permissions not granted by the user.".toast()
//                Toast.makeText(Activity(),"Permissions not granted by the user",Toast.LENGTH_SHORT)
                finish()
            }
        }
    }
}
