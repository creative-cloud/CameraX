package com.example.camerax

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.activity_fullscreen.*
import kotlinx.android.synthetic.main.display_camera_activity.button
import kotlinx.android.synthetic.main.display_camera_activity.textureView
import kotlinx.android.synthetic.main.display_camera_activity.*
import java.io.File

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
//        fullscreen_content.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_LOW_PROFILE or
//                    View.SYSTEM_UI_FLAG_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
//        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.camerax.R.layout.display_camera_activity)
        toggle = findViewById<View>(com.example.camerax.R.id.simpleToggleButton) as ToggleButton
        vidtoggle = findViewById<View>(com.example.camerax.R.id.vidToggleButton) as ToggleButton
        vidtoggle.isChecked=false
        checker = 1
        startCameraForCapture()
//        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        // Set up the user interaction to manually show or hide the system UI.
//        fullscreen_content.setOnClickListener { toggle() }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        dummy_button.setOnTouchListener(mDelayHideTouchListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
//        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
//        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }


        private lateinit var toggle: ToggleButton
        private lateinit var vidtoggle: ToggleButton
        var checker: Int = 0
        lateinit var capture: UseCase


        @SuppressLint("RestrictedApi")
        private fun startCameraForCapture() {

            CameraX.unbindAll()
            val previewConfig = PreviewConfig.Builder().apply {
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
            }.build()

            val videoCaptureConfig = VideoCaptureConfig.Builder().apply {
            }.build()

            val imageCapture = ImageCapture(imageCaptureConfig)
            val videoCapture = VideoCapture(videoCaptureConfig)
            var my_intent: Intent =intent
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
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK, my_intent)
                        CameraX.unbind(imageCapture)
                        finish()
                    }

                    @SuppressLint("RestrictedApi")
                    override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
                        val msg = "Photo capture failed: $message"
//                    msg.toast()
                        Toast.makeText(CameraX.getContext(), msg, Toast.LENGTH_SHORT).show()
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

    }


